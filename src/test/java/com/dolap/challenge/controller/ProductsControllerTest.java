package com.dolap.challenge.controller;

import com.dolap.challenge.entity.Category;
import com.dolap.challenge.entity.Product;
import com.dolap.challenge.exception.CategoryNotFoundException;
import com.dolap.challenge.exception.ProductNotFoundException;
import com.dolap.challenge.service.CategoryService;
import com.dolap.challenge.service.ProductService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductsControllerTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    private Category rootCategory;
    private Category child1Category;
    private Category child2Category;
    private Category child11Category;
    private Category child12Category;
    private Product product;

    @Before
    public void setup() {
        setupCategoryTree();

        product = new Product();
        product.setName("Mavi Elbise");
        product.setDescription("Mavi renkte bir elbisedir");
        product.setRemainingStockCount(99);
        product.setPrice(new BigDecimal("9.99"));
        product.setCategory(child11Category);
    }

    private void setupCategoryTree() {
        rootCategory = new Category();
        rootCategory.setName("Kadin");
        rootCategory.setDescription("Kadin kategorisi");
        rootCategory.setOrderNum(0);
        rootCategory = categoryService.addCategory(rootCategory);

        child1Category = new Category();
        child1Category.setName("Giyim");
        child1Category.setDescription("Kadin+giyim kategorisi");
        child1Category.setOrderNum(0);
        child1Category.setParentCategory(rootCategory);
        categoryService.addCategory(child1Category);

        child11Category = new Category();
        child11Category.setName("Elbise");
        child11Category.setDescription("Kadin+giyim+elbise kategorisi");
        child11Category.setOrderNum(0);
        child11Category.setParentCategory(child1Category);
        categoryService.addCategory(child11Category);

        child12Category = new Category();
        child12Category.setName("Pantalon");
        child12Category.setDescription("Kadin+giyim+pantalon kategorisi");
        child12Category.setOrderNum(1);
        child12Category.setParentCategory(child1Category);
        categoryService.addCategory(child12Category);

        child2Category = new Category();
        child2Category.setName("Ayakkabi");
        child2Category.setDescription("Kadin+ayakkabi kategorisi");
        child2Category.setOrderNum(0);
        child2Category.setParentCategory(rootCategory);
        categoryService.addCategory(child2Category);
    }

    private void addProducts(ProductsController controller) {
        int productSize = 30;
        for(int i = 1; i <= productSize; i++) {
            Product product = new Product();
            product.setName("Test product name " + i);
            product.setDescription("Test product desc " + i);
            product.setRemainingStockCount(i);
            product.setPrice(new BigDecimal(i * 10));

            if(i > 0 && i <= 10){
                product.setCategory(child1Category);
            } else if(i > 10 && i <= 20){
                product.setCategory(child2Category);
            } else {
                product.setCategory(child12Category);
            }

            controller.addProduct(product);
            // ignore result
        }
    }

    @Test
    public void should_add_product_with_valid_params_given() {
        ProductsController controller = new ProductsController(productService);
        Product responseProduct = controller.addProduct(product);
        Assert.assertNotNull(responseProduct);
        Assert.assertNotNull(responseProduct.getId());
        Assert.assertEquals(product.getName(), responseProduct.getName());
        Assert.assertEquals(product.getDescription(), responseProduct.getDescription());
        Assert.assertEquals(product.getRemainingStockCount(), responseProduct.getRemainingStockCount());
        Assert.assertEquals(product.getPrice(), responseProduct.getPrice());
        Assert.assertEquals(product.getCategory().getId(), responseProduct.getCategory().getId());
    }

    @Test
    public void should_throw_ex_when_add_product_with_invalid_params_given() {
        product.setName(null); // name missing intentionally!

        Exception exception = null;
        Product addedProduct = null;
        ProductsController controller = new ProductsController(productService);
        try {
            addedProduct = controller.addProduct(product);
        } catch (Exception e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertNull(addedProduct);
    }

    @Test
    public void should_throw_exception_when_adding_a_product_with_empty_category() {
        product.getCategory().setId(null); // id missing intentionally!

        Exception exception = null;
        Product addedProduct = null;
        ProductsController controller = new ProductsController(productService);
        try {
            addedProduct = controller.addProduct(product);
        } catch (Exception e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertNull(addedProduct);
        Assert.assertTrue(exception instanceof CategoryNotFoundException);
    }

    @Test
    public void should_get_all_products_child1Category() {
        ProductsController controller = new ProductsController(productService);
        addProducts(controller);

        String sortBy = "id";
        String sortOrder = "desc";
        Integer page = 0;
        Integer limit = 10;
        Page<Product> responseProducts = controller.getProducts(child1Category.getId(), sortBy, sortOrder, page, limit);
        Assert.assertNotNull(responseProducts);
        Assert.assertEquals(limit, Integer.valueOf(responseProducts.getContent().size()));
        Assert.assertFalse(responseProducts.isLast());
        Assert.assertEquals(Integer.valueOf(20), Integer.valueOf((int)responseProducts.getTotalElements())); // child1Category + child12Category

        Product product1 = responseProducts.getContent().get(0);
        Product product2 = responseProducts.getContent().get(responseProducts.getContent().size() - 1);

        Assert.assertTrue(product1.getId() > product2.getId()); // check sort desc
    }

    @Test
    public void should_get_only_one_product_child11Category() {
        ProductsController controller = new ProductsController(productService);
        Product addedProduct = controller.addProduct(product);

        String sortBy = "id";
        String sortOrder = "asc";
        Integer page = 0;
        Integer limit = 2;
        Page<Product> responseProducts = controller.getProducts(child11Category.getId(), sortBy, sortOrder, page, limit);
        Assert.assertNotNull(responseProducts);
        Assert.assertNotEquals(limit, Integer.valueOf(responseProducts.getContent().size()));
        Assert.assertTrue(responseProducts.isLast());
        Assert.assertEquals(Integer.valueOf(1), Integer.valueOf((int)responseProducts.getTotalElements())); // product only
        Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(responseProducts.getContent().size())); // product only

        Assert.assertEquals(responseProducts.getContent().get(0).getId(), addedProduct.getId());
    }

    @Test
    public void should_throw_exception_when_trying_to_get_all_products_invalid_category() {
        ProductsController controller = new ProductsController(productService);

        String sortBy = "id";
        String sortOrder = "asc";
        Integer page = 0;
        Integer limit = 2;

        Exception exception = null;
        Page<Product> responseProducts = null;
        try {
            responseProducts = controller.getProducts(Long.valueOf(99999999), sortBy, sortOrder, page, limit);
        } catch (Exception e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertNull(responseProducts);
        Assert.assertTrue(exception instanceof CategoryNotFoundException);
    }

    @Test
    public void should_get_single_product_with_valid_id() {
        ProductsController controller = new ProductsController(productService);
        Product addedProduct = addProductWithController(controller);

        Product foundProduct = controller.findProduct(addedProduct.getId());
        Assert.assertNotNull(foundProduct);
        Assert.assertEquals(foundProduct.getId(), addedProduct.getId());
    }

    @Test
    public void should_throw_exception_when_getting_with_invalid_id() {
        ProductsController controller = new ProductsController(productService);

        Exception exception = null;
        Product foundProduct = null;
        try {
            foundProduct = controller.findProduct(Long.MAX_VALUE);
        } catch (Exception e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertNull(foundProduct);
        Assert.assertTrue(exception instanceof ProductNotFoundException);
    }

    @Test
    public void should_delete_product_when_id_provided() {
        ProductsController controller = new ProductsController(productService);
        Product addedProduct = addProductWithController(controller);

        controller.deleteProduct(addedProduct.getId());

        Exception exception = null;
        Product foundProduct = null;
        try {
            foundProduct = controller.findProduct(addedProduct.getId());
        } catch (Exception e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertNull(foundProduct);
        Assert.assertTrue(exception instanceof ProductNotFoundException);
    }

    @Test
    public void should_update_product_when_valid_params_given() {
        ProductsController controller = new ProductsController(productService);
        Product addedProduct = addProductWithController(controller);
        addedProduct.setName("changed the name of the product");
        addedProduct.setCategory(child12Category);

        Product updatedProduct = controller.updateProduct(addedProduct.getId(), addedProduct);
        Product fetchedProduct = controller.findProduct(addedProduct.getId());

        Assert.assertNotNull(updatedProduct);
        Assert.assertNotNull(fetchedProduct);
        Assert.assertEquals(updatedProduct.getName(), addedProduct.getName());
        Assert.assertEquals(updatedProduct.getName(), fetchedProduct.getName());
        Assert.assertEquals(fetchedProduct.getCategory().getId(), addedProduct.getCategory().getId());
        Assert.assertEquals(fetchedProduct.getCategory().getId(), updatedProduct.getCategory().getId());
    }

    @Test
    public void should_throw_ex_when_update_product_with_invalid_params_given() {
        ProductsController controller = new ProductsController(productService);
        Product addedProduct = addProductWithController(controller);
        String blankDescription = "    ";
        addedProduct.setDescription(blankDescription); // intentionally blank desciption

        Product fetchedProductBeforeUpdate = controller.findProduct(addedProduct.getId());

        Exception exception = null;
        Product updatedProduct = null;
        try {
            updatedProduct = controller.updateProduct(addedProduct.getId(), addedProduct);
        } catch (Exception e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertNull(updatedProduct);

        Product fetchedProductAfterUpdate = controller.findProduct(addedProduct.getId());
        Assert.assertNotEquals(blankDescription, fetchedProductAfterUpdate.getDescription());
        Assert.assertEquals(fetchedProductBeforeUpdate.getDescription(), fetchedProductAfterUpdate.getDescription());
    }

    @Test
    public void should_throw_exception_when_trying_update_with_empty_category() {
        ProductsController controller = new ProductsController(productService);
        Product addedProduct = addProductWithController(controller);
        addedProduct.getCategory().setId(null); // intentionally null

        Product fetchedProductBeforeUpdate = controller.findProduct(addedProduct.getId());

        Exception exception = null;
        Product updatedProduct = null;
        try {
            updatedProduct = controller.updateProduct(addedProduct.getId(), addedProduct);
        } catch (Exception e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertNull(updatedProduct);
        Assert.assertTrue(exception instanceof CategoryNotFoundException);

        Product fetchedProductAfterUpdate = controller.findProduct(addedProduct.getId());
        Assert.assertNotNull(fetchedProductAfterUpdate.getCategory().getId());
    }

    @Test
    public void should_throw_exception_when_trying_update_with_invalid_category() {
        ProductsController controller = new ProductsController(productService);
        Product addedProduct = addProductWithController(controller);
        addedProduct.getCategory().setId(Long.valueOf(99999999));

        Product fetchedProductBeforeUpdate = controller.findProduct(addedProduct.getId());

        Exception exception = null;
        Product updatedProduct = null;
        try {
            updatedProduct = controller.updateProduct(addedProduct.getId(), addedProduct);
        } catch (Exception e) {
            exception = e;
        }

        Assert.assertNotNull(exception);
        Assert.assertNull(updatedProduct);
        Assert.assertTrue(exception instanceof CategoryNotFoundException);

        Product fetchedProductAfterUpdate = controller.findProduct(addedProduct.getId());
        Assert.assertNotNull(fetchedProductAfterUpdate.getCategory().getId());
    }

    private Product addProductWithController(ProductsController productsController) {
        return productsController.addProduct(product);
    }
}
