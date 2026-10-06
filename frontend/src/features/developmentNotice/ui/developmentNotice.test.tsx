import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { DevelopmentNotice } from "./developmentNotice";

describe("Уведомление о разработке", () => {
    it("Отображается текст о том, что страница находится в разработке", () => {
        render(<DevelopmentNotice />);

        expect(screen.getByText("Страница в разработке")).toBeInTheDocument();
    });
});
