import { type RouteConfig, index, layout, route } from "@react-router/dev/routes";

export default [
    layout("routes/layout.tsx", [
        index("routes/home.tsx"),
        route("catalog", "routes/catalog.tsx"),
        route("product/:uuid", "routes/product.tsx"),
        route("*", "routes/not-found.tsx")
    ])
] satisfies RouteConfig;
