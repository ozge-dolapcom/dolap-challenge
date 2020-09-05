package com.dolap.challenge.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Entity
public class Category {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = "{com.dolap.challenge.entity.Category.name.validation.notBlankMessage}")
    private String name;

    @NotBlank(message = "{com.dolap.challenge.entity.Category.description.validation.notBlankMessage}")
    private String description;

    @OneToMany(mappedBy = "parentCategory",
            fetch = FetchType.EAGER,
            cascade =  CascadeType.ALL,
            orphanRemoval = true)
    private List<Category> subCategoryList;

    @ManyToOne
    @JoinColumn(name = "parent_category_id", nullable = true)
    private Category parentCategory;

    @OneToMany(mappedBy = "category")
    private List<Product> productList;

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

    public List<Category> getSubCategoryList() {
        return subCategoryList;
    }

    public void setSubCategoryList(List<Category> subCategoryList) {
        this.subCategoryList = subCategoryList;
    }

    public Category getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(Category parentCategory) {
        this.parentCategory = parentCategory;
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }
}
