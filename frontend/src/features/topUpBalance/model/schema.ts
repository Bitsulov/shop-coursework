import { z } from "zod";

function isExpiryInFuture(value: string, now: Date) {
    const [month, year] = value.split("/").map(Number);
    const lastDay = new Date(2000 + year, month, 0, 23, 59, 59);
    return lastDay >= now;
}

export function createTopUpSchema(now: () => Date = () => new Date()) {
    return z.object({
        amount: z
            .string()
            .transform(value => Number(value.replace(/\s/g, "")))
            .refine(value => Number.isInteger(value) && value >= 100 && value <= 500000, {
                message: "Сумма от 100 до 500 000 ₽"
            }),
        cardNumber: z
            .string()
            .transform(value => value.replace(/\s/g, ""))
            .refine(value => /^\d{16}$/.test(value), {
                message: "Номер карты из 16 цифр"
            }),
        expiry: z
            .string()
            .refine(value => /^(0[1-9]|1[0-2])\/\d{2}$/.test(value), {
                message: "Срок в формате ММ/ГГ"
            })
            .refine(
                value => !/^\d{2}\/\d{2}$/.test(value) || isExpiryInFuture(value, now()),
                {
                    message: "Срок действия карты истёк"
                }
            ),
        cvc: z.string().refine(value => /^\d{3}$/.test(value), {
            message: "CVC из 3 цифр"
        })
    });
}

export type TopUpFormInput = z.input<ReturnType<typeof createTopUpSchema>>;
export type TopUpFormValues = z.output<ReturnType<typeof createTopUpSchema>>;
