import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { getTokenFromRequest } from "./getTokenFromRequest";
import { encryptToString } from "./crypto";
import { generateTestKey } from "../tests/generateTestKey";

function makeRequest(cookie?: string) {
    return new Request("http://localhost/", cookie ? { headers: { cookie } } : {});
}

describe("Токен из запроса страницы", () => {
    let key: string;

    beforeEach(async () => {
        key = await generateTestKey();
        vi.stubEnv("VITE_CRYPTO_KEY", key);
    });

    afterEach(() => {
        vi.unstubAllEnvs();
    });

    it("Возвращается расшифрованный access-токен из cookie", async () => {
        const encrypted = await encryptToString("access-token", key);

        const token = await getTokenFromRequest(
            makeRequest(`theme=light; accessToken=${encrypted}`)
        );

        expect(token).toBe("access-token");
    });

    it("Возвращается null, если в запросе нет cookie", async () => {
        expect(await getTokenFromRequest(makeRequest())).toBeNull();
    });

    it("Возвращается null, если cookie с токеном нет", async () => {
        expect(await getTokenFromRequest(makeRequest("theme=light"))).toBeNull();
    });

    it("Возвращается null, если токен зашифрован другим ключом", async () => {
        const encrypted = await encryptToString("access-token", await generateTestKey());

        expect(
            await getTokenFromRequest(makeRequest(`accessToken=${encrypted}`))
        ).toBeNull();
    });
});
