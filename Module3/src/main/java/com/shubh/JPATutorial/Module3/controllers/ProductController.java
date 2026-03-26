package com.shubh.JPATutorial.Module3.controllers;

import com.shubh.JPATutorial.Module3.entities.ProductEntity;
import com.shubh.JPATutorial.Module3.repositories.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/products")
public class ProductController {
    // default values to fetch per page: good practice to keep a default
    // and not expose this as a param for clients
    private final Integer PAGE_SIZE = 4;
    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public Page<ProductEntity> getAllProducts(@RequestParam(defaultValue = "id") String sortBy, @RequestParam(defaultValue = "0") Integer pageNumber) {
//        // sort by provided parameter, maintainable approach
//        // prevents bloated repositories
//        // if we want to sort by a specific direction, then we can also provide
//        // another parameter specifying direction inside sort by
////        return productRepository.findBy(Sort.by(Sort.Direction.DESC,sortBy));
////        we can also pass order object to get fine-grained control over sorting
//        // sort by multiple params, tie breakers etc.
//        return productRepository.findBy(Sort.by(Sort.Order.desc(sortBy),
//                Sort.Order.asc("priceCurrent"))); // price is the tie breaker

//        // Pagination: getting chunks of data
//        PageRequest pageRequest = PageRequest.of(pageNumber,PAGE_SIZE);

        // sorting also possible with page request
        // Pages use 0 based indexing
        Pageable pageable = PageRequest.of(pageNumber,PAGE_SIZE,Sort.by(Sort.Direction.DESC,sortBy));
        // returns a Page<ProductEntity>
        return productRepository.findAll(pageable);

    }

}