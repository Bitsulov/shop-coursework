import { data } from "react-router";

export function loader() {
    return data(null, { status: 404 });
}

export function meta() {
    return [{ title: "Страница не найдена — Shop" }];
}

export { NotFound as default } from "pages/notFound";
