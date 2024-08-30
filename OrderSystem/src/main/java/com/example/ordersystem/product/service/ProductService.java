package com.example.ordersystem.product.service;

import com.example.ordersystem.common.service.StockInventoryService;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.dto.ProductCreateRqDto;
import com.example.ordersystem.product.dto.ProductListRsDto;
import com.example.ordersystem.product.dto.ProductSearchDto;
import com.example.ordersystem.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final S3Client s3Client;
    private final StockInventoryService stockInventoryService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Transactional
    public Product createProduct(ProductCreateRqDto request) {
        MultipartFile image = request.getProductImage();
        Product product = null;
        try {
            product = productRepository.save(request.toEntity());
            byte[] imageByte = image.getBytes();
            Path imagePath = Paths.get("/tmp",
                    product.getId() + "_" + image.getOriginalFilename());
            Files.write(imagePath, imageByte, StandardOpenOption.CREATE ,StandardOpenOption.WRITE);
            product.updateImagePath(imagePath.toString());

            if (request.getName().contains("sale")) {
                stockInventoryService.increaseStock(product.getId(), request.getStockQuantity());
            }

            return product;
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패");
        }
    }

    public Page<ProductListRsDto> productList(ProductSearchDto searchDto, Pageable pageable){
        // 검색을 위해 Specification 객체를 사용함.
        // 복잡한 쿼리를 명세를 이용해 정의하는 방식으로, 쿼리를 쉽게 생성하게 해준다.
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if(searchDto.getSearchName() != null){
                    // root : DB Column 조회. Entity 의 속성에 접근하기 위한 객체.
                    // CriteriaBuilder : 쿼리를 생성하기 위한 객체.
                    predicates.add(criteriaBuilder.like(root.get("name"), "%" + searchDto.getSearchName() + "%"));
                }
                if(searchDto.getCategory() != null){
                    predicates.add(criteriaBuilder.like(root.get("category"), "%" + searchDto.getCategory() + "%"));
                }
                Predicate[] predicateArr = new Predicate[predicates.size()];
                for(int i = 0; i < predicateArr.length; i++){
                    predicateArr[i] = predicates.get(i);
                    // 위 2개의 쿼리 조건문을 and 조건으로 연결.
                    // 우리는 지금 name 검색 / category 검색 나눠놔서 독립적이지만, and 로 엮여도 상관 없다.
                    // 추후 and 가 필요할 수 있으니 미리 작성하였음.
                }Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };
        Page<Product> products = productRepository.findAll(specification, pageable);
        Page<ProductListRsDto> resDtos = products.map(a->a.fromEntity());
        return resDtos;

    }


    @Transactional
    public Product createAwsProduct(ProductCreateRqDto request) {
        MultipartFile image = request.getProductImage();
        Product product = null;
        try {
            product = productRepository.save(request.toEntity());
            byte[] imageByte = image.getBytes();
            String fileName = product.getId() + "_" + UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
//            로컬 PC에 임시 저장
            Path imagePath = Paths.get("/Users/jeonsangmin/Downloads/tmp",fileName);
            Files.write(imagePath, imageByte, StandardOpenOption.CREATE ,StandardOpenOption.WRITE);

//            AWS에 pc에 저장된 파일을 업로드
            PutObjectRequest putObjectAclRequest = PutObjectRequest.builder()
                    .bucket(bucket) // 버킷명
                    .key(fileName) // 파일명
                    .build();
            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectAclRequest, RequestBody.fromFile(imagePath));
            String s3Path = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(fileName)).toExternalForm();
            product.updateImagePath(s3Path);
            return product;
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패");
        }
    }
}
