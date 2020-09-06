package com.dolap.challenge.service;

import com.dolap.challenge.entity.Category;
import com.dolap.challenge.entity.Product;
import com.dolap.challenge.exception.CategoryNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
public class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    private Category rootCategory1;
    private Category rootCategory2;
    private Category child1Category;
    private Category child2Category;
    private Category child11Category;
    private Category child12Category;

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

        child11Category = new Category();
        child11Category.setName("Elbise");
        child11Category.setDescription("Kadin+giyim+elbise kategorisi");
        child11Category.setOrderNum(3);

        child12Category = new Category();
        child12Category.setName("Pantalon");
        child12Category.setDescription("Kadin+giyim+pantalon kategorisi");
        child12Category.setOrderNum(1);

        child2Category = new Category();
        child2Category.setName("Ayakkabi");
        child2Category.setDescription("Kadin+ayakkabi kategorisi");
        child2Category.setOrderNum(5);
    }

    @Test
    public void should_add_category_when_all_fields_are_valid(){
        rootCategory1.setOrderNum(5);
        Category addedCategory = categoryService.addCategory(rootCategory1);

        Assert.assertNotNull(addedCategory);
        Assert.assertEquals(addedCategory.getId(), rootCategory1.getId());
        Assert.assertEquals(addedCategory.getName(), rootCategory1.getName());
        Assert.assertEquals(addedCategory.getDescription(), rootCategory1.getDescription());
        Assert.assertEquals(addedCategory.getOrderNum(), rootCategory1.getOrderNum());
        Assert.assertNull(addedCategory.getParentCategory());
    }

    @Test
    public void should_add_category_with_valid_parent(){
        rootCategory1 = categoryService.addCategory(rootCategory1);
        child1Category.setParentCategory(rootCategory1);
        Category addedCategory = categoryService.addCategory(child1Category);

        Assert.assertNotNull(addedCategory);
        Assert.assertEquals(addedCategory.getId(), child1Category.getId());
        Assert.assertEquals(addedCategory.getName(), child1Category.getName());
        Assert.assertEquals(addedCategory.getDescription(), child1Category.getDescription());
        Assert.assertEquals(addedCategory.getOrderNum(), child1Category.getOrderNum());
        Assert.assertEquals(addedCategory.getParentCategory().getId(), rootCategory1.getId());
    }

    @Test
    public void should_throw_exception_when_adding_category_with_invalid_params() {
        rootCategory1.setName(null);
        Category addedCategory = null;
        Exception exception = null;
        try {
            addedCategory = categoryService.addCategory(rootCategory1);
        } catch (Exception ex) {
            exception = ex;
        }

        Assert.assertNull(addedCategory);
        Assert.assertNotNull(exception);
    }

    @Test
    public void should_update_category_with_valid_params() {
        Category addedCategory = categoryService.addCategory(rootCategory1);
        addedCategory.setName("Yet another new name");
        Category updatedCategory = categoryService.updateCategory(addedCategory.getId(), addedCategory);
        Category freshCategory  = categoryService.findCategory(addedCategory.getId());

        Assert.assertNotNull(addedCategory);
        Assert.assertNotNull(freshCategory);
        Assert.assertNotNull(updatedCategory);

        Assert.assertEquals(freshCategory.getId(), addedCategory.getId());
        Assert.assertEquals(freshCategory.getId(), updatedCategory.getId());
        Assert.assertEquals(freshCategory.getName(), addedCategory.getName());
        Assert.assertEquals(freshCategory.getName(), updatedCategory.getName());
    }

    @Test
    public void should_throw_exception_when_updating_with_invalid_params() {
        Category addedCategory = categoryService.addCategory(rootCategory1);
        addedCategory.setDescription(null);
        Category updatedCategory = null;
        Exception exception = null;

        try {
            updatedCategory = categoryService.updateCategory(addedCategory.getId(), addedCategory);
        } catch (Exception ex) {
            exception = ex;
        }

        Assert.assertNotNull(exception);
        Assert.assertNull(updatedCategory);
    }

    @Test
    public void should_move_category_between_different_parents() {
        Category parentCategory1 = categoryService.addCategory(rootCategory1);
        Category parentCategory2 = categoryService.addCategory(rootCategory2);

        child1Category.setParentCategory(parentCategory1);
        Category childCategory = categoryService.addCategory(child1Category);

        childCategory.setParentCategory(parentCategory2);
        categoryService.updateCategory(childCategory.getId(), childCategory);

        Category freshParentCategory1 = categoryService.findCategory(parentCategory1.getId());
        Category freshParentCategory2 = categoryService.findCategory(parentCategory2.getId());

        Assert.assertEquals(0, freshParentCategory1.getSubCategoryList().size());
        Assert.assertEquals(1, freshParentCategory2.getSubCategoryList().size());
        Assert.assertNotNull(childCategory.getParentCategory());
        Assert.assertEquals(parentCategory2.getId(), childCategory.getParentCategory().getId());
    }

    @Test
    public void should_throw_exception_when_trying_to_add_category_with_invalid_parent() {
        Category parentCategory1 = categoryService.addCategory(rootCategory1);

        parentCategory1.setId(Long.MAX_VALUE);
        child1Category.setParentCategory(parentCategory1);

        Exception exception = null;
        Category childCategory = null;
        try {
            childCategory = categoryService.addCategory(child1Category);
        } catch (Exception ex) {
            exception = ex;
        }

        Assert.assertNotNull(exception);
        Assert.assertNull(childCategory);
    }

    @Test
    public void should_delete_category_when_no_product_exists() {
        Category parentCategory1 = categoryService.addCategory(rootCategory1);
        categoryService.deleteCategory(parentCategory1.getId());

        Category freshCategory = null;
        Exception exception = null;

        try {
            freshCategory = categoryService.findCategory(parentCategory1.getId());
        } catch (Exception ex) {
            exception = ex;
        }

        Assert.assertNull(freshCategory);
        Assert.assertNotNull(exception);
    }

    @Test
    public void should_throw_ex_when_delete_category_when_product_exists() {
        Category parentCategory1 = categoryService.addCategory(rootCategory1);

        Product product = new Product();
        product.setName("Mavi Elbise");
        product.setDescription("Mavi renkte bir elbisedir");
        product.setRemainingStockCount(99);
        product.setPrice(new BigDecimal("9.99"));
        product.setCategory(parentCategory1);
        productService.addProduct(product);

        Exception exception = null;
        try {
            categoryService.deleteCategory(parentCategory1.getId());
        } catch (Exception ex) {
            exception = ex;
        }

        Assert.assertNotNull(exception);
        Assert.assertNotNull(categoryService.findCategory(parentCategory1.getId()));
    }

    @Test
    public void should_retrieve_product_when_valid_id_provided() {
        Category parentCategory1 = categoryService.addCategory(rootCategory1);
        Category freshCategory = categoryService.findCategory(parentCategory1.getId());

        Assert.assertNotNull(freshCategory);
        Assert.assertEquals(freshCategory.getId(), parentCategory1.getId());
        Assert.assertEquals(freshCategory.getName(), parentCategory1.getName());
        Assert.assertEquals(freshCategory.getDescription(), parentCategory1.getDescription());
    }

    @Test
    public void should_throw_exception_when_retrieving_with_invalid_id() {
        Category freshCategory = null;
        Exception exception = null;

        try {
            freshCategory = categoryService.findCategory(Long.MAX_VALUE);
        } catch (Exception ex) {
            exception = ex;
        }

        Assert.assertNotNull(exception);
        Assert.assertNull(freshCategory);
        Assert.assertTrue(exception instanceof CategoryNotFoundException);
    }

    @Test
    public void should_retrieve_list_of_categories() {
        Category parentCategory1 = categoryService.addCategory(rootCategory1);
        Category parentCategory2 = categoryService.addCategory(rootCategory2);

        child1Category.setParentCategory(parentCategory1);
        child2Category.setParentCategory(parentCategory2);

        Category childCategory1 = categoryService.addCategory(child1Category);
        Category childCategory2 = categoryService.addCategory(child2Category);

        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(parentCategory1.getId());
        categoryIds.add(parentCategory2.getId());
        categoryIds.add(childCategory1.getId());
        categoryIds.add(childCategory2.getId());

        List<Category> allCategories = categoryService.getAll(false);
        Assert.assertNotNull(allCategories);
        categoryIds.stream().forEach(categoryId -> {
            Assert.assertTrue(allCategories.stream().anyMatch(category -> category.getId().compareTo(categoryId) == 0));
        });
    }

    @Test
    public void should_only_retrieve_the_top_level_categories() {
        Category parentCategory1 = categoryService.addCategory(rootCategory1);
        Category parentCategory2 = categoryService.addCategory(rootCategory2);

        child1Category.setParentCategory(parentCategory1);
        child2Category.setParentCategory(parentCategory2);

        Category childCategory1 = categoryService.addCategory(child1Category);
        Category childCategory2 = categoryService.addCategory(child2Category);

        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(childCategory1.getId());
        categoryIds.add(childCategory2.getId());

        List<Category> allCategories = categoryService.getAll(true);
        Assert.assertNotNull(allCategories);
        allCategories.stream().forEach(category -> {
            Assert.assertFalse(categoryIds.contains(category.getId()));
            Assert.assertNull(category.getParentCategory());
        });
    }
}
