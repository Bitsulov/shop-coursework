import { defineConfig } from "vitest/config";

export default defineConfig({
    resolve: {
        tsconfigPaths: true
    },
    test: {
        environment: "jsdom",
        setupFiles: ["./src/shared/tests/setup.ts"],
        css: {
            modules: { classNameStrategy: "non-scoped" }
        },
        coverage: {
            provider: "v8",
            reporter: ["text", "html"],
            include: ["src/**/*.{ts,tsx}"],
            exclude: ["src/app/**"]
            // thresholds: { lines: 75, functions: 75 }
        }
    }
});
