package com.dolap.challenge.controller;

import com.dolap.challenge.entity.Payment;
import com.dolap.challenge.entity.PaymentItem;
import com.dolap.challenge.entity.Product;
import com.dolap.challenge.service.PaymentService;
import com.dolap.challenge.service.ProductService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentsController extends BaseController{
    private PaymentService paymentService;

    private ProductService productService;

    /**
     * Constructs a PaymentController with paymentService and productService injected
     *
     * @param paymentService used to process the payments
     * @param productService used to process the products like reserve and release
     */
    public PaymentsController(PaymentService paymentService, ProductService productService) {
        this.productService = productService;
        this.paymentService = paymentService;
    }

    /**
     * Pays for the product with the given quantity
     * Product is reserved for the user before the app processes the payment
     * and this happens "atomic". If the payment is successfully processed a payment log is saved
     * and returned to the client. If the payment is not successfully processed, the products that are
     * reserved for this specific requests are released back so that any other processes can access
     * to those resources.
     *
     * @param paymentItem defines the product and quantity we want to process the payment for
     * @return the payment log for the purchase
     */
    @PostMapping
    public Payment pay(@Valid @RequestBody PaymentItem paymentItem) {
        productService.reserveStockForProduct(paymentItem.getProductId(), paymentItem.getQuantity());
        try {
            Product product = productService.findProduct(paymentItem.getProductId());
            BigDecimal totalPrice = product.getPrice().multiply(new BigDecimal(paymentItem.getQuantity()));

            return paymentService.pay(totalPrice);
        } catch (Exception exception) {
            productService.releaseReservedStockForProduct(paymentItem.getProductId(), paymentItem.getQuantity());
            throw exception;
        }
    }

    /**
     * Provides a list of all the payments to-date
     *
     * @return list of payments
     */
    @GetMapping
    public List<Payment> getPayments() {
        return paymentService.getPayments();
    }
}
