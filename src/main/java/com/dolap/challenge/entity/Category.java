package com.dolap.challenge.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
public class Category {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * Name of the category
     */
    @NotBlank(message = "{com.dolap.challenge.entity.Category.name.validation.notBlankMessage}")
    private String name;

    /**
     * Description of the category
     */
    @NotBlank(message = "{com.dolap.challenge.entity.Category.description.validation.notBlankMessage}")
    private String description;

    /**
     * Sub category list of the category fetched EAGER.
     * Sub categories are listed in ASC order in a parent category.
     */
    @OneToMany(mappedBy = "parentCategory", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNum ASC")
    private List<Category> subCategoryList;

    /**
     * Parent category information
     * Used only for "write" (deserialization), not accessed during serialization ("read")
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "parent_category_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Category parentCategory;

    /**
     * Order num of the category to determine the order of category on the result list or page.
     * Categories with small order num will be shown first.
     */
    @NotNull(message = "{com.dolap.challenge.entity.Category.orderNum.validation.notNullMessage}")
    @Min(value = 0, message = "{com.dolap.challenge.entity.Category.orderNum.validation.minMessage}")
    private Integer orderNum;

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

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }
}
