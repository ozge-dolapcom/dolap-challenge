package com.dolap.challenge.controller;

import com.dolap.challenge.configuration.Messages;
import com.dolap.challenge.entity.Product;
import com.dolap.challenge.exception.ProductNotFoundException;
import com.dolap.challenge.service.ProductService;
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

    private JsonObject productJson;
    private Product product;

    @Before
    public void setup() {
        productJson = new JsonObject();
        productJson.addProperty("name", "The great product");
        productJson.addProperty("description", "This product will help me get thru the interview process :fingers crossed:");
        productJson.addProperty("remainingStockCount", 99);
        productJson.addProperty("price", new BigDecimal("9.99"));

        product = new Product();
        product.setName("The greatest product");
        product.setDescription("Some random product description goes here");
        product.setRemainingStockCount(102);
        product.setPrice(new BigDecimal("45.98"));
    }

    @Test
    public void should_add_product_with_valid_params_given() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/products");
        request.content(productJson.toString());
        request.contentType("application/json");

        MvcResult mvcResult = mvc.perform(request).andReturn();
        JsonObject responseJson = new JsonParser().parse(mvcResult.getResponse().getContentAsString()).getAsJsonObject();

        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        Assert.assertNotNull(responseJson);
        Assert.assertEquals(productJson.get("name").getAsString(), responseJson.get("name").getAsString());
        Assert.assertNotNull(responseJson.get("id").getAsLong());
    }

    @Test
    public void should_throw_ex_when_add_product_with_invalid_params_given() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/products");
        productJson.remove("name");
        request.content(productJson.toString());
        request.contentType("application/json");
        String localeValue = "tr";
        request.header("Accept-Language", localeValue);

        MvcResult mvcResult = mvc.perform(request).andReturn();
        JsonObject responseJson = new JsonParser().parse(mvcResult.getResponse().getContentAsString()).getAsJsonObject();

        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
        Assert.assertNotNull(responseJson);
        Assert.assertEquals(messages.get("com.dolap.challenge.entity.Product.name.validation.notBlankMessage", java.util.Locale.forLanguageTag(localeValue)), responseJson.get("message").getAsString());
    }

    @Test
    public void should_get_products() throws Exception {
        int productSize = 30;
        for(int i = 1; i <= productSize; i++) {
            Product product = new Product();
            product.setName("Test product name " + i);
            product.setDescription("Test product desc " + i);
            product.setRemainingStockCount(i);
            product.setPrice(new BigDecimal(i * 10));

            productService.addProduct(product);
            // ignore result
        }

        Integer limit = 2;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/products");
        request.param("sortBy", "id");
        request.param("sortOrder", "desc");
        request.param("page", "0");
        request.param("limit", limit.toString());
        request.contentType("application/json");

        MvcResult mvcResult = mvc.perform(request).andReturn();
        JsonObject responseJson = new JsonParser().parse(mvcResult.getResponse().getContentAsString()).getAsJsonObject();

        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        Assert.assertNotNull(responseJson);
        Assert.assertEquals(limit.intValue(), responseJson.get("content").getAsJsonArray().size());
        Assert.assertFalse(responseJson.get("last").getAsBoolean());

        long firstId = responseJson.get("content").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsLong();
        long lastId = responseJson.get("content").getAsJsonArray().get(limit.intValue() - 1).getAsJsonObject().get("id").getAsLong();
        Assert.assertTrue(firstId > lastId); // check sort desc
    }

    @Test
    public void should_delete_product_when_id_provided() throws Exception {
        Product addedProduct = productService.addProduct(product);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/products/" + addedProduct.getId());
        request.contentType("application/json");

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
