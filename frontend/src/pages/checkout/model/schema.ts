import { z } from "zod";

export const recipientSchema = z.object({
    recipientName: z
        .string()
        .trim()
        .min(2, "Имя должно быть от 2 до 50 символов")
        .max(50, "Имя должно быть от 2 до 50 символов"),
    recipientPhone: z
        .string()
        .transform(value => value.replace(/[\s()-]/g, ""))
        .refine(value => /^\+[1-9]\d{9,14}$/.test(value), {
            message: "Телефон в формате +7 999 123-45-67"
        })
});

export type RecipientInput = z.input<typeof recipientSchema>;
export type RecipientValues = z.output<typeof recipientSchema>;
