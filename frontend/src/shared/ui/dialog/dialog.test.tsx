import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { Dialog } from "./dialog";

describe("Модальное окно", () => {
    it("Называется по заголовку", () => {
        render(
            <Dialog open onClose={vi.fn()} title="Удалить адрес?">
                Текст
            </Dialog>
        );

        expect(
            screen.getByRole("dialog", { name: "Удалить адрес?" })
        ).toBeInTheDocument();
    });

    it("Закрывается по Escape и по кнопке закрытия", async () => {
        const onClose = vi.fn();
        render(
            <Dialog open onClose={onClose} title="Удалить адрес?">
                Текст
            </Dialog>
        );

        await userEvent.keyboard("{Escape}");
        await userEvent.click(screen.getByRole("button", { name: "Закрыть" }));

        expect(onClose).toHaveBeenCalledTimes(2);
    });
});
