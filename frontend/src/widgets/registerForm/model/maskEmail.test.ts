import { describe, expect, it } from "vitest";
import { maskEmail } from "./maskEmail";

describe("Маскирование email", () => {
    it("Оставляет первые две буквы имени и домен", () => {
        expect(maskEmail("anna@example.com")).toBe("an••@example.com");
    });

    it("Скрывает хотя бы один символ у короткого имени", () => {
        expect(maskEmail("a@example.com")).toBe("a•@example.com");
    });
});
