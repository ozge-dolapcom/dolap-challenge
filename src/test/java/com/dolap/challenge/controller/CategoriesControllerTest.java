package com.dolap.challenge.controller;

import com.dolap.challenge.entity.Category;
import com.dolap.challenge.entity.Product;
import com.dolap.challenge.service.CategoryService;
import com.dolap.challenge.service.ProductService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAsync
public class CategoriesControllerTest {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    private Category rootCategory1;
    private Category rootCategory2;
    private Category child1Category;
    private Category child2Category;

    @Before
    public void setup() {
        rootCategory1 = new Category();
        rootCategory1.setName("Kadin");
        rootCategory1.setDescription("Kadin kategorisi");
        rootCategory1.setOrderNum(0);

        rootCategory2 = new Category();
        rootCategory2.setName("Erkek");
        rootCategory2.setDescription("Erkek kategorisi");
        rootCategory2.setOrderNum(1);

        child1Category = new Category();
        child1Category.setName("Giyim");
        child1Category.setDescription("Kadin+giyim kategorisi");
        child1Category.setOrderNum(2);

        child2Category = new Category();
        child2Category.setName("Giyim");
        child2Category.setDescription("Erkek+giyim kategorisi");
        child2Category.setOrderNum(2);
    }

    @Test
    public void should_add_category_with_valid_params_given() {
        CategoriesController controller = new CategoriesController(categoryService);
        Category responseCategory = controller.addCategory(rootCategory1);

        Assert.assertNotNull(responseCategory);
        Assert.assertNotNull(responseCategory.getId());
        Assert.assertEquals(responseCategory.getName(), rootCategory1.getName());
        Assert.assertEquals(responseCategory.getDescription(), rootCategory1.getDescription());
        Assert.assertEquals(responseCategory.getOrderNum(), rootCategory1.getOrderNum());
    }

    @Test
    public void should_update_category_with_valid_params_given() {
        CategoriesController controller = new CategoriesController(categoryService);
        Category responseCategory = controller.addCategory(rootCategory1);

        responseCategory.setName("Yeet another name goes here");
        Category updatedCategoryResponse = controller.updateCategory(responseCategory.getId(), responseCategory);

        Assert.assertNotNull(updatedCategoryResponse);
        Assert.assertEquals(updatedCategoryResponse.getId(), responseCategory.getId());
        Assert.assertEquals(updatedCategoryResponse.getName(), responseCategory.getName());
    }

    @Test
    public void should_throw_exception_when_trying_to_add_with_invalid_params() {
        CategoriesController controller = new CategoriesController(categoryService);
        rootCategory1.setDescription("   ");

        Category responseCategory = null;
        Exception exception = null;
        try {
            responseCategory = controller.addCategory(rootCategory1);
        } catch (Exception ex) {
            exception = ex;
        }

        Assert.assertNull(responseCategory);
        Assert.assertNotNull(exception);
    }

    @Test
    public void should_throw_exception_when_updating_with_invalid_params() {
        CategoriesController controller = new CategoriesController(categoryService);
        Category addedCategoryResponse = controller.addCategory(rootCategory1);

        addedCategoryResponse.setName(null);

        Category responseCategory = null;
        Exception exception = null;
        try {
            responseCategory = controller.updateCategory(addedCategoryResponse.getId(), addedCategoryResponse);
        } catch (Exception ex) {
            exception = ex;
        }

        Assert.assertNull(responseCategory);
        Assert.assertNotNull(exception);
    }

    @Test
    public void should_set_parent_when_category_is_valid() {
        CategoriesController controller = new CategoriesController(categoryService);

        Category parentCategoryResponse = controller.addCategory(rootCategory1);
        child1Category.setParentCategory(parentCategoryResponse);

        Category childCategoryResponse = controller.addCategory(child1Category);
        Category freshParentResponse = controller.findCategory(parentCategoryResponse.getId());

        Assert.assertNotNull(childCategoryResponse);
        Assert.assertNotNull(childCategoryResponse.getParentCategory());
        Assert.assertEquals(childCategoryResponse.getParentCategory().getId(), parentCategoryResponse.getId());

        Assert.assertNotNull(freshParentResponse);
        Assert.assertEquals(1, freshParentResponse.getSubCategoryList().size());
    }

    @Test
    public void should_throw_exception_when_setting_invalid_parent() {
        CategoriesController controller = new CategoriesController(categoryService);

        Category parentCategoryResponse = controller.addCategory(rootCategory1);
        parentCategoryResponse.setId(Long.MAX_VALUE);

        child1Category.setParentCategory(parentCategoryResponse);

        Category childCategoryResponse = null;
        Exception exception = null;
        try {
            controller.addCategory(child1Category);
        } catch (Exception ex) {
            exception = ex;
        }

        Assert.assertNotNull(exception);
        Assert.assertNull(childCategoryResponse);
    }

    @Test
    public void should_delete_category_when_no_product_exists() {
        CategoriesController controller = new CategoriesController(categoryService);
        Category parentCategoryResponse = controller.addCategory(rootCategory1);

        controller.deleteCategory(parentCategoryResponse.getId());
        Exception exception = null;
        Category freshCategory = null;
        try {
            freshCategory = controller.findCategory(parentCategoryResponse.getId());
        } catch (Exception ex) {
            exception = ex;
        }

        Assert.assertNull(freshCategory);
        Assert.assertNotNull(exception);
    }

    @Test
    public void should_throw_exception_when_there_is_at_least_one_product_under_category() {
        CategoriesController controller = new CategoriesController(categoryService);
        Category categoryResponse = controller.addCategory(rootCategory1);

        Product product = new Product();
        product.setName("Camera");
        product.setDescription("The one you take photos with");
        product.setPrice(new BigDecimal(99.99));
        product.setRemainingStockCount(100);
        product.setCategory(categoryResponse);

        productService.addProduct(product);

        Exception exception = null;
        try {
            controller.deleteCategory(categoryResponse.getId());
        } catch (Exception ex) {
            exception = ex;
        }

        Category freshCategoryResponse = controller.findCategory(categoryResponse.getId());

        Assert.assertNotNull(exception);
        Assert.assertNotNull(freshCategoryResponse);
        Assert.assertEquals(freshCategoryResponse.getId(), categoryResponse.getId());
    }

    @Test
    public void should_throw_exception_when_deleting_with_invalid_id() {
        CategoriesController controller = new CategoriesController(categoryService);
        Exception exception = null;
        try {
            controller.deleteCategory(Long.MAX_VALUE);
        } catch (Exception ex) {
            exception = ex;
        }

        Assert.assertNotNull(exception);
    }

    @Test
    public void should_retrieve_category_with_valid_id() {
        CategoriesController controller = new CategoriesController(categoryService);
        Category responseCategory = controller.addCategory(rootCategory1);

        Category freshCategory = controller.findCategory(responseCategory.getId());

        Assert.assertNotNull(freshCategory);
        Assert.assertNotNull(responseCategory);
        Assert.assertEquals(responseCategory.getId(), freshCategory.getId());
        Assert.assertEquals(responseCategory.getName(), freshCategory.getName());
        Assert.assertEquals(responseCategory.getDescription(), freshCategory.getDescription());
    }

    public void should_throw_exception_when_retrieving_with_invalid_id() {
        CategoriesController controller = new CategoriesController(categoryService);
        Exception exception = null;
        Category freshCategory = null;
        try {
            freshCategory = controller.findCategory(Long.MAX_VALUE);
        } catch (Exception ex) {
            exception = ex;
        }

        Assert.assertNotNull(exception);
        Assert.assertNull(freshCategory);
    }

    @Test
    public void should_retrieve_all_categories() {
        CategoriesController controller = new CategoriesController(categoryService);
        Category parentCategory1 = controller.addCategory(rootCategory1);
        Category parentCategory2 = controller.addCategory(rootCategory2);

        child1Category.setParentCategory(parentCategory1);
        child2Category.setParentCategory(parentCategory2);

        Category childCategory1 = controller.addCategory(child1Category);
        Category childCategory2 = controller.addCategory(child2Category);

        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(parentCategory1.getId());
        categoryIds.add(parentCategory2.getId());
        categoryIds.add(childCategory1.getId());
        categoryIds.add(childCategory2.getId());

        List<Category> allCategories = controller.getAllCategories(false);
        Assert.assertNotNull(allCategories);
        categoryIds.stream().forEach(categoryId -> {
            Assert.assertTrue(allCategories.stream().anyMatch(category -> category.getId().compareTo(categoryId) == 0));
        });
    }

    @Test
    public void should_only_retrieve_top_level_categories() {
        CategoriesController controller = new CategoriesController(categoryService);
        Category parentCategory1 = controller.addCategory(rootCategory1);
        Category parentCategory2 = controller.addCategory(rootCategory2);

        child1Category.setParentCategory(parentCategory1);
        child2Category.setParentCategory(parentCategory2);

        Category childCategory1 = controller.addCategory(child1Category);
        Category childCategory2 = controller.addCategory(child2Category);

        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(childCategory1.getId());
        categoryIds.add(childCategory2.getId());

        List<Category> allCategories = controller.getAllCategories(true);
        Assert.assertNotNull(allCategories);
        allCategories.stream().forEach(category -> {
            Assert.assertFalse(categoryIds.contains(category.getId()));
            Assert.assertNull(category.getParentCategory());
        });
    }
}
