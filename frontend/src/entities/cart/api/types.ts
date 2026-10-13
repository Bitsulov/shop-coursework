import type { ProductCardResponse } from "entities/product";

export interface CartProductResponse {
    product: ProductCardResponse;
    quantity: number;
    total: number;
}

export interface CartResponse {
    products: CartProductResponse[];
    totalQuantity: number;
    subtotal: number;
    discount: number;
    total: number;
}
