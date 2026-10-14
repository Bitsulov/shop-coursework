import { describe, expect, it } from "vitest";
import { AxiosError, AxiosHeaders } from "axios";
import { orderErrorHandler } from "./orderErrorHandler";

function responseError(status: number, message: string) {
    return new AxiosError("error", "ERR", undefined, undefined, {
        status,
        statusText: "",
        headers: {},
        config: { headers: new AxiosHeaders() },
        data: { message }
    });
}

describe("Ошибки оформления заказа", () => {
    it("Сообщает о нехватке денег на балансе", () => {
        expect(orderErrorHandler(responseError(409, "Not enough balance"))).toBe(
            "На балансе не хватает денег для оплаты."
        );
    });

    it("Сообщает о нехватке товара на складе", () => {
        expect(orderErrorHandler(responseError(409, "Not enough stock"))).toBe(
            "Части товаров уже нет в нужном количестве. Проверьте корзину."
        );
    });

    it("Сообщает о пустой корзине", () => {
        expect(orderErrorHandler(responseError(409, "Cart is empty"))).toBe(
            "Корзина пуста."
        );
    });

    it("Сообщает об общей ошибке при других ответах", () => {
        expect(orderErrorHandler(responseError(500, "Unexpected error occurred"))).toBe(
            "Не удалось оформить заказ. Попробуйте ещё раз чуть позже."
        );
    });
});
