import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { Stepper } from "./stepper";

describe("Степпер количества", () => {
    it("Сообщает новое значение при нажатии на плюс и минус", async () => {
        const onChange = vi.fn();
        render(<Stepper value={2} max={5} label="Количество" onChange={onChange} />);

        await userEvent.click(screen.getByRole("button", { name: "Увеличить" }));
        await userEvent.click(screen.getByRole("button", { name: "Уменьшить" }));

        expect(onChange.mock.calls).toEqual([[3], [1]]);
    });

    it("Блокирует уменьшение на минимуме и увеличение на максимуме", () => {
        render(<Stepper value={1} max={1} label="Количество" onChange={vi.fn()} />);

        expect(screen.getByRole("button", { name: "Уменьшить" })).toBeDisabled();
        expect(screen.getByRole("button", { name: "Увеличить" })).toBeDisabled();
    });
});
