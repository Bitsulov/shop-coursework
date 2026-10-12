package com.example.shop.repositories;

import com.example.shop.dtos.product.ProductFilter;
import com.example.shop.models.entities.Product;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> matches(ProductFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.getCategory() != null) {
                predicates.add(cb.equal(root.get("category").get("name"), filter.getCategory()));
            }
            if (filter.getBrands() != null && !filter.getBrands().isEmpty()) {
                List<String> brands = filter.getBrands().stream()
                        .map(brand -> brand.strip().toLowerCase(Locale.ROOT))
                        .toList();
                predicates.add(cb.lower(root.get("brand").get("name")).in(brands));
            }
            if (filter.getColors() != null && !filter.getColors().isEmpty()) {
                predicates.add(root.get("color").in(filter.getColors()));
            }
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }
            if (filter.isInStock()) {
                predicates.add(cb.greaterThan(root.get("stock"), 0));
            }
            if (filter.isSale()) {
                predicates.add(cb.isNotNull(root.get("oldPrice")));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<Product> orderedByDiscount() {
        return (root, query, cb) -> {
            if (query != null && !Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                Expression<BigDecimal> oldPrice = root.get("oldPrice");
                Expression<BigDecimal> price = root.get("price");
                Expression<Number> discount = cb.quot(cb.diff(oldPrice, price), oldPrice);
                query.orderBy(
                        cb.desc(cb.coalesce(discount, 0)),
                        cb.desc(root.get("createdAt")),
                        cb.desc(root.get("id")));
            }
            return null;
        };
    }
}
