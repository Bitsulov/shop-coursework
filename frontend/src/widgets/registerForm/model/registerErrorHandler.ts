import axios from "axios";

export function registerErrorHandler(error: unknown): string {
    if (!axios.isAxiosError(error) || !error.response) {
        return "Не удалось связаться с сервером. Проверьте подключение к интернету.";
    }

    switch (error.response.status) {
        case 409:
            return "Пользователь с таким email уже зарегистрирован.";
        case 400:
            return error.response.data?.message?.startsWith("Please wait")
                ? "Код уже отправлен. Новый можно запросить через 2 минуты."
                : "Проверьте введённые данные.";
        default:
            return "Что-то пошло не так. Попробуйте ещё раз чуть позже.";
    }
}
