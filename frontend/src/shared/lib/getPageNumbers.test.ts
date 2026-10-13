import { describe, expect, it } from "vitest";
import { getPageNumbers } from "./getPageNumbers";

describe("Номера страниц для пагинации", () => {
    it("Показывает все страницы, если их не больше семи", () => {
        expect(getPageNumbers(3, 5)).toEqual([1, 2, 3, 4, 5]);
    });

    it("В начале списка пропускает страницы перед последней", () => {
        expect(getPageNumbers(1, 20)).toEqual([1, 2, 3, 4, 5, null, 20]);
    });

    it("В середине списка показывает соседние страницы и пропуски с обеих сторон", () => {
        expect(getPageNumbers(10, 20)).toEqual([1, null, 9, 10, 11, null, 20]);
    });

    it("В конце списка пропускает страницы после первой", () => {
        expect(getPageNumbers(20, 20)).toEqual([1, null, 16, 17, 18, 19, 20]);
    });
});
