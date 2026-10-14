import { describe, expect, it } from "vitest";
import { AxiosError, AxiosHeaders } from "axios";
import { verifyErrorHandler } from "./verifyErrorHandler";

function responseError(status: number) {
    return new AxiosError("error", "ERR", undefined, undefined, {
        status,
        statusText: "",
        headers: {},
        config: { headers: new AxiosHeaders() },
        data: {}
    });
}

describe("Ошибки подтверждения кода", () => {
    it("Сообщает о неверном коде при статусе 400", () => {
        expect(verifyErrorHandler(responseError(400))).toBe(
            "Неверный код. Проверьте цифры из письма."
        );
    });

    it("Сообщает об истёкшем коде при статусе 410", () => {
        expect(verifyErrorHandler(responseError(410))).toBe(
            "Код истёк. Отправьте новый."
        );
    });

    it("Сообщает о превышении попыток при статусе 403", () => {
        expect(verifyErrorHandler(responseError(403))).toBe(
            "Слишком много попыток. Отправьте новый код."
        );
    });

    it("Сообщает о проблеме с соединением, если ответа нет", () => {
        expect(verifyErrorHandler(new AxiosError("Network Error"))).toBe(
            "Не удалось связаться с сервером. Проверьте подключение к интернету."
        );
    });
});
