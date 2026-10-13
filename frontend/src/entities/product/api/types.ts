import type { Category } from "../model/categories";

export interface ProductCardResponse {
    uuid: string;
    name: string;
    summary: string;
    price: number;
    oldPrice: number | null;
    stock: number;
    imageUrl: string | null;
    createdAt: string;
}

export interface ProductSpecResponse {
    name: string;
    value: string;
}

export interface ProductResponse {
    uuid: string;
    name: string;
    summary: string;
    description: string | null;
    category: Category;
    brand: string;
    color: string | null;
    price: number;
    oldPrice: number | null;
    stock: number;
    imageUrl: string | null;
    specs: ProductSpecResponse[];
    createdAt: string;
    updatedAt: string;
}

export interface ProductFiltersResponse {
    brands: string[];
    colors: string[];
}
