import { describe, expect, it } from "vitest";
import { addressFormSchema } from "./schema";

const valid = {
    city: "Москва",
    street: "Тверская",
    house: "5к2",
    apartment: "",
    postalCode: "",
    comment: ""
};

function errorsOf(values: Partial<typeof valid>) {
    const result = addressFormSchema.safeParse({ ...valid, ...values });
    return result.success ? {} : result.error.flatten().fieldErrors;
}

describe("Проверка формы адреса", () => {
    it("Принимает адрес без необязательных полей", () => {
        expect(addressFormSchema.safeParse(valid).success).toBe(true);
    });

    it("Требует город, улицу и дом", () => {
        const errors = errorsOf({ city: " ", street: "", house: "" });

        expect(errors.city).toEqual(["Укажите город"]);
        expect(errors.street).toEqual(["Укажите улицу"]);
        expect(errors.house).toEqual(["Укажите дом"]);
    });

    it("Отклоняет индекс не из 6 цифр", () => {
        expect(errorsOf({ postalCode: "12345" }).postalCode).toEqual([
            "Индекс из 6 цифр"
        ]);
    });
});
