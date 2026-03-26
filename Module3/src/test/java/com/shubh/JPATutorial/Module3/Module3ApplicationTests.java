package com.shubh.JPATutorial.Module3;

import com.shubh.JPATutorial.Module3.entities.ProductEntity;
import com.shubh.JPATutorial.Module3.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@SpringBootTest
class Module3ApplicationTests {
// Field injection is used here because JUnit requires a no-args constructor
// to instantiate the test class. Marking these as 'final' would require
// constructor injection, which isn't the default behavior for Spring tests.

	@Autowired
	private ProductRepository productRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void testRepository(){
		ProductEntity productEntity = ProductEntity.builder()
				.sku("Kitkat_1234")
				.title("Kitkat Mega Pack")
				.priceCurrent(BigDecimal.valueOf(134.12))
				.quantity(12)
				.build();
		ProductEntity savedProduct = productRepository.save(productEntity);
		System.out.println(savedProduct);
	}

	@Test
	void getRepository(){
		List<ProductEntity> entityList = productRepository.findAll();
		System.out.println(entityList);
	}

	@Test
	void customRepositoryMethodTest(){
		List<ProductEntity> entityList = productRepository.findByTitle("Pepsi");
		System.out.println(entityList);
	}

	@Test
	void afterDateTest(){
		ProductEntity productEntity = ProductEntity.builder()
				.sku("FIveStar_12")
				.title("FiveStar Crunchy")
				.priceCurrent(BigDecimal.valueOf(134.12))
				.quantity(10)
				.build();
		productRepository.save(productEntity);
		List<ProductEntity> entityList = productRepository.findByCreatedAtAfter(
				LocalDateTime.of(2024,1,1,0,0,0));
		System.out.println(entityList);
	}

	@Test
	void quantityPriceExactMatchTest(){
		ProductEntity productEntity = ProductEntity.builder()
				.sku("FIveStar_13")
				.title("FiveStar Crunchy Big")
				.priceCurrent(BigDecimal.valueOf(134.12))
				.quantity(10)
				.build();

		productRepository.save(productEntity);

		List<ProductEntity> entityList = productRepository.findByQuantityAndPriceCurrent(10,BigDecimal.valueOf(134.12));
						System.out.println(entityList);
	}

	@Test
	void quantityPriceFilterValueTest1(){
		ProductEntity productEntity1 = ProductEntity.builder()
				.sku("FIveStar_1532")
				.title("FiveStar Crunchy 4423")
				.priceCurrent(BigDecimal.valueOf(90))
				.quantity(5)
				.build();

		ProductEntity productEntity2 = ProductEntity.builder()
				.sku("new_p123")
				.title("Maggi432")
				.priceCurrent(BigDecimal.valueOf(45))
				.quantity(11)
				.build();


		productRepository.save(productEntity1);
		productRepository.save(productEntity2);

		List<ProductEntity> entityList = productRepository.findByQuantityGreaterThanAndPriceCurrentLessThan(4,BigDecimal.valueOf(100));
		System.out.println(entityList);
	}

	@Test
	void quantityPriceFilterValueTest2(){
		ProductEntity productEntity1 = ProductEntity.builder()
				.sku("FIveStar_15342")
				.title("FiveStar Crunchy Hello")
				.priceCurrent(BigDecimal.valueOf(90))
				.quantity(4)
				.build();

		ProductEntity productEntity2 = ProductEntity.builder()
				.sku("423_MAggi")
				.title("Maggi")
				.priceCurrent(BigDecimal.valueOf(45))
				.quantity(11)
				.build();


		productRepository.save(productEntity1);
		productRepository.save(productEntity2);

		List<ProductEntity> entityList = productRepository.findByQuantityGreaterThanOrPriceCurrentLessThan(5,BigDecimal.valueOf(100));
		System.out.println(entityList);
	}

	@Test
	void findByTitleWildCardTest(){
		List<ProductEntity> entityList = productRepository.findByTitleLike("%choco%");
		System.out.println(entityList);
	}

	@Test
	void findByTitleContainingTest(){
		// case sensitive search, but mysql by default has case-insensitive collation
		// so getting results using the query
		List<ProductEntity> entityList = productRepository.findByTitleContaining("Ocolat");
		System.out.println(entityList);
	}

	@Test
	void findByTitleContainingIgnoreCaseTest(){
		List<ProductEntity> entityList = productRepository.findByTitleContainingIgnoreCase("Ocolat");
		System.out.println(entityList);
	}

	@Test
	void findByTitlePriceUniqueTest(){
		Optional<ProductEntity> entity = productRepository.findByTitleAndPriceCurrent("Mountain Dew",BigDecimal.valueOf(40));

		// graceful null handling
		// if present then print the entity
		// if present takes in a function(lambda) in this case that needs to
		// execute
//		entity.ifPresent(x->System.out.println(x));
		entity.ifPresent(System.out::println); // using method inference instead of this
	}

	@Test
	void findByTitlePriceJPQLQueryTest(){
		Optional<ProductEntity> entity = productRepository.findByTitleAndPrice("Mountain Dew",BigDecimal.valueOf(40));

		// Alternate implementations for undrstanding
//		Optional<ProductEntity> entity = productRepository.findByTitleAndPrice1("Mountain Dew",BigDecimal.valueOf(40));
//		Optional<ProductEntity> entity = productRepository.findByTitleAndPrice2("Mountain Dew",BigDecimal.valueOf(40));

		// graceful null handling
		// if present then print the entity
		// if present takes in a function(lambda) in this case that needs to
		// execute
		entity.ifPresent(x->System.out.println(x));
//		entity.ifPresent(System.out::println); // using method inference instead of this
	}


}
