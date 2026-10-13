import { describe, expect, it } from "vitest";
import { parseCatalogParams } from "./parseCatalogParams";

describe("Параметры каталога из адреса страницы", () => {
    it("Без параметров возвращает сортировку по новизне и первую страницу", () => {
        expect(parseCatalogParams(new URLSearchParams())).toEqual({
            category: undefined,
            brand: [],
            color: [],
            minPrice: undefined,
            maxPrice: undefined,
            inStock: false,
            sale: false,
            sort: "new",
            page: 1
        });
    });

    it("Читает все фильтры, включая несколько брендов и цветов", () => {
        const params = parseCatalogParams(
            new URLSearchParams(
                "category=tv&brand=Vela&brand=Nord&color=Чёрный&minPrice=1000&maxPrice=5000&inStock=true&sale=true&sort=price_asc&page=3"
            )
        );

        expect(params).toEqual({
            category: "tv",
            brand: ["Vela", "Nord"],
            color: ["Чёрный"],
            minPrice: 1000,
            maxPrice: 5000,
            inStock: true,
            sale: true,
            sort: "price_asc",
            page: 3
        });
    });

    it("Отбрасывает неизвестную категорию, сортировку и неверные числа", () => {
        const params = parseCatalogParams(
            new URLSearchParams(
                "category=cars&sort=popular&minPrice=abc&maxPrice=-5&page=0"
            )
        );

        expect(params.category).toBeUndefined();
        expect(params.sort).toBe("new");
        expect(params.minPrice).toBeUndefined();
        expect(params.maxPrice).toBeUndefined();
        expect(params.page).toBe(1);
    });
});
