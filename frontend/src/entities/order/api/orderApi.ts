import { api } from "shared/api/instance";
import type { OrderRequest, OrderResponse } from "./types";

export async function createOrder(data: OrderRequest) {
    const response = await api.post<OrderResponse>("/orders", data);
    return response.data;
}
