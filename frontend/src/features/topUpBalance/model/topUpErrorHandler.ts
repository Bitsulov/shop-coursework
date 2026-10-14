import axios from "axios";

export function topUpErrorHandler(error: unknown): string {
    if (!axios.isAxiosError(error) || !error.response) {
        return "Не удалось связаться с сервером. Проверьте подключение к интернету.";
    }

    switch (error.response.status) {
        case 402:
            return "Банк отклонил платёж. Проверьте данные или используйте другую карту.";
        case 400:
            return "Срок действия карты истёк.";
        case 409:
            return "Сумма на балансе превысит допустимый предел.";
        default:
            return "Не удалось пополнить баланс. Попробуйте ещё раз чуть позже.";
    }
}
