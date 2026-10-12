package com.example.shop.services.impl;

import com.example.shop.dtos.common.PageResponse;
import com.example.shop.dtos.product.ProductCardResponse;
import com.example.shop.dtos.product.ProductFilter;
import com.example.shop.dtos.product.ProductFiltersResponse;
import com.example.shop.dtos.product.ProductRequest;
import com.example.shop.dtos.product.ProductResponse;
import com.example.shop.dtos.product.ProductSort;
import com.example.shop.dtos.product.ProductSpecRequest;
import com.example.shop.exceptions.ResourceNotFoundException;
import com.example.shop.mappers.ProductMapper;
import com.example.shop.models.entities.Brand;
import com.example.shop.models.entities.Category;
import com.example.shop.models.entities.Product;
import com.example.shop.models.entities.ProductSpec;
import com.example.shop.models.enums.CategoryEnum;
import com.example.shop.repositories.BrandRepository;
import com.example.shop.repositories.CategoryRepository;
import com.example.shop.repositories.ProductRepository;
import com.example.shop.repositories.ProductSpecifications;
import com.example.shop.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final BigDecimal MAX_PRICE = new BigDecimal("9999999999.99");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;
    private final ImageUploaderService imageUploaderService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductCardResponse> search(ProductFilter filter, ProductSort sort, int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page cannot be negative");
        }
        if (size < 1 || size > 48) {
            throw new IllegalArgumentException("Page size must be from 1 to 48");
        }
        if ((long) page * size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Page is too large");
        }
        checkPrice(filter.getMinPrice(), "Min price");
        checkPrice(filter.getMaxPrice(), "Max price");
        if (filter.getMinPrice() != null && filter.getMaxPrice() != null
                && filter.getMinPrice().compareTo(filter.getMaxPrice()) > 0) {
            throw new IllegalArgumentException("Min price cannot be greater than max price");
        }

        Specification<Product> specification = ProductSpecifications.matches(filter);
        PageRequest pageRequest = switch (sort) {
            case NEW -> PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
            case PRICE_ASC -> PageRequest.of(page, size, Sort.by(Sort.Order.asc("price"), Sort.Order.desc("id")));
            case PRICE_DESC -> PageRequest.of(page, size, Sort.by(Sort.Order.desc("price"), Sort.Order.desc("id")));
            case DISCOUNT -> {
                specification = specification.and(ProductSpecifications.orderedByDiscount());
                yield PageRequest.of(page, size);
            }
        };

        Page<ProductCardResponse> products = productRepository.findAll(specification, pageRequest)
                .map(productMapper::toCardResponse);
        return PageResponse.of(products);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductFiltersResponse getFilters(CategoryEnum category) {
        ProductFiltersResponse response = new ProductFiltersResponse();
        response.setBrands(productRepository.findBrandNames(category));
        response.setColors(productRepository.findColors(category));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductCardResponse> getRelated(UUID uuid) {
        Product product = productRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.warn("Getting related products failed: product not found, uuid={}", uuid);
                    return new ResourceNotFoundException("Product not found");
                });

        List<Product> related = productRepository.findTop4ByCategoryAndIdNotOrderByCreatedAtDescIdDesc(
                product.getCategory(), product.getId());
        return productMapper.toCardResponses(related);
    }

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

    private void checkPrice(BigDecimal price, String name) {
        if (price != null && (price.signum() < 0 || price.scale() > 2 || price.compareTo(MAX_PRICE) > 0)) {
            throw new IllegalArgumentException(name + " must be from 0 to 9999999999.99 with up to 2 decimals");
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
