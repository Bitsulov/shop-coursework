import { describe, expect, it } from "vitest";
import { validateAvatar } from "./validateAvatar";

function makeFile(type: string, size: number) {
    return new File([new Uint8Array(size)], "photo", { type });
}

describe("Проверка фото профиля", () => {
    it("Принимает JPG, PNG и WebP до 5 МБ", () => {
        expect(validateAvatar(makeFile("image/jpeg", 1024))).toBeNull();
        expect(validateAvatar(makeFile("image/png", 1024))).toBeNull();
        expect(validateAvatar(makeFile("image/webp", 5 * 1024 * 1024))).toBeNull();
    });

    it("Отклоняет другие форматы", () => {
        expect(validateAvatar(makeFile("image/gif", 1024))).toBe(
            "Подойдёт фото в формате JPG, PNG или WebP"
        );
    });

    it("Отклоняет файлы больше 5 МБ", () => {
        expect(validateAvatar(makeFile("image/png", 5 * 1024 * 1024 + 1))).toBe(
            "Фото должно быть не больше 5 МБ"
        );
    });
});
