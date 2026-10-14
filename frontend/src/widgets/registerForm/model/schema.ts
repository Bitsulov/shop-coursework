import { z } from "zod";

export const passwordRules = [
    { text: "Не короче 8 символов", test: (value: string) => value.length >= 8 },
    { text: "Хотя бы одна буква", test: (value: string) => /\p{L}/u.test(value) },
    { text: "Хотя бы одна цифра", test: (value: string) => /\d/.test(value) }
];

export const registerFormSchema = z
    .object({
        name: z
            .string()
            .trim()
            .min(2, "Имя должно быть от 2 до 50 символов")
            .max(50, "Имя должно быть от 2 до 50 символов"),
        email: z.string().trim().min(1, "Введите email").email("Неверный формат email"),
        phone: z
            .string()
            .transform(value => value.replace(/[\s()-]/g, ""))
            .refine(value => value === "" || /^\+[1-9]\d{9,14}$/.test(value), {
                message: "Телефон в формате +7 999 123-45-67"
            }),
        password: z
            .string()
            .max(72, "Пароль не длиннее 72 символов")
            .refine(value => passwordRules.every(rule => rule.test(value)), {
                message: "Пароль не соответствует требованиям"
            }),
        confirmPassword: z.string().min(1, "Повторите пароль")
    })
    .refine(values => values.password === values.confirmPassword, {
        path: ["confirmPassword"],
        message: "Пароли не совпадают"
    });

export type RegisterFormInput = z.input<typeof registerFormSchema>;
export type RegisterFormValues = z.output<typeof registerFormSchema>;
