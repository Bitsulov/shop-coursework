import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { Field } from "./field";

describe("Поле ввода", () => {
    it("Связывает подпись с полем", () => {
        render(<Field label="Email" />);

        expect(screen.getByLabelText("Email")).toBeInTheDocument();
    });

    it("Показывает ошибку и отмечает поле как неверное", () => {
        render(<Field label="Email" error="Введите email" />);

        expect(screen.getByText("Введите email")).toBeInTheDocument();
        expect(screen.getByLabelText("Email")).toHaveAttribute("aria-invalid", "true");
    });

    it("Кнопка у пароля показывает и скрывает введённый текст", async () => {
        render(<Field label="Пароль" type="password" />);
        const input = screen.getByLabelText("Пароль");

        expect(input).toHaveAttribute("type", "password");
        await userEvent.click(screen.getByRole("button", { name: "Показать пароль" }));
        expect(input).toHaveAttribute("type", "text");
        await userEvent.click(screen.getByRole("button", { name: "Скрыть пароль" }));
        expect(input).toHaveAttribute("type", "password");
    });
});
