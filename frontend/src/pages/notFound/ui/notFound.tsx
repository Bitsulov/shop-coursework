import { Link } from "react-router";

export function NotFound() {
    return (
        <>
            <h1>Страница не найдена</h1>
            <Link to="/">На главную</Link>
        </>
    );
}
