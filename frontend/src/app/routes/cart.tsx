import { redirect } from "react-router";
import { hasRefreshToken } from "shared/lib/hasRefreshToken";
import type { Route } from "./+types/cart";

export function loader({ request }: Route.LoaderArgs) {
    if (!hasRefreshToken(request)) {
        throw redirect("/login?redirect=%2Fcart");
    }
    return null;
}

export function meta() {
    return [{ title: "Корзина — Shop" }];
}

export { Cart as default } from "pages/cart";
