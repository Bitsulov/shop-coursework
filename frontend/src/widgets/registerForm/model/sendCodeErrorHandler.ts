import axios from "axios";

export function sendCodeErrorHandler(error: unknown): string {
    if (!axios.isAxiosError(error) || !error.response) {
        return "Не удалось связаться с сервером. Проверьте подключение к интернету.";
    }

    switch (error.response.status) {
        case 400:
            return "Новый код можно запросить через 2 минуты после предыдущего.";
        case 409:
            return "Почта уже подтверждена. Войдите в аккаунт.";
        default:
            return "Не удалось отправить код. Попробуйте ещё раз чуть позже.";
    }
}
