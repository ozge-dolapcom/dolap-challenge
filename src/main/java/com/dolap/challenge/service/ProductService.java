package com.dolap.challenge.service;

import com.dolap.challenge.configuration.Messages;
import com.dolap.challenge.entity.Category;
import com.dolap.challenge.entity.Product;
import com.dolap.challenge.exception.CategoryNotFoundException;
import com.dolap.challenge.exception.OutOfStockException;
import com.dolap.challenge.exception.ProductNotFoundException;
import com.dolap.challenge.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.util.ArrayList;

@Service
@Transactional
public class ProductService {
    private EntityManager entityManager;
    private Messages messages;
    private ProductRepository productRepository;
    private CategoryService categoryService;

    /**
     * Constructs a new ProductService with specified product repository,
     * entityManager and messages that depends on the locale.
     *
     * @param productRepository the interface that provides the connection with the data layer.
     * @param entityManager the interface used to lock the records
     * @param messages the interface we used to pull the relevant messages depending on the locale set
     */
    public ProductService(ProductRepository productRepository, EntityManager entityManager, Messages messages, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.entityManager = entityManager;
        this.messages = messages;
        this.categoryService = categoryService;
    }

    /**
     * Adds a new product and saves it through the repository.
     * Once saved, the updated product is returned back to the caller
     *
     * @param product is the product to be saved
     * @return the product that is saved successfully
     */
    public Product addProduct(Product product) {
        if(product.getCategory().getId() == null){
            throw new CategoryNotFoundException(messages.get(CategoryNotFoundException.CATEGORY_NOT_FOUND_EXCEPTION_MESSAGE_KEY));
        }
        Category category = categoryService.findCategory(product.getCategory().getId());
        product.setCategory(category);
        return productRepository.save(product);
    }

    /**
     * Returns a list of products from the repository / data layer.
     * Ideally we'd use elasticsearch or solr to search the products but in this context
     * loading from the database should be fair enough.
     *
     *
     * @param categoryId
     * @param sortBy is the field you want to on
     * @param sortOrder whether it is desc or asc by the sort column {@see sortBy}
     * @param page defines the offset
     * @param limit is the number of total records that will be retrieved from the repository
     * @return a page which contains a list of products that fits the search criteria
     */
    public Page<Product> getAll(Long categoryId, String sortBy, String sortOrder, Integer page, Integer limit) {
        Category rootCategory = categoryService.findCategory(categoryId);
        ArrayList<Long> idList = findAllCategoryTreeIds(rootCategory);
        return productRepository.findAllByCategory(idList, PageRequest.of(page, limit, getSort(sortBy, sortOrder)));
    }

    private ArrayList<Long> findAllCategoryTreeIds(Category rootCategory){
        ArrayList<Long> idList = new ArrayList<>();
        idList.add(rootCategory.getId());
        if(rootCategory.getSubCategoryList() != null && !rootCategory.getSubCategoryList().isEmpty()){
            rootCategory.getSubCategoryList().stream().forEach(subCategory -> idList.addAll(findAllCategoryTreeIds(subCategory)));
        }
        return idList;
    }

    /**
     * Constructs a Sort object that defines the sort column and sort direction
     *
     * @param sortBy is the column you want to sort on
     * @param sortOrder is the direction you want to sort - asc or desc
     * @return the constructed sort object {@see Sort}
     */
    private Sort getSort(String sortBy, String sortOrder) {
        Sort sort = Sort.by(sortBy);
        switch (sortOrder) {
            case "asc":
                sort = sort.ascending();
                break;
            case "desc":
                sort = sort.descending();
                break;
            default:
                break;
        }
        return sort;
    }

    /**
     * Given the id of the product, it is updated with the new values provided
     * In case the provided id is not valid, @{see ProductNotFoundException} is thrown
     *
     * @param id is the id of the product you want to update
     * @param updatedProduct is the values you want to update to
     * @return updated product when successful
     */
    public Product updateProduct(Long id, Product updatedProduct) {
        if(updatedProduct.getCategory().getId() == null){
            throw new CategoryNotFoundException(messages.get(CategoryNotFoundException.CATEGORY_NOT_FOUND_EXCEPTION_MESSAGE_KEY));
        }
        Category updatedCategory = categoryService.findCategory(updatedProduct.getCategory().getId());
        return productRepository.findById(id)
                .map(product -> {
                    product.setName(updatedProduct.getName());
                    product.setDescription(updatedProduct.getDescription());
                    product.setRemainingStockCount(updatedProduct.getRemainingStockCount());
                    product.setPrice(updatedProduct.getPrice());
                    product.setCategory(updatedCategory);
                    return product;
                })
                .orElseThrow(() -> new ProductNotFoundException(messages.get(ProductNotFoundException.PRODUCT_NOT_FOUND_EXCEPTION_MESSAGE_KEY)));
    }

    /**
     * Deletes the product from the database when valid id is provided
     * Nothing happens if the id is invalid
     *
     * @param id is the product id you want to delete
     */
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    /**
     * Retrives the product from the database with given id when valid id is provided
     * @throws {@link ProductNotFoundException} when the id is invalid
     *
     * @param id is the id of the product you want to retrieve
     * @return the product freshly retrieved
     */
    public Product findProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(messages.get(ProductNotFoundException.PRODUCT_NOT_FOUND_EXCEPTION_MESSAGE_KEY)));
    }

    /**
     * Reserves stocks of the product with any quantity provided
     * This operation is "atomic" in a way that the record is locked for update
     * so that no other operations can read / change the value which could cause reading false information
     * Also, @throws {@link OutOfStockException} when trying to reserve more than what's in the stock
     *
     * @param productId is the id of the product you want to reserve some stocks
     * @param quantity is the amount you want to reserve for yourself
     * @throws OutOfStockException when trying to reserve more than what's in the stocks
     */
    public void reserveStockForProduct(Long productId, Integer quantity) throws OutOfStockException {
        Product product = entityManager.find(Product.class, productId, LockModeType.PESSIMISTIC_WRITE);
        if (product.getRemainingStockCount() < quantity){
            throw new OutOfStockException(messages.get(OutOfStockException.OUT_OF_STOCK_EXCEPTION_MESSAGE_KEY));
        }

        int remainingStockCount = product.getRemainingStockCount() - quantity;
        product.setRemainingStockCount(remainingStockCount);
    }

    /**
     * Releases the reserved stocks for the given amount
     * Any released stocks will increase the {@link Product#getRemainingStockCount()} of the product
     * so that it's atomic.
     * The product is locked for update so that no other process / threads could read false information
     *
     * @param productId is the id of the product you want to release the reserved stocks
     * @param quantity is the amount you want to release
     */
    public void releaseReservedStockForProduct(Long productId, Integer quantity) {
        Product product = entityManager.find(Product.class, productId, LockModeType.PESSIMISTIC_WRITE);
        int stockCount = product.getRemainingStockCount() + quantity;
        product.setRemainingStockCount(stockCount);
    }
}
