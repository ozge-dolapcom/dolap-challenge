package com.dolap.challenge.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Defines a product where merchants can add / remove / update
 * and "customers" can purchase.
 */
@Entity
public class Product {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * Name of the product that the merchant is selling
     */
    @NotBlank(message = "{com.dolap.challenge.entity.Product.name.validation.notBlankMessage}")
    private String name;

    /**
     * Description of the prodcut
     */
    @NotBlank(message = "{com.dolap.challenge.entity.Product.description.validation.notBlankMessage}")
    private String description;

    /**
     * Defines the number of available stocks for the product where the merchant
     * can collect the proceeds for
     */
    @Min(value = 0, message = "{com.dolap.challenge.entity.Product.remainingStockCount.validation.minMessage}")
    private Integer remainingStockCount;

    /**
     * Price of an individual product (quantity - 1)
     */
    @Positive(message = "{com.dolap.challenge.entity.Product.price.validation.positiveMessage}")
    private BigDecimal price;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRemainingStockCount() {
        return remainingStockCount;
    }

    public void setRemainingStockCount(Integer remainingStockCount) {
        this.remainingStockCount = remainingStockCount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
