import { api } from "shared/api/instance";
import type { CartResponse } from "./types";

export async function getCart() {
    const response = await api.get<CartResponse>("/cart");
    return response.data;
}
