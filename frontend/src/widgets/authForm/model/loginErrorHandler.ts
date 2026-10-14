import axios from "axios";

export function loginErrorHandler(error: unknown): string {
    if (!axios.isAxiosError(error) || !error.response) {
        return "Не удалось связаться с сервером. Проверьте подключение к интернету.";
    }

    switch (error.response.status) {
        case 401:
            return "Неверный email или пароль.";
        case 403:
            return "Вход недоступен: аккаунт заблокирован или почта не подтверждена.";
        default:
            return "Что-то пошло не так. Попробуйте ещё раз чуть позже.";
    }
}
