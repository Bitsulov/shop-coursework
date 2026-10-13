import type { AxiosInstance } from "axios";
import { api } from "shared/api/instance";
import type { Page } from "shared/lib/types";
import type { Category } from "../model/categories";
import type { ProductsParams } from "../model/types";
import type {
    ProductCardResponse,
    ProductFiltersResponse,
    ProductResponse
} from "./types";

export async function getProducts(params: ProductsParams, client: AxiosInstance = api) {
    const response = await client.get<Page<ProductCardResponse>>("/products", {
        params,
        paramsSerializer: { indexes: null }
    });
    return response.data;
}

export async function getProductFilters(
    category?: Category,
    client: AxiosInstance = api
) {
    const response = await client.get<ProductFiltersResponse>("/products/filters", {
        params: { category }
    });
    return response.data;
}

export async function getProduct(uuid: string, client: AxiosInstance = api) {
    const response = await client.get<ProductResponse>(`/products/${uuid}`);
    return response.data;
}

export async function getRelatedProducts(uuid: string, client: AxiosInstance = api) {
    const response = await client.get<ProductCardResponse[]>(`/products/${uuid}/related`);
    return response.data;
}
