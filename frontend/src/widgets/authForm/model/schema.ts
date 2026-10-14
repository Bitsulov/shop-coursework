import { z } from "zod";

export const authFormSchema = z.object({
    email: z.string().trim().min(1, "Введите email").email("Неверный формат email"),
    password: z.string().min(1, "Введите пароль")
});

export type AuthFormValues = z.infer<typeof authFormSchema>;
