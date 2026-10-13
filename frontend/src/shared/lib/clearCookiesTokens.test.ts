import { beforeEach, describe, expect, it } from "vitest";
import { clearCookiesTokens } from "./clearCookiesTokens";
import { getCookieByName } from "./getCookieByName";
import { clearCookies } from "../tests/clearCookies";

describe("Удаление cookie с токенами", () => {
    beforeEach(() => {
        clearCookies();
    });

    it("Удаляет токены и сроки их действия", () => {
        document.cookie = "accessToken=access; path=/";
        document.cookie = "accessTokenExpiresAt=123; path=/";
        document.cookie = "refreshToken=refresh; path=/";
        document.cookie = "refreshTokenExpiresAt=456; path=/";

        clearCookiesTokens();

        expect(getCookieByName("accessToken")).toBeNull();
        expect(getCookieByName("accessTokenExpiresAt")).toBeNull();
        expect(getCookieByName("refreshToken")).toBeNull();
        expect(getCookieByName("refreshTokenExpiresAt")).toBeNull();
    });

    it("Не затрагивает другие cookie", () => {
        document.cookie = "other=value; path=/";

        clearCookiesTokens();

        expect(getCookieByName("other")).toBe("value");
    });
});
