package com.sparta.myselectshop.dto;

import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.ProductFolder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class ProductResponseDto {
	private Long id;
	private String title;
	private String link;
	private String image;
	private int lprice;
	private int myprice;

	private List<FolderResponseDto> productFolderList = new ArrayList<>();

	public ProductResponseDto(Product product) {
		this.id = product.getId();
		this.title = product.getTitle();
		this.link = product.getLink();
		this.image = product.getImage();
		this.lprice = product.getLprice();
		this.myprice = product.getMyprice();
		// Folder의 정보를 담기 위한 반복문 - 지연 로딩을 사용해야 함으로 Service 단에 Transaction 환경을 주입해야 함
		for (ProductFolder productFolder : product.getProductFolderList()) {
			productFolderList.add(new FolderResponseDto(productFolder.getFolder()));
		}
	}
}