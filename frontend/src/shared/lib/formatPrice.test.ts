import { describe, expect, it } from "vitest";
import { formatPrice } from "./formatPrice";

describe("Форматирование цены", () => {
    it("Разделяет разряды пробелом и добавляет знак рубля", () => {
        expect(formatPrice(164990).replace(/\s/g, " ")).toBe("164 990 ₽");
    });

    it("Сохраняет копейки, если они есть", () => {
        expect(formatPrice(99.5).replace(/\s/g, " ")).toBe("99,5 ₽");
    });
});
