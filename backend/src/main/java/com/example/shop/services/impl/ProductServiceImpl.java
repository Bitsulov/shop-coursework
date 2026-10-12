package com.example.shop.services.impl;

import com.example.shop.dtos.product.ProductRequest;
import com.example.shop.dtos.product.ProductResponse;
import com.example.shop.dtos.product.ProductSpecRequest;
import com.example.shop.exceptions.ResourceNotFoundException;
import com.example.shop.mappers.ProductMapper;
import com.example.shop.models.entities.Brand;
import com.example.shop.models.entities.Category;
import com.example.shop.models.entities.Product;
import com.example.shop.models.entities.ProductSpec;
import com.example.shop.repositories.BrandRepository;
import com.example.shop.repositories.CategoryRepository;
import com.example.shop.repositories.ProductRepository;
import com.example.shop.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;
    private final ImageUploaderService imageUploaderService;

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getByUuid(UUID uuid) {
        Product product = productRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.warn("Getting product failed: product not found, uuid={}", uuid);
                    return new ResourceNotFoundException("Product not found");
                });

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request, MultipartFile image) {
        boolean hasImage = image != null && !image.isEmpty();
        if (hasImage && request.isDeleteImage()) {
            log.warn("Product creation warn: new image sent together with image deletion");
            throw new IllegalArgumentException("Cannot upload and delete image at the same time");
        }

        Product product = productMapper.toEntity(request);
        product.setUuid(UUID.randomUUID());
        applyRelations(request, product);

        Instant now = Instant.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        productRepository.saveAndFlush(product);

        if (hasImage) {
            product.setImageUrl(imageUploaderService.uploadImage(image));
        }

        log.info("Product created, uuid={}", product.getUuid());
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse update(UUID uuid, ProductRequest request, MultipartFile image) {
        boolean hasImage = image != null && !image.isEmpty();
        if (hasImage && request.isDeleteImage()) {
            log.warn("Product update warn: new image sent together with image deletion, uuid={}", uuid);
            throw new IllegalArgumentException("Cannot upload and delete image at the same time");
        }

        Product product = productRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.warn("Product update failed: product not found, uuid={}", uuid);
                    return new ResourceNotFoundException("Product not found");
                });
        String oldImageUrl = product.getImageUrl();

        productMapper.updateEntity(request, product);
        applyRelations(request, product);
        product.setUpdatedAt(Instant.now());
        productRepository.saveAndFlush(product);

        if (hasImage) {
            product.setImageUrl(imageUploaderService.uploadImage(image));
        } else if (request.isDeleteImage()) {
            product.setImageUrl(null);
        }

        if (oldImageUrl != null && (hasImage || request.isDeleteImage())) {
            deleteImageQuietly(oldImageUrl, uuid);
        }

        log.info("Product updated, uuid={}", uuid);
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public void delete(UUID uuid) {
        Product product = productRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.warn("Product deletion failed: product not found, uuid={}", uuid);
                    return new ResourceNotFoundException("Product not found");
                });
        String imageUrl = product.getImageUrl();

        productRepository.delete(product);
        productRepository.flush();

        if (imageUrl != null) {
            deleteImageQuietly(imageUrl, uuid);
        }

        log.info("Product deleted, uuid={}", uuid);
    }

    private void applyRelations(ProductRequest request, Product product) {
        Category category = categoryRepository.findByName(request.getCategory())
                .orElseThrow(() -> new IllegalStateException("Category is missing in the database: " + request.getCategory()));
        product.setCategory(category);
        product.setBrand(findOrCreateBrand(request.getBrand()));
        product.setColor(normalizeColor(request.getColor()));

        List<ProductSpec> specs = product.getSpecs();
        specs.clear();
        List<ProductSpecRequest> specRequests = request.getSpecs();
        for (int i = 0; i < specRequests.size(); i++) {
            ProductSpecRequest specRequest = specRequests.get(i);
            ProductSpec spec = new ProductSpec();
            spec.setProduct(product);
            spec.setName(specRequest.getName().strip());
            spec.setValue(specRequest.getValue().strip());
            spec.setPosition(i);
            specs.add(spec);
        }
    }

    private Brand findOrCreateBrand(String rawName) {
        String name = rawName.strip();
        return brandRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Brand brand = new Brand();
                    brand.setName(name);
                    Brand saved = brandRepository.save(brand);
                    log.info("Brand created, name={}", name);
                    return saved;
                });
    }

    private String normalizeColor(String rawColor) {
        if (rawColor == null) {
            return null;
        }
        String color = rawColor.strip().toLowerCase(Locale.ROOT);
        return color.substring(0, 1).toUpperCase(Locale.ROOT) + color.substring(1);
    }

    private void deleteImageQuietly(String imageUrl, UUID uuid) {
        try {
            imageUploaderService.deleteImage(imageUrl);
        } catch (RuntimeException ex) {
            log.error("Product image was not deleted from storage, url={}, uuid={}", imageUrl, uuid, ex);
        }
    }
}
