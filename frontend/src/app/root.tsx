import type { ReactNode } from "react";
import { Links, Meta, Outlet, Scripts, ScrollRestoration } from "react-router";
import { ErrorPage } from "pages/error";
import { QueryProvider } from "./providers/queryProvider";
import { StoreProvider } from "./providers/storeProvider";
import "./global.scss";

export function Layout({ children }: { children: ReactNode }) {
    return (
        <html lang="ru">
            <head>
                <meta charSet="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1" />
                <link rel="icon" type="image/svg+xml" href="/favicon.svg" />
                <Meta />
                <Links />
            </head>
            <body>
                {children}
                <ScrollRestoration />
                <Scripts />
            </body>
        </html>
    );
}

export default function App() {
    return (
        <StoreProvider>
            <QueryProvider>
                <Outlet />
            </QueryProvider>
        </StoreProvider>
    );
}

export function ErrorBoundary() {
    return (
        <main>
            <ErrorPage />
        </main>
    );
}
