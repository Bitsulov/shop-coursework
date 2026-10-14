import { describe, expect, it } from "vitest";
import { createTopUpSchema } from "./schema";

const schema = createTopUpSchema(() => new Date(2026, 9, 14));

const valid = {
    amount: "5000",
    cardNumber: "4000 0000 0000 0001",
    expiry: "12/27",
    cvc: "123"
};

function errorsOf(values: Partial<typeof valid>) {
    const result = schema.safeParse({ ...valid, ...values });
    return result.success ? {} : result.error.flatten().fieldErrors;
}

describe("Проверка формы пополнения", () => {
    it("Превращает сумму в число и убирает пробелы из номера карты", () => {
        expect(schema.parse(valid)).toEqual({
            amount: 5000,
            cardNumber: "4000000000000001",
            expiry: "12/27",
            cvc: "123"
        });
    });

    it("Отклоняет сумму меньше 100 и больше 500 000", () => {
        expect(errorsOf({ amount: "99" }).amount).toEqual(["Сумма от 100 до 500 000 ₽"]);
        expect(errorsOf({ amount: "500001" }).amount).toEqual([
            "Сумма от 100 до 500 000 ₽"
        ]);
    });

    it("Отклоняет номер карты не из 16 цифр", () => {
        expect(errorsOf({ cardNumber: "4000 0000" }).cardNumber).toEqual([
            "Номер карты из 16 цифр"
        ]);
    });

    it("Принимает карту, которая действует до конца текущего месяца", () => {
        expect(errorsOf({ expiry: "10/26" }).expiry).toBeUndefined();
    });

    it("Отклоняет истёкшую карту и неверный формат срока", () => {
        expect(errorsOf({ expiry: "09/26" }).expiry).toEqual([
            "Срок действия карты истёк"
        ]);
        expect(errorsOf({ expiry: "13/27" }).expiry).toEqual(["Срок в формате ММ/ГГ"]);
    });
});
