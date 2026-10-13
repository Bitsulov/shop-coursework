import { describe, expect, it } from "vitest";
import { decryptFromString, encryptToString } from "./crypto";
import { generateTestKey } from "../tests/generateTestKey";

describe("Шифрование строк", () => {
    it("Расшифрованная строка совпадает с исходной", async () => {
        const key = await generateTestKey();

        const encrypted = await encryptToString("access-token", key);

        expect(await decryptFromString(encrypted, key)).toBe("access-token");
    });

    it("Зашифрованная строка не содержит символов, недопустимых в cookie", async () => {
        const key = await generateTestKey();

        const encrypted = await encryptToString("a".repeat(100), key);

        expect(encrypted).not.toMatch(/[+/=]/);
    });

    it("Одна и та же строка каждый раз шифруется по-разному", async () => {
        const key = await generateTestKey();

        const first = await encryptToString("token", key);
        const second = await encryptToString("token", key);

        expect(first).not.toBe(second);
    });

    it("Строку нельзя расшифровать другим ключом", async () => {
        const encrypted = await encryptToString("token", await generateTestKey());

        await expect(
            decryptFromString(encrypted, await generateTestKey())
        ).rejects.toThrow();
    });
});
