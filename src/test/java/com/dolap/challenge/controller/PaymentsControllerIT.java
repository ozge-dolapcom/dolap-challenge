package com.dolap.challenge.controller;

import com.dolap.challenge.configuration.Messages;
import com.dolap.challenge.entity.Category;
import com.dolap.challenge.entity.Product;
import com.dolap.challenge.exception.OutOfStockException;
import com.dolap.challenge.service.CategoryService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PaymentsControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private Messages messages;

    private Product product;

    private Category category;

    @Before
    public void setup() {
        product = new Product();
        product.setName("The great product");
        product.setDescription("This product will help me get thru the interview process :fingers crossed:");
        product.setPrice(new BigDecimal("9.99"));
        product.setRemainingStockCount(99);

        category = new Category();
        category.setName("Cat cat");
        category.setDescription("Where kittens hangout");
        category.setOrderNum(1);
        categoryService.addCategory(category);

        product.setCategory(category);
        productService.addProduct(product);
    }

    @Test
    public void should_pay_with_single_quantity() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/payments");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("productId", product.getId());
        jsonObject.addProperty("quantity", 1);
        request.content(jsonObject.toString());
        request.contentType("application/json");

        MvcResult mvcResult = mvc.perform(request).andReturn();
        JsonObject responseJson = new JsonParser().parse(mvcResult.getResponse().getContentAsString()).getAsJsonObject();

        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        Assert.assertNotNull(responseJson);
        Assert.assertEquals(product.getPrice(), BigDecimal.valueOf((responseJson.get("price").getAsDouble())));
        Assert.assertEquals("200", (responseJson.get("bankResponse").getAsString()));
    }

    @Test
    public void should_throw_exception_when_try_to_purchase_more_than_the_stock() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/payments");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("productId", product.getId());
        jsonObject.addProperty("quantity", product.getRemainingStockCount() + 10);
        request.content(jsonObject.toString());
        request.contentType("application/json");
        String localeValue = "tr";
        request.header("Accept-Language", localeValue);

        MvcResult mvcResult = mvc.perform(request).andReturn();
        JsonObject responseJson = new JsonParser().parse(mvcResult.getResponse().getContentAsString()).getAsJsonObject();

        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
        Assert.assertNotNull(responseJson);
        Assert.assertEquals(messages.get(OutOfStockException.OUT_OF_STOCK_EXCEPTION_MESSAGE_KEY, java.util.Locale.forLanguageTag(localeValue)), responseJson.get("message").getAsString());
    }

    @Test
    public void should_handle_concurrent_txns_properly() throws ExecutionException, InterruptedException {
        List<CompletableFuture> futures = new ArrayList<>();
        List<MvcResult> results = new ArrayList<>();

        // user1 pays for the majority
        JsonObject jsonObject1 = new JsonObject();
        jsonObject1.addProperty("productId", product.getId());
        jsonObject1.addProperty("quantity", 98);
        CompletableFuture<MvcResult> completableFuture1 = paymentWithQuantity(jsonObject1);
        futures.add(completableFuture1);

        // user 2 pays for a single one
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("productId", product.getId());
        jsonObject2.addProperty("quantity", 1);
        CompletableFuture<MvcResult> completableFuture2 = paymentWithQuantity(jsonObject2);
        futures.add(completableFuture2);

        // user 3 pays for a single one
        JsonObject jsonObject3 = new JsonObject();
        jsonObject3.addProperty("productId", product.getId());
        jsonObject3.addProperty("quantity", 1);
        CompletableFuture<MvcResult> completableFuture3 = paymentWithQuantity(jsonObject3);
        futures.add(completableFuture3);

        futures.stream().forEach(f -> CompletableFuture.allOf(f).join());

        results.add(completableFuture1.get());
        results.add(completableFuture2.get());
        results.add(completableFuture3.get());

        // one of the payments should be null because it will receive out of stock exception
        // check the total payments count
        int totalSuccessfullPayments = results.stream().map((result) -> {
            return (result.getResponse().getStatus() == HttpStatus.OK.value() ? 1 : 0);
        }).reduce(0, (subtotal, element) -> subtotal + element);

        Assert.assertEquals(totalSuccessfullPayments, 2);
    }

    protected  CompletableFuture<MvcResult> paymentWithQuantity(JsonObject jsonObject) {
        return CompletableFuture.supplyAsync(() -> {
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/payments");
            request.content(jsonObject.toString());
            request.contentType("application/json");
            String localeValue = "tr";
            request.header("Accept-Language", localeValue);

            MvcResult mvcResult = null;
            try {
                mvcResult = mvc.perform(request).andReturn();
            } catch (Exception ignored) {
            }
            return mvcResult;
        });
    }

}
