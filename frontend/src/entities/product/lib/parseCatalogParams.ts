import { isCategory } from "../model/categories";
import type { ProductSort, ProductsParams } from "../model/types";

const sorts: ProductSort[] = ["new", "price_asc", "price_desc", "discount"];

function parsePrice(value: string | null): number | undefined {
    if (!value) return undefined;
    const price = Number(value);
    return Number.isFinite(price) && price >= 0 ? price : undefined;
}

export function parseCatalogParams(searchParams: URLSearchParams): ProductsParams {
    const category = searchParams.get("category");
    const sort = searchParams.get("sort");
    const page = Number(searchParams.get("page"));

    return {
        category: isCategory(category) ? category : undefined,
        brand: searchParams.getAll("brand"),
        color: searchParams.getAll("color"),
        minPrice: parsePrice(searchParams.get("minPrice")),
        maxPrice: parsePrice(searchParams.get("maxPrice")),
        inStock: searchParams.get("inStock") === "true",
        sale: searchParams.get("sale") === "true",
        sort: sorts.includes(sort as ProductSort) ? (sort as ProductSort) : "new",
        page: Number.isInteger(page) && page > 1 ? page : 1
    };
}
