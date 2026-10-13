import { describe, expect, it } from "vitest";
import { getPluralForm } from "./getPluralForm";

const forms: [string, string, string] = ["товар", "товара", "товаров"];

describe("Склонение по числу", () => {
    it("Для чисел, оканчивающихся на 1, кроме 11, выбирает первую форму", () => {
        expect(getPluralForm(1, forms)).toBe("товар");
        expect(getPluralForm(21, forms)).toBe("товар");
    });

    it("Для чисел, оканчивающихся на 2–4, кроме 12–14, выбирает вторую форму", () => {
        expect(getPluralForm(3, forms)).toBe("товара");
        expect(getPluralForm(42, forms)).toBe("товара");
    });

    it("Для остальных чисел выбирает третью форму", () => {
        expect(getPluralForm(0, forms)).toBe("товаров");
        expect(getPluralForm(11, forms)).toBe("товаров");
        expect(getPluralForm(14, forms)).toBe("товаров");
        expect(getPluralForm(25, forms)).toBe("товаров");
    });
});
