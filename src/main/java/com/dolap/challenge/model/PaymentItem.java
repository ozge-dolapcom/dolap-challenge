package com.dolap.challenge.model;

import javax.persistence.Id;
import javax.validation.constraints.Min;

/**
 * Interface that's used to define when the app collects the money
 * It defines which product that the "customers" are paying and in which quantity
 */
public class PaymentItem {

    /**
     * Id of the product that will be purchased
     */
    @Id
    private Long productId;

    /**
     * Amount of the product that will be purchased
     */
    @Min(value = 1, message = "{com.dolap.challenge.model.PaymentItem.quantity.validation.minMessage}")
    private Integer quantity;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
