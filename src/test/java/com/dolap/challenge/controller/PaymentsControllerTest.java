package com.dolap.challenge.controller;

import com.dolap.challenge.entity.Payment;
import com.dolap.challenge.entity.PaymentItem;
import com.dolap.challenge.entity.Product;
import com.dolap.challenge.exception.OutOfStockException;
import com.dolap.challenge.service.PaymentService;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAsync
public class PaymentsControllerTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ProductService productService;

    private Product product;

    @Before
    public void setup() {
        product = new Product();
        product.setName("The great product");
        product.setDescription("This product will help me get thru the interview process :fingers crossed:");
        product.setPrice(new BigDecimal("9.99"));
        product.setRemainingStockCount(99);

        productService.addProduct(product);
    }

    @Test
    public void should_pay_with_single_quantity() {
        PaymentItem paymentItem = new PaymentItem();
        paymentItem.setProductId(product.getId());
        paymentItem.setQuantity(1);

        PaymentsController controller = new PaymentsController(paymentService, productService);
        Exception exception = null;
        Payment payment = null;
        try {
            payment = controller.pay(paymentItem);
        } catch (Exception e) {
            exception = e;
        }

        Assert.assertNotNull(payment);
        Assert.assertNull(exception);
        Assert.assertEquals(payment.getPrice(), product.getPrice());
        Assert.assertEquals(payment.getBankResponse(), "200");

        int expectedRemainingProductCount = product.getRemainingStockCount() - 1;
        Product refreshedProduct = productService.findProduct(product.getId());
        Assert.assertNotNull(refreshedProduct);
        Assert.assertEquals(refreshedProduct.getRemainingStockCount(), Integer.valueOf(expectedRemainingProductCount));
    }

    @Test
    public void should_pay_for_more_than_one_product_quantity() {
        PaymentItem paymentItem = new PaymentItem();
        paymentItem.setProductId(product.getId());
        paymentItem.setQuantity(product.getRemainingStockCount());

        PaymentsController controller = new PaymentsController(paymentService, productService);
        Exception exception = null;
        Payment payment = null;
        try {
            payment = controller.pay(paymentItem);
        } catch (Exception e) {
            exception = e;
        }

        Assert.assertNotNull(payment);
        Assert.assertNull(exception);
        Assert.assertEquals(payment.getPrice(), product.getPrice().multiply(new BigDecimal(paymentItem.getQuantity())));
        Assert.assertEquals(payment.getBankResponse(), "200");

        int expectedRemainingProductCount = product.getRemainingStockCount() - paymentItem.getQuantity();
        Product refreshedProduct = productService.findProduct(product.getId());
        Assert.assertNotNull(refreshedProduct);
        Assert.assertEquals(refreshedProduct.getRemainingStockCount(), Integer.valueOf(expectedRemainingProductCount));
    }

    @Test
    public void should_throw_exception_when_try_to_purchase_more_than_the_stock() {
        PaymentItem paymentItem = new PaymentItem();
        paymentItem.setProductId(product.getId());
        paymentItem.setQuantity(product.getRemainingStockCount() + 10);

        PaymentsController controller = new PaymentsController(paymentService, productService);
        Exception exception = null;
        Payment payment = null;
        try {
            payment = controller.pay(paymentItem);
        } catch (Exception e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertNull(payment);
        Assert.assertTrue(exception instanceof OutOfStockException);

        int expectedRemainingProductCount = product.getRemainingStockCount();
        Product refreshedProduct = productService.findProduct(product.getId());
        Assert.assertNotNull(refreshedProduct);
        Assert.assertEquals(refreshedProduct.getRemainingStockCount(), Integer.valueOf(expectedRemainingProductCount));
    }

    @Test
    public void should_handle_concurrent_txns_properly() throws ExecutionException, InterruptedException {
        PaymentsController controller = new PaymentsController(paymentService, productService);

        List<CompletableFuture> futures = new ArrayList<>();
        List<Payment> payments = new ArrayList<>();

        // user1 pays for the majority
        PaymentItem paymentItem1 = new PaymentItem();
        paymentItem1.setQuantity(98);
        paymentItem1.setProductId(product.getId());
        CompletableFuture<Payment> completableFuture1 = paymentWithQuantity(controller, paymentItem1);
        futures.add(completableFuture1);

        // user 2 pays for a single one
        PaymentItem paymentItem2 = new PaymentItem();
        paymentItem2.setQuantity(1);
        paymentItem2.setProductId(product.getId());
        CompletableFuture<Payment> completableFuture2 = paymentWithQuantity(controller, paymentItem2);
        futures.add(completableFuture2);

        // user 3 pays for a single one
        PaymentItem paymentItem3 = new PaymentItem();
        paymentItem3.setQuantity(1);
        paymentItem3.setProductId(product.getId());
        CompletableFuture<Payment> completableFuture3 = paymentWithQuantity(controller, paymentItem3);
        futures.add(completableFuture3);

        futures.stream().forEach(f -> CompletableFuture.allOf(f).join());

        payments.add(completableFuture1.get());
        payments.add(completableFuture2.get());
        payments.add(completableFuture3.get());

        // one of the payments should be null because it will receive out of stock exception
        // check the total payments count
        int totalSuccessfullPayments = payments.stream().map((payment) -> {
            return (payment == null ? 0 : 1);
        }).reduce(0, (subtotal, element) -> subtotal + element);

        Assert.assertEquals(totalSuccessfullPayments, 2);
    }

    protected  CompletableFuture<Payment> paymentWithQuantity(PaymentsController controller, PaymentItem paymentItem) {
        return CompletableFuture.supplyAsync(() -> {
            Payment payment = null;
            try {
                payment = controller.pay(paymentItem);
            } catch (Exception ignored) {
            }
            return payment;
        });
    }
}
