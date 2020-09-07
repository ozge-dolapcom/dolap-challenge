package com.dolap.challenge.controller;

import com.dolap.challenge.entity.Product;
import com.dolap.challenge.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
@RequestMapping("/products")
public class ProductsController extends BaseController {

    private ProductService productService;

    /**
     * Constructs a ProductsController with productsService injected
     *
     * @param productService is the services used for product operations
     */
    public ProductsController(ProductService productService){
        this.productService = productService;
    }

    /**
     * Adds a new product to the database.
     * Validates the request body to see if it's valid or not.
     *
     * @param newProduct is the product we want to save
     * @return the product that's just saved in the database
     */
    @PostMapping
    public Product addProduct(@Valid @RequestBody Product newProduct) {
        return productService.addProduct(newProduct);
    }

    /**
     * Retrieves a list of products of the given category with the defined criteria by the request parameters
     * Defaults to order by id desc offset 0 limit 20
     *
     * @param categoryId is the category id of the product (required)
     * @param sortBy is the sort column
     * @param sortOrder is the direction of the sort - asc or desc
     * @param page is the offset
     * @param limit is the number of products in the response
     * @return the page information that contains the requested products
     */
    @GetMapping
    public Page<Product> getProducts(
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "id", required = false) String sortBy,
            @RequestParam(defaultValue = "desc", required = false) String sortOrder,
            @RequestParam(defaultValue = "0", required = false) Integer page,
            @RequestParam(defaultValue = "20", required = false) Integer limit) {
        return productService.getAll(categoryId, sortBy, sortOrder, page, limit);
    }

    /**
     * Updates a product
     * Category information is necessary.
     *
     * @param id id of the product you want to update
     * @param updatedProduct contains the information about the values you want to update to
     * @return the updated product
     */
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @Valid @RequestBody Product updatedProduct) {
        return productService.updateProduct(id, updatedProduct);
    }

    /**
     * Deletes a product
     *
     * @param id is the id of the product you want to delete
     */
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    /**
     * Retrieves a single product by the id
     *
     * @param id you want to retrieve as a product
     * @return the product retrieved
     */
    @GetMapping("/{id}")
    public Product findProduct(@PathVariable Long id) {
        return productService.findProduct(id);
    }
}
