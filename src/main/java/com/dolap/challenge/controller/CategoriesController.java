package com.dolap.challenge.controller;

import com.dolap.challenge.entity.Category;
import com.dolap.challenge.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoriesController extends BaseController{

    private CategoryService categoryService;

    /**
     * Constructs a CategoriesController with categoryService injected
     *
     * @param categoryService is the services used for category operations
     */
    public CategoriesController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Adds a new category to the database.
     * Validates the request body to see if it's valid or not.
     *
     * @param newCategory is the category we want to save
     * @return the category that's just saved in the database
     */
    @PostMapping
    public Category addCategory(@Valid @RequestBody Category newCategory) {
        return categoryService.addCategory(newCategory);
    }

    /**
     * Retrieves a list of categories
     * Sub categories will be fetched eagerly, so if {@code skipChildren} is set to true, do not include sub categories alone in the result list
     *
     * @param skipChildren the parameter that controls whether sub categories (children) will be included alone in the result list or not
     *                     default value is true
     * @return the list of categories in ascending order by "orderNum" field
     */
    @GetMapping
    public List<Category> getAllCategories(@RequestParam(defaultValue = "true", required = false) boolean skipChildren) {
        return categoryService.getAll(skipChildren);
    }

    /**
     * Deletes a category
     *
     * @param id is the id of the category you want to delete
     */
    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }

    /**
     * Retrieves a single category by the id
     *
     * @param id you want to retrieve as a category
     * @return the category retrieved
     */
    @GetMapping("/{id}")
    public Category findCategory(@PathVariable Long id) {
        return categoryService.findCategory(id);
    }

    /**
     * Updates a category
     *
     * @param updatedCategory contains the information about the values you want to update to
     * @param id id of the category you want to update
     * @return the updated category
     */
    @PutMapping("/{id}")
    public Category updateCategory(@PathVariable Long id, @Valid @RequestBody Category updatedCategory) {
        return categoryService.updateCategory(id, updatedCategory);
    }
}
