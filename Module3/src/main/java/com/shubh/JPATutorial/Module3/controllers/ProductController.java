package com.shubh.JPATutorial.Module3.controllers;

import com.shubh.JPATutorial.Module3.entities.ProductEntity;
import com.shubh.JPATutorial.Module3.repositories.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/products")
public class ProductController {
    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<ProductEntity> getAllProducts(@RequestParam(defaultValue = "id") String sortBy) {
        // sort by provided parameter, maintainable approach
        // prevents bloated repositories
        // if we want to sort by a specific direction, then we can also provide
        // another parameter specifying direction inside sort by
//        return productRepository.findBy(Sort.by(Sort.Direction.DESC,sortBy));
//        we can also pass order object to get fine-grained control over sorting
        // sort by multiple params, tie breakers etc.
        return productRepository.findBy(Sort.by(Sort.Order.desc(sortBy),
                Sort.Order.asc("priceCurrent"))); // price is the tie breaker

    }
}