package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.*;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.FolderRepository;
import com.sparta.myselectshop.repository.ProductFolderRepository;
import com.sparta.myselectshop.repository.ProductRepository;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final FolderRepository folderRepository;
	private final ProductFolderRepository productFolderRepository;

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

	public void addFolder(Long productId, Long folderId, User user) {

		// product 검증
		Product product = productRepository.findById(productId).orElseThrow(
				() -> new NullPointerException("해당 상품이 존재하지 않습니다.")
		);

		// folder 검증
		Folder folder = folderRepository.findById(folderId).orElseThrow(
				() -> new NullPointerException("해당 폴더가 존재하지 않습니다.")
		);

		// user 검증
		if(!product.getUser().getId().equals(user.getId())
		|| !folder.getUser().getId().equals(user.getId())){
			throw new IllegalArgumentException("회원님의 관심상품이 아니거나, 회원님의 폴더가 아닙니다.");
		}

		Optional<ProductFolder> overlapFolder = productFolderRepository.findByProductAndFolder(product, folder);

		if(overlapFolder.isPresent()){
			throw new IllegalArgumentException("중복된 폴더입니다.");
		}

		productFolderRepository.save(new ProductFolder(product, folder));
	}

	public Page<ProductResponseDto> getProductsInFolder(Long folderId, int page, int size, String sortBy, boolean isAsc, User user) {
		// 정렬 전처리
		Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
		Sort sort = Sort.by(direction, sortBy);
		Pageable pageable = PageRequest.of(page, size, sort);

		Page<Product> productList = productRepository.findAllByUserAndProductFolderList_FolderId(user, folderId, pageable);

		// Product -> ProductResponseDto Convert
		Page<ProductResponseDto> responseDtoList = productList.map(ProductResponseDto::new);

		return responseDtoList;
	}
}
