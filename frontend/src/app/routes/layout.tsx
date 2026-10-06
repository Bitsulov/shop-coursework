import { isRouteErrorResponse, Outlet } from "react-router";
import { Layout } from "widgets/layout";
import { NotFound } from "pages/notFound";
import { ErrorPage } from "pages/error";
import type { Route } from "./+types/layout";

export default function LayoutRoute() {
    return (
        <Layout>
            <Outlet />
        </Layout>
    );
}

export function ErrorBoundary({ error }: Route.ErrorBoundaryProps) {
    const isNotFound = isRouteErrorResponse(error) && error.status === 404;

    return <Layout>{isNotFound ? <NotFound /> : <ErrorPage />}</Layout>;
}
