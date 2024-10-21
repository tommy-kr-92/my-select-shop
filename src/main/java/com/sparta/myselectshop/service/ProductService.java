package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.entity.UserRoleEnum;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;

	public static final int MIN_MY_PRICE = 100;

	// 저장
	public ProductResponseDto createProduct(ProductRequestDto requestDto, User user) {
		Product product = productRepository.save(new Product(requestDto, user));
		return new ProductResponseDto(product);
	}

	@Transactional	// dirty checking을 위한 트랜잭션 적용
	public ProductResponseDto updateProduct(Long id, ProductMypriceRequestDto requestDto) {
		int myprice = requestDto.getMyprice();
		if (myprice < MIN_MY_PRICE) {
			throw new IllegalArgumentException("유효하지 않은 관심 가격입니다. 최소 " + MIN_MY_PRICE + "원 이상으로 설정해 주세요.");
		}

		Product product = productRepository.findById(id).orElseThrow(() ->
				new NullPointerException("해당 상품을 찾을 수 없습니다.")
		);

		product.update(requestDto);

		return new ProductResponseDto(product);
	}

	@Transactional(readOnly = true)	// 조회 성능을 높이기 위해 readOnly Option 사용
	public Page<ProductResponseDto> getProducts(User user, int page, int size, String sortBy, boolean isAsc) {
		// 정렬 전처리
		Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
		Sort sort = Sort.by(direction, sortBy);
		Pageable pageable = PageRequest.of(page, size, sort);

		UserRoleEnum userRoleEnum = user.getRole();

		Page<Product> productList;

		// 권한에 따른 다른 메서드 호출
		if(userRoleEnum == UserRoleEnum.USER){
			productList = productRepository.findAllByUser(user, pageable);
		} else {
			productList = productRepository.findAll(pageable);
		}
		
		// Page 타입은 converter 인 map 메서드 지원
		return productList.map(ProductResponseDto::new);
	}

	@Transactional
	public void updateBySearch(Long id, ItemDto itemDto) {
		Product product = productRepository.findById(id).orElseThrow(()->
				new NullPointerException("해당 상품은 존재하지 않습니다.")
				);
		product.updateByItemDto(itemDto);
	}
}
