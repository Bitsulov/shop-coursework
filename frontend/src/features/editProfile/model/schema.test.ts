import { describe, expect, it } from "vitest";
import { profileFormSchema } from "./schema";

describe("Проверка формы профиля", () => {
    it("Принимает имя без телефона", () => {
        expect(profileFormSchema.parse({ name: "Анна", phone: "" })).toEqual({
            name: "Анна",
            phone: ""
        });
    });

    it("Приводит телефон к международному формату без пробелов", () => {
        expect(
            profileFormSchema.parse({ name: "Анна", phone: "+7 999 123-45-67" }).phone
        ).toBe("+79991234567");
    });

    it("Отклоняет слишком короткое имя и неверный телефон", () => {
        const result = profileFormSchema.safeParse({ name: "А", phone: "12345" });
        const errors = result.success ? {} : result.error.flatten().fieldErrors;

        expect(errors.name).toEqual(["Имя должно быть от 2 до 50 символов"]);
        expect(errors.phone).toEqual(["Телефон в формате +7 999 123-45-67"]);
    });
});
