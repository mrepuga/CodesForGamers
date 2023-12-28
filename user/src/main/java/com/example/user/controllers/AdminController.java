package com.example.user.controllers;

import com.example.user.controllers.dtos.*;
import com.example.user.entities.*;
import com.example.user.repositories.OrderCartRepository;
import com.example.user.repositories.UserRepository;
import com.example.user.services.CartItemService;
import com.example.user.services.UserService;
import com.example.user.utils.JsonMapper;
import com.example.user.utils.Sessions;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;




@Log4j2
@Controller
@RequestMapping("/admin")
public class AdminController {


    private UserRepository userRepository;

    private UserService userService;

    private CartItemService cartItemService;

    private PasswordEncoder passwordEncoder;
    private RestTemplate restTemplate;

    private OrderCartRepository orderCartRepository;

    private JsonMapper jsonMapper;

    @Value("${catalog.api.url}")
    private String catalogApiUrl;
    @Value("${upload.directory}")
    private String uploadDirectory;

    @Autowired
    public AdminController(UserRepository userRepository, UserService userService, CartItemService cartItemService, PasswordEncoder passwordEncoder, RestTemplate restTemplate, OrderCartRepository orderCartRepository, JsonMapper jsonMapper) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.cartItemService = cartItemService;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.orderCartRepository = orderCartRepository;
        this.jsonMapper = jsonMapper;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {

        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }
        return "admin/dashboard"; // Assuming you have a Thymeleaf template for the admin dashboard
    }

    @GetMapping("/createAdmin")
    public String showCreateAdminForm(@RequestParam(name = "error", required = false) String error, Model model, HttpSession session) {
        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }
        model.addAttribute("error", error);
        model.addAttribute("createUserRequest", new CreateUserRequest("", "", "", ""));

        return "admin/registerAdmin";
    }

    @PostMapping("/registerAdmin")
    public String createAdminUser(@ModelAttribute("createUserRequest") @Valid CreateUserRequest createUserRequest, BindingResult result, Model model, HttpSession session) {

        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }
        if (result.hasErrors()) {
            model.addAttribute("createUserRequest", createUserRequest);
            model.addAttribute("error", "validation");
            return "redirect:/admin/createAdmin?error=validation";
        }
        if (userService.existsUserByEmail(createUserRequest.getEmail())) {
            model.addAttribute("createUserRequest", createUserRequest);
            model.addAttribute("error", "exists");
            return "redirect:/admin/createAdmin?error=exists";
        }
        String encodedPassword = passwordEncoder.encode(createUserRequest.getPassword());

        userService.createAdminUser(
                createUserRequest.getEmail(),
                encodedPassword,
                createUserRequest.getFullName(),
                createUserRequest.getPhoneNumber()).getId();

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/viewUsers")
    public String viewUsers(Model model, HttpSession session) {
        // Fetch the list of users from the database
        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }
        List<User> allUsers = userRepository.findAll();
        List<User> userList = allUsers.stream()
                .filter(user -> !user.equals(adminUser))
                .collect(Collectors.toList());

        // Add the user list to the model
        model.addAttribute("userList", userList);

        // Return the view name (HTML template) for displaying the user list
        return "admin/viewUsers";
    }

    @PostMapping("/deleteUser/{userId}")
    public String deleteUser(@PathVariable Long userId, HttpSession session) {
        // Perform deletion logic in the service layer
        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }

        User user = userRepository.getById(userId);

        if (user.getTypeUser() == TypeUser.ADMIN) {
            userService.deleteUserById(userId);
            return "redirect:/admin/viewUsers";
        }

        List<OrderCart> orderCarts = user.getOrderCarts();
        if(!orderCarts.isEmpty()){
            for (OrderCart orderCart: orderCarts){
                for (CartItem cartItem: orderCart.getCartItems()){
                    cartItemService.deleteCartItem(cartItem.getId());
                }
            }
        }
        Cart cart = user.getCart();
        if (cart.getCartItems().isEmpty()) {
            userService.deleteUserById(userId);
        } else {
            List<CartItem> cartItems = cart.getCartItems();
            Iterator<CartItem> iterator = cartItems.iterator();

            while (iterator.hasNext()) {
                CartItem cartItem = iterator.next();
                String apiUrl = catalogApiUrl + "/CodeGameItem/unselectCodeGameItem/"+ cartItem.getCode();
                String codeResponse = restTemplate.getForObject(apiUrl, String.class);
                iterator.remove(); // Remove the current item from the list
            }
            userService.deleteUserById(userId);

        }

        Iterator<HttpSession> iterator = Sessions.getAllSessions().iterator();
        while (iterator.hasNext()) {
            HttpSession sessionFind = iterator.next();
            if (!sessionIsValid(sessionFind)) {
                // Remove the session from the Sessions utility
                iterator.remove();
                continue; // Skip to the next iteration
            }
            // Check if the session belongs to the user
            User sessionUser = (User) sessionFind.getAttribute("user");
            if (sessionUser != null && sessionUser.getId().equals(userId)) {
                // Remove the session from the Sessions utility
                sessionFind.removeAttribute("user");
                iterator.remove();
                // Explicitly invalidate the session to log out the user
                break;
            }
        }

        // Redirect to the view users page after deletion
        return "redirect:/admin/viewUsers";
    }

    private boolean sessionIsValid(HttpSession session) {
        try {
            // Try to access a session attribute to check validity
            session.getAttribute("user");
            return true;
        } catch (IllegalStateException e) {
            // Session is invalidated
            return false;
        }
    }

    @GetMapping("/modifyCatalog")
    public String modifyCatalog(Model model, HttpSession session) {
        // Fetch the list of games and categories from the external API
        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }

        String gamesApiUrl = catalogApiUrl +"/Game/";
        String categoriesApiUrl = catalogApiUrl + "/category/";

        String gamesJsonResponse = restTemplate.getForObject(gamesApiUrl, String.class);
        String categoriesJsonResponse = restTemplate.getForObject(categoriesApiUrl, String.class);

        List<Map<String, Object>> games = jsonMapper.parseJsonResponse(gamesJsonResponse);
        List<Map<String, Object>> categories = jsonMapper.parseJsonResponse(categoriesJsonResponse);


        Map<Integer, List<Map<String, Object>>> gameCategoriesMap = new HashMap<>();
        for (Map<String, Object> game : games) {
            int gameId = (int) game.get("id");
            List<Map<String, Object>> gameCategories = categories.stream()
                    .filter(category -> ((List<Integer>) game.get("categoryIds")).contains(category.get("id")))
                    .collect(Collectors.toList());
            gameCategoriesMap.put(gameId, gameCategories);
        }

        Map<Integer, List<Map<String, Object>>> categoryGamesMap = new HashMap<>();
        for (Map<String, Object> category : categories) {
            int categoryId = (int) category.get("id");
            List<Map<String, Object>> categoryGames = games.stream()
                    .filter(game -> ((List<Integer>) game.get("categoryIds")).contains(categoryId))
                    .collect(Collectors.toList());
            categoryGamesMap.put(categoryId, categoryGames);
        }


        // Add the game and category lists to the model
        model.addAttribute("games", games);
        model.addAttribute("gameCategoriesMap", gameCategoriesMap);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryGamesMap", categoryGamesMap);
        // Return the view name (HTML template) for modifying the catalog
        return "admin/modifyCatalog";
    }


    @PutMapping("/updateGame/{id}")
    public String updateGame(@PathVariable Long id,
                             @RequestParam String gameName,
                             @RequestParam String gameDescription,
                             @RequestParam String gamePublisher,
                             @RequestParam Float gamePrice,
                             RedirectAttributes redirectAttributes, HttpSession session) {

        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Fetch the old game name
        String oldGameName = "";

        ResponseEntity<String> nameResponse = restTemplate.exchange(
                catalogApiUrl+"/Game/imageReference/" + id,
                    HttpMethod.GET,
                    null,
                    String.class
            );

        oldGameName = nameResponse.getBody();

        // Create CreateGameRequest object
        CreateGameRequest createGameRequest = new CreateGameRequest(gameName.toUpperCase(), gameDescription, gamePublisher, gamePrice);
        HttpEntity<CreateGameRequest> requestEntity = new HttpEntity<>(createGameRequest, headers);

        try {
            // Make the request to the catalog API using PUT
            String url = catalogApiUrl +"/Game/updateGame/" + id;
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    String.class
            );

            // Check the response status
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                if(oldGameName==null)
                    return "redirect:/admin/modifyCatalog";
                if(!oldGameName.equals(gameName) || oldGameName.isEmpty()){
                    renameGameImageFile(oldGameName, gameName, id);
                }
            } else {
                redirectAttributes.addFlashAttribute("errorUpdateGame", "A GAME WITH THE SAME NAME ALREADY EXISTS");
                return "redirect:/admin/modifyCatalog";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorUpdateGame", "A GAME WITH THE SAME NAME ALREADY EXISTS");
            return "redirect:/admin/modifyCatalog";
        }
        return "redirect:/admin/modifyCatalog";
    }

    private void renameGameImageFile(String oldGameName, String gameName, Long gameId) {
        try {
            // Construct paths for old and new image files
            String sanitizedGameName = gameName.replaceAll("[^a-zA-Z0-9]", "_");
            String fileName = sanitizedGameName + "_" + gameId + ".jpg";

            Path oldImagePath = Paths.get(uploadDirectory, oldGameName);
            Path newImagePath = Paths.get(uploadDirectory, fileName);

            // Rename the image file
            Files.move(oldImagePath, newImagePath, StandardCopyOption.REPLACE_EXISTING);

            String apiUrl = catalogApiUrl+"/Game/uploadImageReference/"+fileName+"/"+gameId;
            restTemplate.postForEntity(apiUrl, null, String.class);

            // Log the successful renaming
            System.out.println("Image file renamed from " + oldGameName + " to " + gameName + ".jpg");
        } catch (IOException e) {
            // Handle exceptions (e.g., file not found, permission issues)
            System.err.println("Error renaming image file: " + e.getMessage());
        }

    }

    @PutMapping("/updateCategory/{id}")
    public String updateCategory(@PathVariable Long id, @RequestParam String categoryName,
                                 @RequestParam String categoryDescription,
                                 RedirectAttributes redirectAttributes, HttpSession session) {


        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }

        // Set up the request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create CreateCategoryRequest object with the new name
        CreateCategoryRequest createCategoryRequest = new CreateCategoryRequest(categoryName.toUpperCase(), categoryDescription);
        HttpEntity<CreateCategoryRequest> requestEntity = new HttpEntity<>(createCategoryRequest, headers);

        try {
            // Make the request to the catalog API using PUT
            String url = catalogApiUrl+"/category/updateCategory/" + id;
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    String.class
            );

            // Check the response status
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return "redirect:/admin/modifyCatalog";
            } else {
                redirectAttributes.addFlashAttribute("errorUpdateCategory", "A CATEGORY WITH THE SAME NAME ALREADY EXISTS");
                return "redirect:/admin/modifyCatalog";
            }
        } catch (Exception e) {
            // Handle exceptions
            redirectAttributes.addFlashAttribute("errorUpdateCategory", "A CATEGORY WITH THE SAME NAME ALREADY EXISTS");
            return "redirect:/admin/modifyCatalog";

        }

    }

    @PostMapping("/createCategory")
    public String createCategory(@RequestParam String newCategoryName,
                                 @RequestParam String newCategoryDescription,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {
        // Your creation logic here
        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Prepare the request body
        CreateCategoryRequest createCategoryRequest = new CreateCategoryRequest(newCategoryName, newCategoryDescription);

        HttpEntity<CreateCategoryRequest> requestEntity = new HttpEntity<>(createCategoryRequest, headers);


        try {
            // Make the request to the catalog API using PUT
            String url = catalogApiUrl+"/category";
            ResponseEntity<Long> responseEntity = restTemplate.postForEntity(url, requestEntity, Long.class);

            // Check the response status
            if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
                return "redirect:/admin/modifyCatalog";
            } else {
                redirectAttributes.addFlashAttribute("errorCreateCategory", "A CATEGORY WITH THE SAME NAME ALREADY EXISTS");
                return "redirect:/admin/modifyCatalog";
            }
        } catch (Exception e) {
            // Handle exceptions
            redirectAttributes.addFlashAttribute("errorCreateCategory", "A CATEGORY WITH THE SAME NAME ALREADY EXISTS");
            return "redirect:/admin/modifyCatalog";

        }

    }

    @PostMapping("/createGame")
    public String createGame(@RequestParam String newGameName,
                             @RequestParam String newGameDescription,
                             @RequestParam String newGamePublisher,
                             @RequestParam Float newGamePrice,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {

        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }
                // Your creation logic here
        RestTemplate restTemplatefind = new RestTemplate();
        String gamesJsonResponse = restTemplatefind.getForObject(catalogApiUrl+"/Game/getGameByName/"+ newGameName.toUpperCase(), String.class);


        if (gamesJsonResponse.contains(newGameName.toUpperCase())) {
            redirectAttributes.addFlashAttribute("errorCreateGame", "A GAME WITH THE SAME NAME ALREADY EXISTS");
            return "redirect:/admin/modifyCatalog";
        }

        RestTemplate restTemplateCreate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Prepare the request body
        CreateGameRequest createGameRequest = new CreateGameRequest(newGameName, newGameDescription, newGamePublisher, newGamePrice);


        HttpEntity<CreateGameRequest> requestEntity = new HttpEntity<>(createGameRequest, headers);

        // Make the HTTP POST request
        String GAME_API_URL = catalogApiUrl+"/Game";
        ResponseEntity<Long> responseEntity = restTemplateCreate.postForEntity(GAME_API_URL, requestEntity, Long.class);


        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return "redirect:/admin/modifyCatalog";
        }else{
            return "redirect:/admin/dashboard";
        }

    }

    @PostMapping("/linkUnlinkAction")
    public String linkUnlinkAction(@RequestParam String action,
                                   @RequestParam Long linkGameId,
                                   @RequestParam Long linkCategoryId,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) {

        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }

        String url;
        if ("link".equals(action)) {
            url = catalogApiUrl+"/Game/link/" + linkGameId + "/category/" + linkCategoryId;
        } else if ("unlink".equals(action)) {
            url = catalogApiUrl+"/Game/unlink/" + linkGameId + "/category/" + linkCategoryId;
        } else {

            redirectAttributes.addFlashAttribute("errorAction", "Invalid action");
            return "redirect:/admin/modifyCatalog";
        }

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, null, String.class);


            if (responseEntity.getStatusCode() == HttpStatus.OK) {

                return "redirect:/admin/modifyCatalog";
            } else {

                redirectAttributes.addFlashAttribute("errorLink", "Failed to perform action");
                return "redirect:/admin/modifyCatalog";
            }
        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("errorLink", "Failed to perform action");
            return "redirect:/admin/modifyCatalog";
        }
    }

    @DeleteMapping("/deleteGame/{id}")
    public String deleteGame(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {

        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }


        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construct the URL for the catalog API
        ResponseEntity<String> nameResponse = restTemplate.exchange(
                catalogApiUrl+"/Game/imageReference/" + id,
                HttpMethod.GET,
                null,
                String.class
        );

        String gameReference = nameResponse.getBody();




        String deleteGameApiUrl = catalogApiUrl+"/Game/delete/" + id;

        // Create a request entity with headers
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(
                    deleteGameApiUrl,
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class
            );

            cartItemService.deleteCartItemsByGameId(id);

            if(!(gameReference==null)){
                deleteImageFile(gameReference);
            }
            return "redirect:/admin/modifyCatalog";
        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("errorDeleteGame", "FAILED TO DELETE THE GAME OR ALREADY DELETED");
            return "redirect:/admin/modifyCatalog";
        }
    }

    private void deleteImageFile(String gameReference) {
        try {
            // Construct the path for the image file
            Path imagePath = Paths.get(uploadDirectory, gameReference);

            Files.deleteIfExists(imagePath);

            // Log the successful deletion
            System.out.println("Image file deleted: " + gameReference);
        } catch (IOException e) {
            // Handle exceptions (e.g., file not found, permission issues)
            System.err.println("Error deleting image file: " + e.getMessage());
        }

    }

    @DeleteMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {

        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }


        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construct the URL for the catalog API
        String url = catalogApiUrl+"/category/delete/" + id;

        // Create a request entity with headers
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class
            );

            return "redirect:/admin/modifyCatalog";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorDeleteCategory", "FAILED TO DELETE THE CATEGORY: ALREADY DELETED OR CATEGORY STILL HAS GAMES");
            return "redirect:/admin/modifyCatalog";
        }
    }

    @PostMapping("/uploadImage")
    public String uploadImage(@RequestParam("imageFile") MultipartFile imageFile,
                                    @RequestParam("gameId") Long gameId,
                              @RequestParam("gameName") String gameName,
                              RedirectAttributes redirectAttributes, HttpSession session) {
        // Handle image upload and any necessary processing

        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }


        if (imageFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorUploadImage", "Error uploading the image.");
            return "redirect:/admin/modifyCatalog"; // Redirect to the page where the form is located
        }

        try {
            String sanitizedGameName = gameName.replaceAll("[^a-zA-Z0-9]", "_");
            String fileName = sanitizedGameName + "_" + gameId;

            String contentType = imageFile.getContentType();
            if (contentType != null && !contentType.startsWith("image")) {
                redirectAttributes.addFlashAttribute("errorUploadImage", "Invalid file type. Please select an image.");
                return "redirect:/admin/modifyCatalog";
            }

            String originalFileExtension = StringUtils.getFilenameExtension(imageFile.getOriginalFilename());
            if (StringUtils.hasText(originalFileExtension)) {
                fileName += "." + originalFileExtension;
            } else {
                fileName += ".png";
            }



            Path folderPath = Paths.get(uploadDirectory);
            if (Files.notExists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            BufferedImage originalImage = ImageIO.read(imageFile.getInputStream());
            BufferedImage resizedImage = Thumbnails.of(originalImage)
                    .size(200, 137) // Set the desired size
                    .asBufferedImage();

            Path destination = folderPath.resolve(fileName);

            ImageIO.write(resizedImage, originalFileExtension, destination.toFile());

            // Handle additional logic (e.g., save file path in the database, etc.)
            String reference = fileName;

            RestTemplate restTemplate = new RestTemplate();

            // Set up the request body and headers if needed
            // For simplicity, assuming the second API expects the reference and gameId as path variables
            String apiUrl = catalogApiUrl+"/Game/uploadImageReference/"+reference+"/"+gameId;
            restTemplate.postForEntity(apiUrl, null, String.class);
            redirectAttributes.addFlashAttribute("successUploadImage", "Image uploaded successfully.");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorUploadImage", "Error uploading the image.");
            return "redirect:/admin/modifyCatalog";
        }

        return "redirect:/admin/modifyCatalog"; // Redirect to the page where the form is located
    }



    @GetMapping("/manageInventory")
    public String manageInventory(Model model, HttpSession session) {
        // Fetch the list of games and categories from the external API
        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }

        String gamesApiUrl = catalogApiUrl+"/Game/";
        String gamesJsonResponse = restTemplate.getForObject(gamesApiUrl, String.class);
        List<Map<String, Object>> games = jsonMapper.parseJsonResponse(gamesJsonResponse);


        String codesApiUrl = catalogApiUrl+"/CodeGameItem";
        String codesJsonResponse = restTemplate.getForObject(codesApiUrl, String.class);
        List<Map<String, Object>> codes = jsonMapper.parseJsonResponse(codesJsonResponse);


        model.addAttribute("codes",codes);
        model.addAttribute("games", games);


        // Return the view name (HTML template) for modifying the catalog
        return "admin/manageInventory";
    }

    @PostMapping("/createCodeGameItem")
    public String createCodeGameItem(@RequestParam String newCode,
                             @RequestParam String newPlatform,
                             @RequestParam String newGameId,
                             RedirectAttributes redirectAttributes,
                                     HttpSession session) {

        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }

        // Your creation logic here
        RestTemplate restTemplatefind = new RestTemplate();
        String gamesJsonResponse = restTemplatefind.getForObject(catalogApiUrl+"/CodeGameItem/getCodeGameItemByCode/"+ newCode, String.class);


        if (gamesJsonResponse != null && gamesJsonResponse.contains(newCode)) {
            redirectAttributes.addFlashAttribute("errorCreateCodeGameItem", "A CODE WITH THE SAME VALUE ALREADY EXISTS");
            return "redirect:/admin/manageInventory";
        }

        RestTemplate restTemplateCreate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Prepare the request body
        CreateCodeGameItemRequest createCodeGameItemRequest = new CreateCodeGameItemRequest(Long.parseLong(newGameId), newCode,newPlatform);


        HttpEntity<CreateCodeGameItemRequest> requestEntity = new HttpEntity<>(createCodeGameItemRequest, headers);

        // Make the HTTP POST request
        String GAME_API_URL = catalogApiUrl+"/CodeGameItem";
        ResponseEntity<String> responseEntity = restTemplateCreate.postForEntity(GAME_API_URL, requestEntity, String.class);


        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return "redirect:/admin/manageInventory";
        }else{
            return "redirect:/admin/manageInventory";
        }

    }

    @DeleteMapping("/deleteCode")
    public String deleteCode(@RequestParam("codeId") String codeId, RedirectAttributes redirectAttributes, HttpSession session) {

        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }


        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construct the URL for the catalog API

        String url = catalogApiUrl+"/CodeGameItem/" + codeId;


        // Create a request entity with headers
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class
            );

            return "redirect:/admin/manageInventory";
        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("errorDeleteCategory", "FAILED TO DELETE THE CATEGORY OR ALREADY DELETED");
            return "redirect:/admin/manageInventory";
        }

    }


    @GetMapping("/manageOrders")
    public String manageOrders(Model model, HttpSession session) {
        // Fetch the list of games and categories from the external API
        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }

        return "admin/manageOrders";

    }

    @GetMapping("/getOrdersByEmail")
    public String getOrdersByEmail(@RequestParam(name = "userEmail") String userEmail,
                                   Model model,
                                   RedirectAttributes redirectAttributes, HttpSession session) {

        User adminUser = (User) session.getAttribute("admin");
        if (adminUser == null) {
            return "redirect:/home";
        }

        User user = userRepository.findByEmail(userEmail);
        if(user == null){
            redirectAttributes.addFlashAttribute("errorFindingUser", "USER NOT FOUND OR DOES NOT EXISTS");
            return "redirect:/admin/manageOrders";

        }

        List<OrderCart> orderCarts = orderCartRepository.findByUserId(user.getId());

        List<MergedOrderCartItem> mergedOrderCartItems = new ArrayList<>();

        for (OrderCart orderCart : orderCarts) {
            List<CartItem> cartItems = orderCart.getCartItems();
            LocalDateTime time = orderCart.getPurchaseDateTime();

            List<MergedCartItem> mergedCartItems = new ArrayList<>();
            for (CartItem cartItem : cartItems) {
                Map<String, Object> gameDetail = fetchGameDetails(cartItem.getGameId());
                mergedCartItems.add(new MergedCartItem(cartItem, gameDetail));
            }

            mergedOrderCartItems.add(new MergedOrderCartItem(orderCart, mergedCartItems, time));
        }

        mergedOrderCartItems.sort(Comparator.comparing(MergedOrderCartItem::getTime).reversed());
        model.addAttribute("mergedOrderCartItems", mergedOrderCartItems);


        return "admin/manageOrders";

    }



    ///AUX FUNCS

    private Map<String, Object> fetchGameDetails(Long gameId) {
        String apiUrl = catalogApiUrl+"/Game/getGameById/"+gameId;
        String jsonResponse = restTemplate.getForObject(apiUrl, String.class);
        return jsonMapper.parseJsonObjectResponse(jsonResponse);
    }




}





