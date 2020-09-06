package com.dolap.challenge.service;

import com.dolap.challenge.entity.Product;
import com.dolap.challenge.exception.OutOfStockException;
import com.dolap.challenge.exception.ProductNotFoundException;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAsync
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    private Product product;

    @Before
    public void setup() {
        product = new Product();
        product.setName("The greatest product");
        product.setDescription("Some random product description goes here");
        product.setRemainingStockCount(99);
        product.setPrice(new BigDecimal("9.99"));
    }

    @Test
    public void should_add_product_when_all_fields_are_valid() {
        Product addedProduct = productService.addProduct(product);

        Assert.assertNotNull(addedProduct);
        Assert.assertEquals(addedProduct.getId(), product.getId());
        Assert.assertEquals(addedProduct.getName(), product.getName());
    }

    @Test
    public void should_throw_exception_when_adding_a_product_with_empty_name() {
        product.setName(null);

        Exception exception = null;
        Product addedProduct = null;
        try {
            productService.addProduct(product);
        } catch (Exception e) {
            exception = e;
        }

        Assert.assertNull(addedProduct);
        Assert.assertNotNull(exception);
        Assert.assertNull(product.getId());
    }

    @Test
    public void should_find_product_by_id() {
        Product addedProduct = productService.addProduct(product);

        Product foundProduct = productService.findProduct(addedProduct.getId());
        Assert.assertNotNull(foundProduct);
        Assert.assertEquals(foundProduct.getId(), addedProduct.getId());
        Assert.assertEquals(foundProduct.getPrice(), addedProduct.getPrice());
    }

    @Test
    public void should_throw_exception_when_finding_product_with_invalid_id() {
        Exception exception = null;
        Product foundProduct = null;
        try {
            foundProduct = productService.findProduct(Long.MAX_VALUE);
        } catch (Exception e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertNull(foundProduct);
        Assert.assertTrue(exception instanceof ProductNotFoundException);
    }

//    @Test
//    public void should_get_all_products() {
//        int productSize = 30;
//        for(int i = 1; i <= productSize; i++) {
//            Product product = new Product();
//            product.setName("Test product name " + i);
//            product.setDescription("Test product desc " + i);
//            product.setRemainingStockCount(i);
//            product.setPrice(new BigDecimal(i * 10));
//
//            productService.addProduct(product);
//            // ignore result
//        }
//
//        String sortBy = "id";
//        String sortOrder = "desc";
//        Integer page = 0;
//        Integer limit = 10;
//        Page<Product> responseProducts = productService.getAll(null, sortBy, sortOrder, page, limit);
//        Assert.assertNotNull(responseProducts);
//        Assert.assertEquals(limit, Integer.valueOf(responseProducts.getContent().size()));
//        Assert.assertFalse(responseProducts.isLast());
//
//        Product product1 = responseProducts.getContent().get(0);
//        Product product2 = responseProducts.getContent().get(responseProducts.getContent().size() - 1);
//
//        Assert.assertTrue(product1.getId() > product2.getId()); // check sort desc
//    }

    @Test
    public void should_update_product_when_trying_update_with_valid_parameters() {
        Product addedProduct = productService.addProduct(product);
        addedProduct.setName("updated name goes here");

        Product updatedProduct = productService.updateProduct(addedProduct.getId(), addedProduct);
        Product foundProduct = productService.findProduct(addedProduct.getId());

        Assert.assertNotNull(updatedProduct);
        Assert.assertNotNull(foundProduct);
        Assert.assertEquals(foundProduct.getName(), updatedProduct.getName());
        Assert.assertEquals(foundProduct.getId(), addedProduct.getId());
        Assert.assertEquals(updatedProduct.getId(), addedProduct.getId());
        Assert.assertEquals(foundProduct.getName(), addedProduct.getName());
    }

    @Test
    public void should_throw_exception_when_trying_update_with_invalid_paramters() {
        Product addedProduct = productService.addProduct(product);
        addedProduct.setRemainingStockCount(-1);

        Exception exception = null;
        Product updatedProduct = null;
        try {
            updatedProduct = productService.updateProduct(addedProduct.getId(), addedProduct);
        } catch (Exception e) {
            exception = e;
        }
        Product foundProduct = productService.findProduct(addedProduct.getId());

        Assert.assertNull(updatedProduct);
        Assert.assertNotNull(exception);
        Assert.assertEquals(foundProduct.getId(), addedProduct.getId());
        Assert.assertNotEquals(foundProduct.getRemainingStockCount(), addedProduct.getRemainingStockCount());
    }

    @Test
    public void should_be_able_to_delete_product_by_id() {
        Product addedProduct = productService.addProduct(product);
        productService.deleteProduct(addedProduct.getId());

        Exception exception = null;
        Product foundProduct = null;
        try {
            foundProduct = productService.findProduct(addedProduct.getId());
        } catch (Exception e) {
            exception = e;
        }

        Assert.assertNull(foundProduct);
        Assert.assertNotNull(exception);
        Assert.assertTrue(exception instanceof ProductNotFoundException);
    }

    @Test
    public void should_reserve_product_and_decrease_the_remaining_stock_count_by_quantity() {
        Product addedProduct = productService.addProduct(product);
        int reserveQuantity = 2;

        productService.reserveStockForProduct(addedProduct.getId(), reserveQuantity);
        Product freshProduct = productService.findProduct(addedProduct.getId());

        int expectedRemainingQuantity = addedProduct.getRemainingStockCount() - reserveQuantity;

        Assert.assertNotNull(freshProduct);
        Assert.assertEquals(freshProduct.getId(), addedProduct.getId());
        Assert.assertEquals(freshProduct.getRemainingStockCount(), Integer.valueOf(expectedRemainingQuantity));
    }

    @Test
    public void should_throw_exception_when_trying_over_reserve() {
        Product addedProduct = productService.addProduct(product);
        int reserveQuantity = addedProduct.getRemainingStockCount() + 2;

        Exception exception = null;
        try {
            productService.reserveStockForProduct(addedProduct.getId(), reserveQuantity);
        } catch (Exception e) {
            exception = e;
        }

        Product freshProduct = productService.findProduct(addedProduct.getId());

        Assert.assertNotNull(freshProduct);
        Assert.assertEquals(freshProduct.getId(), addedProduct.getId());
        Assert.assertEquals(freshProduct.getRemainingStockCount(), addedProduct.getRemainingStockCount());
        Assert.assertNotNull(exception);
        Assert.assertTrue(exception instanceof OutOfStockException);
    }

    @Test
    public void should_handle_concurrent_reserve_requests_properly() throws ExecutionException, InterruptedException {
        productService.addProduct(product); // ignore result

        List<CompletableFuture> futures = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();

        // user1 pays for the majority
        CompletableFuture<Exception> completableFuture1 = reserveWithQuantity(productService, product.getId(), 98);
        futures.add(completableFuture1);

        // user 2 pays for a single one
        CompletableFuture<Exception> completableFuture2 = reserveWithQuantity(productService, product.getId(), 1);
        futures.add(completableFuture2);

        // user 3 pays for a single one
        CompletableFuture<Exception> completableFuture3 = reserveWithQuantity(productService, product.getId(), 1);
        futures.add(completableFuture3);

        futures.stream().forEach(f -> CompletableFuture.allOf(f).join());

        exceptions.add(completableFuture1.get());
        exceptions.add(completableFuture2.get());
        exceptions.add(completableFuture3.get());

        // one of the payments should be null because it will receive out of stock exception
        // check the total payments count
        int totalFailedReserves = exceptions.stream().map((exception) -> {
            return (exception == null ? 0 : 1);
        }).reduce(0, (subtotal, element) -> subtotal + element);

        Assert.assertEquals(totalFailedReserves, 1);
    }

    @Test
    public void should_release_product_and_increase_the_remaining_stock_count_by_quantity() {
        Product addedProduct = productService.addProduct(product);
        int releasedQuantity = 2;

        productService.releaseReservedStockForProduct(product.getId(), releasedQuantity);
        Product freshProduct = productService.findProduct(addedProduct.getId());

        int expectedRemainingQuantity = addedProduct.getRemainingStockCount() + releasedQuantity;

        Assert.assertNotNull(freshProduct);
        Assert.assertEquals(freshProduct.getId(), addedProduct.getId());
        Assert.assertEquals(freshProduct.getRemainingStockCount(), Integer.valueOf(expectedRemainingQuantity));
    }

    @Test
    public void should_handle_concurrent_release_requests_properly() throws ExecutionException, InterruptedException {
        Product addedProduct = productService.addProduct(product);

        List<CompletableFuture> futures = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();

        // user1 pays for a single one
        CompletableFuture<Exception> completableFuture1 = releaseWithQuantity(productService, product.getId(), 1);
        futures.add(completableFuture1);

        // user 2 pays for a single one
        CompletableFuture<Exception> completableFuture2 = releaseWithQuantity(productService, product.getId(), 1);
        futures.add(completableFuture2);

        // user 3 pays for a single one
        CompletableFuture<Exception> completableFuture3 = releaseWithQuantity(productService, product.getId(), 1);
        futures.add(completableFuture3);

        futures.stream().forEach(f -> CompletableFuture.allOf(f).join());

        exceptions.add(completableFuture1.get());
        exceptions.add(completableFuture2.get());
        exceptions.add(completableFuture3.get());

        // none of the release operations should receive exception
        // check the total exception count
        int totalFailedRelease = exceptions.stream().map((exception) -> {
            return (exception == null ? 0 : 1);
        }).reduce(0, (subtotal, element) -> subtotal + element);

        Product freshProduct = productService.findProduct(addedProduct.getId());
        int expectedRemainingQuantity = addedProduct.getRemainingStockCount() + 3;

        Assert.assertEquals(totalFailedRelease, 0);
        Assert.assertEquals(freshProduct.getRemainingStockCount(), Integer.valueOf(expectedRemainingQuantity));
    }

    protected CompletableFuture<Exception> reserveWithQuantity(ProductService productService, Long productId, int quantity) {
        return CompletableFuture.supplyAsync(() -> {
            Exception exception = null;
            try {
                productService.reserveStockForProduct(productId, quantity);
            } catch (Exception e) {
                exception = e;
            }
            return exception;
        });
    }

    protected CompletableFuture<Exception> releaseWithQuantity(ProductService productService, Long productId, int quantity) {
        return CompletableFuture.supplyAsync(() -> {
            Exception exception = null;
            try {
                productService.releaseReservedStockForProduct(productId, quantity);
            } catch (Exception e) {
                exception = e;
            }
            return exception;
        });
    }
}
