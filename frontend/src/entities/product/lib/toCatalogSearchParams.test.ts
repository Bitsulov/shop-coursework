import { describe, expect, it } from "vitest";
import { toCatalogSearchParams } from "./toCatalogSearchParams";
import { parseCatalogParams } from "./parseCatalogParams";

describe("Параметры каталога в адресе страницы", () => {
    it("Не записывает значения по умолчанию", () => {
        expect(
            toCatalogSearchParams({ sort: "new", page: 1, inStock: false }).toString()
        ).toBe("");
    });

    it("После разбора адреса получаются те же параметры", () => {
        const params = {
            category: "tv" as const,
            brand: ["Vela", "Nord"],
            color: ["Белый"],
            minPrice: 1000,
            maxPrice: 5000,
            inStock: true,
            sale: true,
            sort: "discount" as const,
            page: 2
        };

        expect(parseCatalogParams(toCatalogSearchParams(params))).toEqual(params);
    });
});
