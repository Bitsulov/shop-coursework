import axios from "axios";

export function verifyErrorHandler(error: unknown): string {
    if (!axios.isAxiosError(error) || !error.response) {
        return "Не удалось связаться с сервером. Проверьте подключение к интернету.";
    }

    switch (error.response.status) {
        case 400:
            return "Неверный код. Проверьте цифры из письма.";
        case 410:
            return "Код истёк. Отправьте новый.";
        case 403:
            return "Слишком много попыток. Отправьте новый код.";
        default:
            return "Что-то пошло не так. Попробуйте ещё раз чуть позже.";
    }
}
