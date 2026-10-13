import clsx from "clsx";
import type { ProductCardResponse } from "entities/product";
import { ProductCard } from "./productCard";
import { ProductCardSkeleton } from "./productCardSkeleton";
import styles from "./productGrid.module.scss";

interface ProductGridProps {
    products?: ProductCardResponse[];
    isLoading?: boolean;
    skeletonCount?: number;
    wide?: boolean;
}

export function ProductGrid({
    products = [],
    isLoading = false,
    skeletonCount = 8,
    wide = false
}: ProductGridProps) {
    return (
        <div
            className={clsx(styles.grid, wide && styles.wide)}
            aria-busy={isLoading}
            aria-label={isLoading ? "Загрузка товаров" : undefined}
        >
            {isLoading
                ? Array.from({ length: skeletonCount }, (_, index) => (
                      <ProductCardSkeleton key={index} />
                  ))
                : products.map(product => (
                      <ProductCard key={product.uuid} product={product} />
                  ))}
        </div>
    );
}
