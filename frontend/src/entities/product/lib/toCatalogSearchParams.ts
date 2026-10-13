import type { ProductsParams } from "../model/types";

export function toCatalogSearchParams(params: ProductsParams): URLSearchParams {
    const searchParams = new URLSearchParams();

    if (params.category) searchParams.set("category", params.category);
    params.brand?.forEach(brand => searchParams.append("brand", brand));
    params.color?.forEach(color => searchParams.append("color", color));
    if (params.minPrice !== undefined)
        searchParams.set("minPrice", String(params.minPrice));
    if (params.maxPrice !== undefined)
        searchParams.set("maxPrice", String(params.maxPrice));
    if (params.inStock) searchParams.set("inStock", "true");
    if (params.sale) searchParams.set("sale", "true");
    if (params.sort && params.sort !== "new") searchParams.set("sort", params.sort);
    if (params.page && params.page > 1) searchParams.set("page", String(params.page));

    return searchParams;
}
