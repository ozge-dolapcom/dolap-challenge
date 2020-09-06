package com.dolap.challenge.controller;

import com.dolap.challenge.entity.Product;
import com.dolap.challenge.exception.ProductNotFoundException;
import com.dolap.challenge.service.ProductService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductsControllerTest {

    @Autowired
    private ProductService productService;

    private Product product;

    @Before
    public void setup() {
        product = new Product();
        product.setName("The greatest product");
        product.setDescription("Some random product description goes here");
        product.setRemainingStockCount(102);
        product.setPrice(new BigDecimal("45.98"));
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

//    @Test
//    public void should_get_products() {
//        int productSize = 30;
//        ProductsController controller = new ProductsController(productService);
//        for(int i = 1; i <= productSize; i++) {
//            Product product = new Product();
//            product.setName("Test product name " + i);
//            product.setDescription("Test product desc " + i);
//            product.setRemainingStockCount(i);
//            product.setPrice(new BigDecimal(i * 10));
//
//            controller.addProduct(product);
//            // ignore result
//        }
//
//        String sortBy = "id";
//        String sortOrder = "asc";
//        Integer page = 0;
//        Integer limit = 2;
//        Page<Product> responseProducts = controller.getProducts(Long.valueOf(1), sortBy, sortOrder, page, limit);
//        Assert.assertNotNull(responseProducts);
//        Assert.assertEquals(limit, Integer.valueOf(responseProducts.getContent().size()));
//        Assert.assertFalse(responseProducts.isLast());
//
//        // since limit=2, only 2 products
//        Product product1 = responseProducts.getContent().get(0);
//        Product product2 = responseProducts.getContent().get(responseProducts.getContent().size() - 1);
//
//        Assert.assertTrue(product1.getId() < product2.getId()); // check sort asc
//    }

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

        Product updatedProduct = controller.updateProduct(addedProduct.getId(), addedProduct);
        Product fetchedProduct = controller.findProduct(addedProduct.getId());

        Assert.assertNotNull(updatedProduct);
        Assert.assertNotNull(fetchedProduct);
        Assert.assertEquals(updatedProduct.getName(), addedProduct.getName());
        Assert.assertEquals(updatedProduct.getName(), fetchedProduct.getName());
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

    private Product addProductWithController(ProductsController productsController) {
        return productsController.addProduct(product);
    }
}
