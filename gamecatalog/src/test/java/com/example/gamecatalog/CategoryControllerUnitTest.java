package com.example.gamecatalog;

import com.example.gamecatalog.entities.Category;
import com.example.gamecatalog.repositories.CategoryRepository;
import com.example.gamecatalog.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

 class CategoryControllerUnitTest {

    @Mock
    CategoryRepository categoryRepository;


    @BeforeEach
    public void setUp(){
        MockitoAnnotations.initMocks(this);

        Category Action = Category.builder().id(1L).name("Action")
                .description("Action based games").build();
        Category Adventure = Category.builder().id(2L).name("Adventure")
                .description("Adventure based games").build();

        List<Category> categories = Arrays.asList(Action,Adventure);

        when(categoryRepository.findAllCategories()).thenReturn(categories);
    }

    @Test
    public void allCategories(){

        List<Category> categories = categoryRepository.findAllCategories();

        assertEquals(categories.get(0).getName(), "Action");
        assertEquals(categories.get(1).getName(), "Adventure");

        System.out.println("Se han encontrado todas las categorias");
    }


}
