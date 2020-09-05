package com.dolap.challenge.service;

import com.dolap.challenge.configuration.Messages;
import com.dolap.challenge.entity.Category;
import com.dolap.challenge.exception.CategoryNotFoundException;
import com.dolap.challenge.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private Messages messages;
    private CategoryRepository categoryRepository;

    /**
     * Constructs a new CategoryService with messages that depends on the locale
     * and specified category repository.
     *
     * @param messages the interface we used to pull the relevant messages depending on the locale set
     * @param categoryRepository the interface that provides the connection with the data layer.
     */
    public CategoryService(Messages messages, CategoryRepository categoryRepository) {
        this.messages = messages;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Adds a new category and saves it through the repository.
     * Once saved, the updated category is returned back to the caller
     *
     * @param category is the category to be saved
     * @return the category that is saved successfully
     */
    public Category addCategory(Category category){
        return categoryRepository.save(category);
    }

    /**
     * Returns a list of categories from the repository / data layer.
     * Sub categories will be fetched eagerly, so if {@code skipChildren} is set to true, do not include sub categories alone in the result list
     *
     * @param skipChildren the parameter that controls whether sub categories (children) will be included alone in the result list or not
     * @return the list of categories in ascending order by "orderNum" field
     */
    public List<Category> getAll(boolean skipChildren){
        Sort sort = Sort.by("orderNum").ascending();
        return skipChildren ? categoryRepository.findAllSkipChildren(sort) : categoryRepository.findAll(sort);
    }

    /**
     * Deletes the category from the database when valid id is provided
     *
     * @param id s the category id you want to delete
     */
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    /**
     * Retrives the category from the database with given id when valid id is provided
     * @throws {@link CategoryNotFoundException} when the id is invalid
     *
     * @param id is the id of the category you want to retrieve
     * @return the category freshly retrieved with also its subcategories
     */
    public Category findCategory(Long id){
        return categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException(messages.get(CategoryNotFoundException.CATEGORY_NOT_FOUND_EXCEPTION_MESSAGE_KEY)));
    }

    /**
     * Given the id of the category, it is updated with the new values provided
     * In case the provided id is not valid, @{see CategoryNotFoundException} is thrown
     *
     * @param id is the id of the category you want to update
     * @param updatedCategory is the values you want to update to
     * @return updated category when successful
     */
    public Category updateCategory(Long id, Category updatedCategory) {
        return categoryRepository.findById(id)
                .map(category -> {
                    category.setName(updatedCategory.getName());
                    category.setDescription(updatedCategory.getDescription());
                    category.setOrderNum(updatedCategory.getOrderNum());
                    category.setParentCategory(updatedCategory.getParentCategory());
                    return category;
                })
                .orElseThrow(() -> new CategoryNotFoundException(messages.get(CategoryNotFoundException.CATEGORY_NOT_FOUND_EXCEPTION_MESSAGE_KEY)));
    }
}
