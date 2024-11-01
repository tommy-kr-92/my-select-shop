package com.sparta.myselectshop.controller;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.security.UserDetailsImpl;
import com.sparta.myselectshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProductController {

	private final ProductService productService;

	// Create Product
	@PostMapping("/products")
	public ProductResponseDto createProduct(@RequestBody ProductRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails){
		return productService.createProduct(requestDto, userDetails.getUser());
	}

	// Update Product
	@PutMapping("/products/{id}")
	public ProductResponseDto updateProduct(@PathVariable Long id, @RequestBody ProductMypriceRequestDto requestDto){
		return productService.updateProduct(id, requestDto);
	}

	// Get Product -> Page Object 사용
	// page, size, sortBy, ASC/DESC - 현재 페이지, 전체 페이지, 정렬 기준, 정렬 방식
	// 하지만 page 는 0부터 시작하기 때문에 -1 을 한 후 Service 단으로 보내줘야함
	@GetMapping("/products")
	public Page<ProductResponseDto> getProducts(
			@RequestParam("page") int page,
			@RequestParam("size") int size,
			@RequestParam("sortBy") String sortBy,
			@RequestParam("isAsc") boolean isAsc,
			@AuthenticationPrincipal UserDetailsImpl userDetails){
		return productService.getProducts(userDetails.getUser(),
				page-1,	// 페이지는 0부터 시작됨
				size,
				sortBy,
				isAsc
		);
	}

	// Add Folder
	@PostMapping("/products/{productId}/folder")
	public void addFolder(
			@PathVariable Long productId,
			@RequestParam Long folderId,
			@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		productService.addFolder(productId, folderId, userDetails.getUser());
	}

	// Get Product To Folder
	@GetMapping("/folders/{folderId}/products")
	public Page<ProductResponseDto> getProductsInFolder(
			@PathVariable Long folderId,
			@RequestParam("page") int page,
			@RequestParam("size") int size,
			@RequestParam("sortBy") String sortBy,
			@RequestParam("isAsc") boolean isAsc,
			@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		return productService.getProductsInFolder(
				folderId,
				page-1,
				size,
				sortBy,
				isAsc,
				userDetails.getUser()
		);
	}
}
