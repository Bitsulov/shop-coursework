import { api } from "shared/api/instance";
import type { CartResponse } from "./types";

export async function getCart() {
    const response = await api.get<CartResponse>("/cart");
    return response.data;
}

export async function setCartProductQuantity({
    productUuid,
    quantity
}: {
    productUuid: string;
    quantity: number;
}) {
    const response = await api.put<CartResponse>(`/cart/products/${productUuid}`, {
        quantity
    });
    return response.data;
}

export async function removeCartProduct(productUuid: string) {
    const response = await api.delete<CartResponse>(`/cart/products/${productUuid}`);
    return response.data;
}
