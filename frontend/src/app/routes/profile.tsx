import { redirect } from "react-router";
import { hasRefreshToken } from "shared/lib/hasRefreshToken";
import type { Route } from "./+types/profile";

export function loader({ request }: Route.LoaderArgs) {
    if (!hasRefreshToken(request)) {
        throw redirect("/login?redirect=%2Fprofile");
    }
    return null;
}

export function meta() {
    return [{ title: "Личный кабинет — Shop" }];
}

export { Profile as default } from "pages/profile";
