package com.shubh.JPATutorial.Module3.repositories;

import com.shubh.JPATutorial.Module3.entities.ProductEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// the implementation for various methods is defined in SimpleJpaRepository class
// which implements JpaRepositoryImplementation interface
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity,Long> {
    // hibernate auto generates the method implementation based on method name
    // field name should be used here in method name(to follow camel-case)
    // instead of database column name

    // from spring data jpa documentation method naming rules
    List<ProductEntity> findByTitle(String title);

    // find records created after certain data
    List<ProductEntity> findByCreatedAtAfter(LocalDateTime dateTime);

    // find records by quantity and price
    List<ProductEntity> findByQuantityAndPriceCurrent(Integer qty, BigDecimal price);

    // find records by quantity and price with conditions
    // strict comparison > and <
    List<ProductEntity> findByQuantityGreaterThanAndPriceCurrentLessThan(Integer qty, BigDecimal price);

    // find records by quantity and price with conditions
    // strict comparison > and <
    List<ProductEntity> findByQuantityGreaterThanOrPriceCurrentLessThan(Integer qty, BigDecimal price);

    // wildcard search
    List<ProductEntity> findByTitleLike(String pattern);

    // granular search : internally uses wildcard
    // case-sensitive: but mysql database collation is case-insensitive by defaukt
    List<ProductEntity> findByTitleContaining(String word);

    // Return single entity: This should only be used for fields with a UNIQUE constraint
    // (like @Id or @Column(unique = true)). If the query returns more than one
    // record, Spring Data JPA will throw a NonUniqueResultException.

    // we can return the entity if it is found then we return it otherwise
    // we return null, if we don't want to worry about null pointer exceptions
    // then we can simply return an optional of Product Entity
    // hibernate will take care of returning in the correct format
    Optional<ProductEntity> findByTitleAndPriceCurrent(String title,BigDecimal priceCurrent);


    // JPQL (Java Persistence Query Language)
    // Uses '?' followed by a number (index) to map method parameters into the query.
    // JPQL operates on Java Entities and Field names, not Database Table/Column names.
    // 'select e' indicates we are returning the entire ProductEntity object.
    @Query("select e from ProductEntity e where e.title = ?1 and e.priceCurrent = ?2")
    Optional<ProductEntity> findByTitleAndPrice(String title, BigDecimal price);
    // NOTE: Defining manual queries doesn't require following Hibernate method naming
    // conventions because the query is not being auto-generated from the name.


    // Named Parameters: Uses ':' followed by the parameter name.
    // RULE: The name after ':' MUST match the method parameter name
    // unless you use @Param("customName") to explicitly map them.
    // This approach is more robust as it doesn't depend on the order of arguments.
    @Query("select e from ProductEntity e where e.title = :title and e.priceCurrent = :price")
    Optional<ProductEntity> findByTitleAndPrice1(String title, BigDecimal price);

    // Named Parameters: Uses ':' followed by the parameter name.
    // RULE: The name after ':' MUST match the method parameter name
    // unless you use @Param("customName") to explicitly map them.
    // This approach is more robust as it doesn't depend on the order of arguments.
    @Query("select e from ProductEntity e where e.title = :t and e.priceCurrent = :pCurrent")
    Optional<ProductEntity> findByTitleAndPrice2(@Param("t")String title,@Param("pCurrent") BigDecimal price);

    // Sorting

    List<ProductEntity> findByTitleOrderByPriceCurrent(String title);

    // since we are not finding on any field we have to name it findBy
    // sorting by descending price
    List<ProductEntity> findByOrderByPriceCurrentDesc();

    // For any new query pattern which requires sorting we are currently
    // defining a sort implementation, so if we have numerous such requirements
    // the interface would become bloated. So instead we can use a sort parameter
    // to optionally sort based on usecase

    // It also helps reduce tight coupling with sorting parameters
    // we can sort using any parameters which is a more flexible approach

    List<ProductEntity> findBy(Sort sort);

    // ignore case in title search
    // adding pagination
    List<ProductEntity> findByTitleContainingIgnoreCase(String word,Pageable page);

}


