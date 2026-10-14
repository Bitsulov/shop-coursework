import { z } from "zod";

export const profileFormSchema = z.object({
    name: z
        .string()
        .trim()
        .min(2, "Имя должно быть от 2 до 50 символов")
        .max(50, "Имя должно быть от 2 до 50 символов"),
    phone: z
        .string()
        .transform(value => value.replace(/[\s()-]/g, ""))
        .refine(value => value === "" || /^\+[1-9]\d{9,14}$/.test(value), {
            message: "Телефон в формате +7 999 123-45-67"
        })
});

export type ProfileFormInput = z.input<typeof profileFormSchema>;
export type ProfileFormValues = z.output<typeof profileFormSchema>;
