import { reactRouter } from "@react-router/dev/vite";
import babel from "@rolldown/plugin-babel";
import { defineConfig } from "vite";

export default defineConfig({
    plugins: [reactRouter(), babel({ plugins: ["babel-plugin-react-compiler"] })],
    resolve: {
        tsconfigPaths: true
    }
});
