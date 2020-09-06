package com.dolap.challenge.controller;

import com.dolap.challenge.configuration.Messages;
import com.dolap.challenge.entity.Category;
import com.dolap.challenge.entity.Product;
import com.dolap.challenge.entity.User;
import com.dolap.challenge.exception.CategoryNotFoundException;
import com.dolap.challenge.exception.ProductNotFoundException;
import com.dolap.challenge.service.CategoryService;
import com.dolap.challenge.service.ProductService;
import com.dolap.challenge.service.UserService;
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

@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ProductsControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Messages messages;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    private JsonObject productJson;
    private Product product;

    private Category rootCategory;
    private Category child1Category;
    private Category child2Category;
    private Category child11Category;
    private Category child12Category;

    private static String jwtToken;

    private static final String testUserName = "testuser";

    @Before
    public void setup() throws Exception {
        setupCategoryTree();

        product = new Product();
        product.setName("Mavi Elbise");
        product.setDescription("Mavi renkte bir elbisedir");
        product.setRemainingStockCount(99);
        product.setPrice(new BigDecimal("9.99"));
        product.setCategory(child11Category);

        productJson = new JsonObject();
        productJson.addProperty("name", product.getName());
        productJson.addProperty("description", product.getDescription());
        productJson.addProperty("remainingStockCount", product.getRemainingStockCount());
        productJson.addProperty("price", product.getPrice());

        JsonObject categoryJson = new JsonObject();
        categoryJson.addProperty("id", product.getCategory().getId());
        productJson.add("category", categoryJson);

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

    private void setupCategoryTree() {
        rootCategory = new Category();
        rootCategory.setName("Kadin");
        rootCategory.setDescription("Kadin kategorisi");
        rootCategory.setOrderNum(0);
        rootCategory = categoryService.addCategory(rootCategory);

        child1Category = new Category();
        child1Category.setName("Giyim");
        child1Category.setDescription("Kadin+giyim kategorisi");
        child1Category.setOrderNum(0);
        child1Category.setParentCategory(rootCategory);
        categoryService.addCategory(child1Category);

        child11Category = new Category();
        child11Category.setName("Elbise");
        child11Category.setDescription("Kadin+giyim+elbise kategorisi");
        child11Category.setOrderNum(0);
        child11Category.setParentCategory(child1Category);
        categoryService.addCategory(child11Category);

        child12Category = new Category();
        child12Category.setName("Pantalon");
        child12Category.setDescription("Kadin+giyim+pantalon kategorisi");
        child12Category.setOrderNum(1);
        child12Category.setParentCategory(child1Category);
        categoryService.addCategory(child12Category);

        child2Category = new Category();
        child2Category.setName("Ayakkabi");
        child2Category.setDescription("Kadin+ayakkabi kategorisi");
        child2Category.setOrderNum(0);
        child2Category.setParentCategory(rootCategory);
        categoryService.addCategory(child2Category);
    }

    private void addProducts() {
        int productSize = 30;
        for(int i = 1; i <= productSize; i++) {
            Product product = new Product();
            product.setName("Test product name " + i);
            product.setDescription("Test product desc " + i);
            product.setRemainingStockCount(i);
            product.setPrice(new BigDecimal(i * 10));

            if(i > 0 && i <= 10){
                product.setCategory(child1Category);
            } else if(i > 10 && i <= 20){
                product.setCategory(child2Category);
            } else {
                product.setCategory(child12Category);
            }

            productService.addProduct(product);
            // ignore result
        }
    }

    @Test
    public void should_add_product_with_valid_params_given() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/products");
        request.content(productJson.toString());
        request.contentType("application/json");
        request.header("Authorization", "Bearer " + this.jwtToken);

        MvcResult mvcResult = mvc.perform(request).andReturn();
        JsonObject responseJson = new JsonParser().parse(mvcResult.getResponse().getContentAsString()).getAsJsonObject();

        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        Assert.assertNotNull(responseJson);
        Assert.assertEquals(productJson.get("name").getAsString(), responseJson.get("name").getAsString());
        Assert.assertEquals(productJson.get("category").getAsJsonObject().get("id"), responseJson.get("category").getAsJsonObject().get("id"));
        Assert.assertNotNull(responseJson.get("id").getAsLong());
    }

    @Test
    public void should_throw_ex_when_add_product_with_invalid_params_given() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/products");
        productJson.remove("name");
        request.content(productJson.toString());
        request.contentType("application/json");
        request.header("Authorization", "Bearer " + this.jwtToken);

        String localeValue = "tr";
        request.header("Accept-Language", localeValue);

        MvcResult mvcResult = mvc.perform(request).andReturn();
        JsonObject responseJson = new JsonParser().parse(mvcResult.getResponse().getContentAsString()).getAsJsonObject();

        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
        Assert.assertNotNull(responseJson);
        Assert.assertEquals(messages.get("com.dolap.challenge.entity.Product.name.validation.notBlankMessage", java.util.Locale.forLanguageTag(localeValue)), responseJson.get("message").getAsString());
    }

    @Test
    public void should_throw_ex_when_add_product_with_invalid_category_given() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/products");

        productJson.remove("category");
        JsonObject categoryJson = new JsonObject();
        categoryJson.addProperty("id", Long.valueOf(99999999));
        productJson.add("category", categoryJson);

        request.content(productJson.toString());
        request.contentType("application/json");
        request.header("Authorization", "Bearer " + this.jwtToken);

        String localeValue = "tr";
        request.header("Accept-Language", localeValue);

        MvcResult mvcResult = mvc.perform(request).andReturn();
        JsonObject responseJson = new JsonParser().parse(mvcResult.getResponse().getContentAsString()).getAsJsonObject();

        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
        Assert.assertNotNull(responseJson);
        Assert.assertEquals(messages.get(CategoryNotFoundException.CATEGORY_NOT_FOUND_EXCEPTION_MESSAGE_KEY, java.util.Locale.forLanguageTag(localeValue)), responseJson.get("message").getAsString());
    }

    @Test
    public void should_get_products_child1Category() throws Exception {
        addProducts();

        Integer limit = 10;
        Integer page = 0;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/products");
        request.param("categoryId", child1Category.getId().toString());
        request.param("sortBy", "id");
        request.param("sortOrder", "asc");
        request.param("page", page.toString());
        request.param("limit", limit.toString());
        request.contentType("application/json");

        MvcResult mvcResult = mvc.perform(request).andReturn();
        JsonObject responseJson = new JsonParser().parse(mvcResult.getResponse().getContentAsString()).getAsJsonObject();

        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        Assert.assertNotNull(responseJson);
        Assert.assertEquals(limit.intValue(), responseJson.get("content").getAsJsonArray().size());
        Assert.assertFalse(responseJson.get("last").getAsBoolean());
        Assert.assertEquals(Integer.valueOf(20), Integer.valueOf(responseJson.get("totalElements").getAsInt()));

        long firstId = responseJson.get("content").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsLong();
        long lastId = responseJson.get("content").getAsJsonArray().get(limit.intValue() - 1).getAsJsonObject().get("id").getAsLong();
        Assert.assertTrue(firstId < lastId); // check sort asc
    }

    @Test
    public void should_throw_ex_get_products_empty_category() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/products");
        request.param("categoryId", String.valueOf(99999999)); // invalid id
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
    public void should_delete_product_when_id_provided() throws Exception {
        Product addedProduct = productService.addProduct(product);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/products/" + addedProduct.getId());
        request.contentType("application/json");
        request.header("Authorization", "Bearer " + this.jwtToken);

        MvcResult mvcResult = mvc.perform(request).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());

        Exception exception = null;
        Product foundProduct = null;
        try {
            foundProduct = productService.findProduct(addedProduct.getId());
        } catch (Exception e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertNull(foundProduct);
        Assert.assertTrue(exception instanceof ProductNotFoundException);
    }
}
