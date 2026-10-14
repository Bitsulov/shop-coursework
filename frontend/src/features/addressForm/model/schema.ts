import { z } from "zod";

const optionalText = (max: number, message: string) =>
    z.string().trim().max(max, message);

export const addressFormSchema = z.object({
    city: z
        .string()
        .trim()
        .min(1, "Укажите город")
        .max(100, "Город не длиннее 100 символов"),
    street: z
        .string()
        .trim()
        .min(1, "Укажите улицу")
        .max(150, "Улица не длиннее 150 символов"),
    house: z.string().trim().min(1, "Укажите дом").max(20, "Дом не длиннее 20 символов"),
    apartment: optionalText(20, "Квартира не длиннее 20 символов"),
    postalCode: z
        .string()
        .trim()
        .refine(value => value === "" || /^\d{6}$/.test(value), {
            message: "Индекс из 6 цифр"
        }),
    comment: optionalText(255, "Комментарий не длиннее 255 символов")
});

export type AddressFormValues = z.infer<typeof addressFormSchema>;
