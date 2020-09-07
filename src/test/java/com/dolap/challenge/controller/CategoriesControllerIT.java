package com.dolap.challenge.controller;

import com.dolap.challenge.configuration.Messages;
import com.dolap.challenge.entity.Category;
import com.dolap.challenge.entity.Product;
import com.dolap.challenge.entity.User;
import com.dolap.challenge.exception.CategoryNotFoundException;
import com.dolap.challenge.service.CategoryService;
import com.dolap.challenge.service.ProductService;
import com.dolap.challenge.service.UserService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CategoriesControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Messages messages;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    private static String jwtToken;
    private static final String testUserName = "testuser";

    private Category rootCategory1;
    private Category rootCategory2;
    private Category child1Category;
    private Category child2Category;

    private JsonObject rootCategory1Json;
    private JsonObject rootCategory2Json;
    private JsonObject child1CategoryJson;
    private JsonObject child2CategoryJson;

    @Before
    public void setup() throws Exception {
        rootCategory1 = new Category();
        rootCategory1.setName("Kadin");
        rootCategory1.setDescription("Kadin kategorisi");
        rootCategory1.setOrderNum(0);
        rootCategory1Json = new JsonObject();
        rootCategory1Json.addProperty("name", rootCategory1.getName());
        rootCategory1Json.addProperty("description", rootCategory1.getDescription());
        rootCategory1Json.addProperty("orderNum", rootCategory1.getOrderNum());

        rootCategory2 = new Category();
        rootCategory2.setName("Erkek");
        rootCategory2.setDescription("Erkek kategorisi");
        rootCategory2.setOrderNum(1);
        rootCategory2Json = new JsonObject();
        rootCategory2Json.addProperty("name", rootCategory2.getName());
        rootCategory2Json.addProperty("description", rootCategory2.getDescription());
        rootCategory2Json.addProperty("orderNum", rootCategory2.getOrderNum());

        child1Category = new Category();
        child1Category.setName("Giyim");
        child1Category.setDescription("Kadin+giyim kategorisi");
        child1Category.setOrderNum(2);
        child1CategoryJson = new JsonObject();
        child1CategoryJson.addProperty("name", child1Category.getName());
        child1CategoryJson.addProperty("description", child1Category.getDescription());
        child1CategoryJson.addProperty("orderNum", child1Category.getOrderNum());

        child2Category = new Category();
        child2Category.setName("Giyim");
        child2Category.setDescription("Erkek+giyim kategorisi");
        child2Category.setOrderNum(2);
        child2CategoryJson = new JsonObject();
        child2CategoryJson.addProperty("name", child2Category.getName());
        child2CategoryJson.addProperty("description", child2Category.getDescription());
        child2CategoryJson.addProperty("orderNum", child2Category.getOrderNum());

        if (jwtToken == null) {
            jwtToken = getJwtToken();
        }
        userService.updateRole(testUserName, User.ROLE_ADMIN);
    }

    private String getJwtToken() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/auth/register");
        JsonObject userJson = new JsonObject();
        userJson.addProperty("username", testUserName);
        userJson.addProperty("password", "test");
        request.content(userJson.toString());
        request.contentType("application/json");

        MvcResult mvcResult = mvc.perform(request).andReturn();
        JsonObject responseJson = new JsonParser().parse(mvcResult.getResponse().getContentAsString()).getAsJsonObject();
        return responseJson.get("token").getAsString();
    }

    @Test
    public void should_add_category_with_valid_params_given() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/categories");
        request.content(rootCategory1Json.toString());
        request.contentType("application/json");
        request.header("Authorization", "Bearer " + this.jwtToken);

        MvcResult mvcResult = mvc.perform(request).andReturn();
        JsonObject responseJson = new JsonParser().parse(mvcResult.getResponse().getContentAsString()).getAsJsonObject();

        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        Assert.assertNotNull(responseJson);
        Assert.assertEquals(rootCategory1Json.get("name").getAsString(), responseJson.get("name").getAsString());
        Assert.assertEquals(rootCategory1Json.get("description").getAsString(), responseJson.get("description").getAsString());
        Assert.assertNotNull(responseJson.get("id").getAsLong());
    }

    @Test
    public void should_throw_exception_when_trying_to_add_with_invalid_params() throws Exception {
        // add invalid category
        JsonObject parentCategoryJson = new JsonObject();
        parentCategoryJson.addProperty("id", Long.MAX_VALUE);
        rootCategory1Json.add("parentCategory", parentCategoryJson);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/categories");
        request.content(rootCategory1Json.toString());
        request.contentType("application/json");
        request.header("Authorization", "Bearer " + this.jwtToken);

        MvcResult mvcResult = mvc.perform(request).andReturn();
        JsonObject responseJson = new JsonParser().parse(mvcResult.getResponse().getContentAsString()).getAsJsonObject();

        Assert.assertNotEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        Assert.assertNotNull(responseJson);
    }

    @Test
    public void should_throw_ex_when_add_category_without_admin_role() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/categories");
        request.content(rootCategory1Json.toString());
        request.contentType("application/json");
        request.header("Authorization", "Bearer " + this.jwtToken);
        userService.updateRole(testUserName, User.ROLE_USER);

        MvcResult mvcResult = mvc.perform(request).andReturn();
        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void should_throw_ex_when_add_category_without_token() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/categories");
        request.content(rootCategory1Json.toString());
        request.contentType("application/json");

        MvcResult mvcResult = mvc.perform(request).andReturn();
        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void should_update_category_with_valid_params_given() throws Exception {
        Category addedCategory = categoryService.addCategory(rootCategory1);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/categories/" + addedCategory.getId());
        rootCategory1Json.remove("name");
        String updatedName = "Update category name";
        rootCategory1Json.addProperty("name", updatedName);
        request.content(rootCategory1Json.toString());
        request.contentType("application/json");
        request.header("Authorization", "Bearer " + this.jwtToken);

        MvcResult mvcResult = mvc.perform(request).andReturn();
        JsonObject responseJson = new JsonParser().parse(mvcResult.getResponse().getContentAsString()).getAsJsonObject();

        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        Assert.assertNotNull(responseJson);
        Assert.assertEquals(updatedName, responseJson.get("name").getAsString());
    }

    @Test
    public void should_throw_exception_when_updating_with_invalid_params() throws Exception {
        Category addedCategory = categoryService.addCategory(rootCategory1);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/categories/" + addedCategory.getId());
        rootCategory1Json.remove("orderNum"); // intentionally removed
        rootCategory1Json.addProperty("orderNum", -1);
        request.content(rootCategory1Json.toString());
        request.contentType("application/json");
        String localeValue = "tr";
        request.header("Accept-Language", localeValue);
        request.header("Authorization", "Bearer " + this.jwtToken);

        MvcResult mvcResult = mvc.perform(request).andReturn();
        JsonObject responseJson = new JsonParser().parse(mvcResult.getResponse().getContentAsString()).getAsJsonObject();

        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
        Assert.assertNotNull(responseJson);
        Assert.assertEquals(messages.get("com.dolap.challenge.entity.Category.orderNum.validation.minMessage", java.util.Locale.forLanguageTag(localeValue)), responseJson.get("message").getAsString());

    }

    @Test
    public void should_throw_ex_when_update_category_without_admin_role() throws Exception {
        Category addedCategory = categoryService.addCategory(rootCategory1);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/categories/" + addedCategory.getId());
        request.content(rootCategory1Json.toString());
        request.contentType("application/json");
        request.header("Authorization", "Bearer " + this.jwtToken);
        userService.updateRole(testUserName, User.ROLE_USER);

        MvcResult mvcResult = mvc.perform(request).andReturn();
        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void should_throw_ex_when_update_category_without_token() throws Exception {
        Category addedCategory = categoryService.addCategory(rootCategory1);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/categories/" + addedCategory.getId());
        request.content(rootCategory1Json.toString());
        request.contentType("application/json");

        MvcResult mvcResult = mvc.perform(request).andReturn();
        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void should_find_category_without_token() throws Exception {
        Category addedCategory = categoryService.addCategory(rootCategory1);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/categories/" + addedCategory.getId());
        request.contentType("application/json");
        userService.updateRole(testUserName, User.ROLE_USER);

        MvcResult mvcResult = mvc.perform(request).andReturn();
        JsonObject responseJson = new JsonParser().parse(mvcResult.getResponse().getContentAsString()).getAsJsonObject();

        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        Assert.assertEquals(addedCategory.getId().longValue(), responseJson.get("id").getAsLong());
    }

    @Test
    public void should_throw_exception_when_retrieving_with_invalid_id() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/categories/" + Long.valueOf(99999));
        request.contentType("application/json");
        String localeValue = "tr";
        request.header("Accept-Language", localeValue);

        MvcResult mvcResult = mvc.perform(request).andReturn();
        JsonObject responseJson = new JsonParser().parse(mvcResult.getResponse().getContentAsString()).getAsJsonObject();

        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
        Assert.assertNotNull(responseJson);
        Assert.assertEquals(messages.get(CategoryNotFoundException.CATEGORY_NOT_FOUND_EXCEPTION_MESSAGE_KEY, java.util.Locale.forLanguageTag(localeValue)), responseJson.get("message").getAsString());
    }

    @Test
    public void should_delete_category_when_no_product_exists() throws Exception {
        Category addedCategory = categoryService.addCategory(rootCategory1);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/categories/" + addedCategory.getId());
        request.contentType("application/json");
        request.header("Authorization", "Bearer " + this.jwtToken);

        MvcResult mvcResult = mvc.perform(request).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());

        Exception exception = null;
        Category foundCategory = null;
        try {
            foundCategory = categoryService.findCategory(addedCategory.getId());
        } catch (Exception e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertNull(foundCategory);
        Assert.assertTrue(exception instanceof CategoryNotFoundException);
    }

    @Test
    public void should_throw_exception_when_there_is_at_least_one_product_under_category() throws Exception {
        Category addedCategory = categoryService.addCategory(rootCategory1);
        Product product = new Product();
        product.setName("Camera");
        product.setDescription("The one you take photos with");
        product.setPrice(new BigDecimal(99.99));
        product.setRemainingStockCount(100);
        product.setCategory(addedCategory);
        productService.addProduct(product);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/categories/" + addedCategory.getId());
        request.contentType("application/json");
        request.header("Authorization", "Bearer " + this.jwtToken);

        MvcResult mvcResult = mvc.perform(request).andReturn();
        Assert.assertNotEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());

        Exception exception = null;
        Category foundCategory = null;
        try {
            foundCategory = categoryService.findCategory(addedCategory.getId());
        } catch (Exception e) {
            exception = e;
        }

        Assert.assertNull(exception);
        Assert.assertNotNull(foundCategory);
        Assert.assertEquals(addedCategory.getId(), foundCategory.getId());
    }

    @Test
    public void should_throw_exception_when_deleting_with_invalid_id() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/categories/" + Long.valueOf(9999999));
        request.contentType("application/json");
        request.header("Authorization", "Bearer " + this.jwtToken);

        MvcResult mvcResult = mvc.perform(request).andReturn();
        Assert.assertNotEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void should_throw_ex_when_delete_without_admin_role() throws Exception {
        Category addedCategory = categoryService.addCategory(rootCategory1);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/categories/" + addedCategory.getId());
        request.contentType("application/json");
        request.header("Authorization", "Bearer " + this.jwtToken);
        userService.updateRole(testUserName, User.ROLE_USER);

        MvcResult mvcResult = mvc.perform(request).andReturn();
        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void should_throw_ex_when_delete_without_token() throws Exception {
        Category addedCategory = categoryService.addCategory(rootCategory1);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/categories/" + addedCategory.getId());
        request.contentType("application/json");

        MvcResult mvcResult = mvc.perform(request).andReturn();
        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void should_retrieve_all_categories() throws Exception {
        Category parentCategory1 = categoryService.addCategory(rootCategory1);
        Category parentCategory2 = categoryService.addCategory(rootCategory2);
        child1Category.setParentCategory(parentCategory1);
        Category childCategory1 = categoryService.addCategory(child1Category);
        child2Category.setParentCategory(parentCategory2);
        Category childCategory2 = categoryService.addCategory(child2Category);

        List<Long> childCategoryIds = new ArrayList<>();
        childCategoryIds.add(childCategory1.getId());
        childCategoryIds.add(childCategory2.getId());

        // skipChildren is true (default)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/categories/");
        request.contentType("application/json");

        MvcResult mvcResult = mvc.perform(request).andReturn();
        JsonArray responseJsonArray = new JsonParser().parse(mvcResult.getResponse().getContentAsString()).getAsJsonArray();

        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        Assert.assertNotNull(responseJsonArray);
        responseJsonArray.forEach(jsonObj -> {
            Assert.assertFalse(childCategoryIds.contains(jsonObj.getAsJsonObject().get("id").getAsLong()));
        });
    }

    @Test
    public void should_only_retrieve_top_level_categories() throws Exception {
        Category parentCategory1 = categoryService.addCategory(rootCategory1);
        Category parentCategory2 = categoryService.addCategory(rootCategory2);
        child1Category.setParentCategory(parentCategory1);
        Category childCategory1 = categoryService.addCategory(child1Category);
        child2Category.setParentCategory(parentCategory2);
        Category childCategory2 = categoryService.addCategory(child2Category);

        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(childCategory1.getId());
        categoryIds.add(childCategory2.getId());
        categoryIds.add(parentCategory1.getId());
        categoryIds.add(parentCategory2.getId());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/categories/");
        request.param("skipChildren", "false");
        request.contentType("application/json");

        MvcResult mvcResult = mvc.perform(request).andReturn();
        JsonArray responseJsonArray = new JsonParser().parse(mvcResult.getResponse().getContentAsString()).getAsJsonArray();

        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        Assert.assertNotNull(responseJsonArray);
        categoryIds.stream().forEach(categoryId -> {
            boolean found = false;
            for(JsonElement jsonObj : responseJsonArray){
                if(categoryId.compareTo(jsonObj.getAsJsonObject().get("id").getAsLong()) == 0){
                    found = true;
                    break;
                }
            }
            Assert.assertTrue(found);
        });
    }
}
