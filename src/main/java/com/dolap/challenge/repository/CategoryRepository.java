package com.dolap.challenge.repository;

import com.dolap.challenge.entity.Category;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("FROM Category c WHERE c.parentCategory IS NULL")
    public List<Category> findAllSkipChildren(Sort sort);
}
