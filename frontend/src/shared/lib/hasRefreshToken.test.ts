import { describe, expect, it } from "vitest";
import { hasRefreshToken } from "./hasRefreshToken";

function makeRequest(cookie?: string) {
    return new Request("http://localhost/", cookie ? { headers: { cookie } } : {});
}

describe("Проверка сессии в запросе страницы", () => {
    it("Находит refresh-токен среди других cookie", () => {
        expect(hasRefreshToken(makeRequest("theme=light; refreshToken=abc"))).toBe(true);
    });

    it("Не считает сессией пустой refresh-токен", () => {
        expect(hasRefreshToken(makeRequest("refreshToken="))).toBe(false);
    });

    it("Не путает refresh-токен с похожими cookie", () => {
        expect(hasRefreshToken(makeRequest("refreshTokenExpiresAt=123"))).toBe(false);
    });

    it("Возвращает false без cookie", () => {
        expect(hasRefreshToken(makeRequest())).toBe(false);
    });
});
