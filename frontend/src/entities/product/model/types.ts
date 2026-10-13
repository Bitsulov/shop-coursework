import type { Category } from "./categories";

export type ProductSort = "new" | "price_asc" | "price_desc" | "discount";

export interface ProductsParams {
    category?: Category;
    brand?: string[];
    color?: string[];
    minPrice?: number;
    maxPrice?: number;
    inStock?: boolean;
    sale?: boolean;
    sort?: ProductSort;
    page?: number;
    size?: number;
}
