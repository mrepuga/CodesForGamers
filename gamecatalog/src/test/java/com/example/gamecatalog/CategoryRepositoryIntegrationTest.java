package com.example.gamecatalog;

import com.example.gamecatalog.entities.Category;
import com.example.gamecatalog.repositories.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CategoryRepositoryIntegrationTest {


    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findCategoryById()  {
        Category category = Category.builder().id(1L).name("Action").description("Action based games").build();
        System.out.println(category.getId() + ": " + category.getName() + "" + category.getDescription());
        entityManager.merge(category);

        Category found = categoryRepository.findById(category.getId()).get();
        assertThat(found.getName()).isEqualTo(category.getName());
    }
}
