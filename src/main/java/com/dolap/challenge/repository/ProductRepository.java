package com.dolap.challenge.repository;

import com.dolap.challenge.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "FROM Product p WHERE p.category.id IN :categoryList")
    public Page<Product> findAllByCategory(@Param("categoryList") List<Long> ids, Pageable pageable);
}
