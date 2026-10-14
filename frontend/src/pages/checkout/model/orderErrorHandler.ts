import axios from "axios";

export function orderErrorHandler(error: unknown): string {
    if (!axios.isAxiosError(error) || !error.response) {
        return "Не удалось связаться с сервером. Проверьте подключение к интернету.";
    }

    const message: string = error.response.data?.message ?? "";
    if (error.response.status === 409) {
        if (message.includes("balance")) return "На балансе не хватает денег для оплаты.";
        if (message.includes("stock")) {
            return "Части товаров уже нет в нужном количестве. Проверьте корзину.";
        }
        if (message.includes("empty")) return "Корзина пуста.";
    }
    return "Не удалось оформить заказ. Попробуйте ещё раз чуть позже.";
}
