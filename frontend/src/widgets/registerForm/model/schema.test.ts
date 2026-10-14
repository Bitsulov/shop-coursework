import { describe, expect, it } from "vitest";
import { registerFormSchema } from "./schema";

const valid = {
    name: "Анна",
    email: "anna@example.com",
    phone: "",
    password: "secret123",
    confirmPassword: "secret123"
};

function errorsOf(values: Partial<typeof valid>) {
    const result = registerFormSchema.safeParse({ ...valid, ...values });
    return result.success ? {} : result.error.flatten().fieldErrors;
}

describe("Проверка формы регистрации", () => {
    it("Принимает заполненную форму без телефона", () => {
        expect(registerFormSchema.safeParse(valid).success).toBe(true);
    });

    it("Убирает пробелы, скобки и дефисы из телефона", () => {
        const result = registerFormSchema.parse({
            ...valid,
            phone: "+7 (999) 123-45-67"
        });

        expect(result.phone).toBe("+79991234567");
    });

    it("Отклоняет телефон без кода страны", () => {
        expect(errorsOf({ phone: "89991234567" }).phone).toEqual([
            "Телефон в формате +7 999 123-45-67"
        ]);
    });

    it("Отклоняет пароль без цифры", () => {
        expect(
            errorsOf({ password: "onlyletters", confirmPassword: "onlyletters" }).password
        ).toEqual(["Пароль не соответствует требованиям"]);
    });

    it("Отклоняет несовпадающие пароли", () => {
        expect(errorsOf({ confirmPassword: "secret124" }).confirmPassword).toEqual([
            "Пароли не совпадают"
        ]);
    });

    it("Отклоняет имя короче двух символов", () => {
        expect(errorsOf({ name: "А" }).name).toEqual([
            "Имя должно быть от 2 до 50 символов"
        ]);
    });
});
