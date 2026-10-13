import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { http, HttpResponse } from "msw";
import { api } from "./instance";
import { decryptFromString, encryptToString } from "../lib/crypto";
import { getCookieByName } from "../lib/getCookieByName";
import { server } from "../tests/server";
import { clearCookies } from "../tests/clearCookies";
import { generateTestKey } from "../tests/generateTestKey";

describe("Клиентский экземпляр axios", () => {
    let key: string;

    beforeEach(async () => {
        key = await generateTestKey();
        vi.stubEnv("VITE_CRYPTO_KEY", key);
        clearCookies();
    });

    afterEach(() => {
        vi.unstubAllEnvs();
        clearCookies();
    });

    function captureAuthorization() {
        const received: (string | null)[] = [];
        server.use(
            http.get("*/api/protected", ({ request }) => {
                received.push(request.headers.get("Authorization"));
                return HttpResponse.json({});
            })
        );
        return received;
    }

    it("Подставляет расшифрованный access-токен в заголовок Authorization", async () => {
        const received = captureAuthorization();
        document.cookie = `accessToken=${await encryptToString("access-token", key)}; path=/`;

        await api.get("/protected");

        expect(received).toEqual(["Bearer access-token"]);
    });

    it("Не подставляет заголовок Authorization, если токенов нет", async () => {
        const received = captureAuthorization();

        await api.get("/protected");

        expect(received).toEqual([null]);
    });

    it("Не подставляет токен в запросы входа", async () => {
        let receivedAuth: string | null = "не вызывался";
        server.use(
            http.post("*/api/auth/login", ({ request }) => {
                receivedAuth = request.headers.get("Authorization");
                return HttpResponse.json({});
            })
        );
        document.cookie = `accessToken=${await encryptToString("access-token", key)}; path=/`;

        await api.post("/auth/login", {});

        expect(receivedAuth).toBeNull();
    });

    it("Обновляет токены по refresh-токену, если access-токена нет", async () => {
        const received = captureAuthorization();
        server.use(
            http.post("*/api/auth/refresh", () =>
                HttpResponse.json({
                    accessToken: "new-access",
                    refreshToken: "new-refresh",
                    accessTokenExpiresIn: 60_000,
                    refreshTokenExpiresIn: 120_000
                })
            )
        );
        document.cookie = `refreshToken=${await encryptToString("old-refresh", key)}; path=/`;

        await api.get("/protected");

        expect(received).toEqual(["Bearer new-access"]);
        expect(await decryptFromString(getCookieByName("accessToken") ?? "", key)).toBe(
            "new-access"
        );
        expect(await decryptFromString(getCookieByName("refreshToken") ?? "", key)).toBe(
            "new-refresh"
        );
    });

    it("Удаляет cookie с токенами, если refresh-токен отклонён", async () => {
        const received = captureAuthorization();
        server.use(
            http.post("*/api/auth/refresh", () =>
                HttpResponse.json({ statusCode: 401 }, { status: 401 })
            )
        );
        document.cookie = `refreshToken=${await encryptToString("old-refresh", key)}; path=/`;

        await api.get("/protected");

        expect(received).toEqual([null]);
        expect(getCookieByName("refreshToken")).toBeNull();
    });
});
