import { data } from "react-router";
import axios from "axios";
import { getProduct } from "entities/product";
import { createServerInstance } from "shared/api/serverInstance";
import type { Route } from "./+types/product";

export async function loader({ params }: Route.LoaderArgs) {
    try {
        await getProduct(params.uuid, createServerInstance());
    } catch (error) {
        const status = axios.isAxiosError(error) ? error.response?.status : undefined;
        if (status === 404 || status === 400) {
            throw data(null, { status: 404 });
        }
    }
    return null;
}

export function meta() {
    return [{ title: "Товар — Shop" }];
}

export { Product as default } from "pages/product";
