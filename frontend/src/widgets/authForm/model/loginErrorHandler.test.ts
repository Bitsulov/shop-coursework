import { describe, expect, it } from "vitest";
import { AxiosError, AxiosHeaders } from "axios";
import { loginErrorHandler } from "./loginErrorHandler";

function responseError(status: number) {
    return new AxiosError("error", "ERR", undefined, undefined, {
        status,
        statusText: "",
        headers: {},
        config: { headers: new AxiosHeaders() },
        data: {}
    });
}

describe("Ошибки входа", () => {
    it("Сообщает о неверных данных при статусе 401", () => {
        expect(loginErrorHandler(responseError(401))).toBe("Неверный email или пароль.");
    });

    it("Сообщает о недоступном входе при статусе 403", () => {
        expect(loginErrorHandler(responseError(403))).toBe(
            "Вход недоступен: аккаунт заблокирован или почта не подтверждена."
        );
    });

    it("Сообщает о неизвестной ошибке при других статусах", () => {
        expect(loginErrorHandler(responseError(500))).toBe(
            "Что-то пошло не так. Попробуйте ещё раз чуть позже."
        );
    });
});
