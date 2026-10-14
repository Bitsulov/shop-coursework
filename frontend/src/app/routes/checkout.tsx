import { redirect } from "react-router";
import { hasRefreshToken } from "shared/lib/hasRefreshToken";
import type { Route } from "./+types/checkout";

export function loader({ request }: Route.LoaderArgs) {
    if (!hasRefreshToken(request)) {
        throw redirect("/login?redirect=%2Fcheckout");
    }
    return null;
}

export function meta() {
    return [{ title: "Оформление заказа — Shop" }];
}

export { Checkout as default } from "pages/checkout";
