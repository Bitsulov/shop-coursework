import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { Drawer } from "./drawer";

describe("Выдвижная панель", () => {
    it("При открытии фокус переходит на кнопку закрытия", () => {
        render(
            <Drawer open onClose={vi.fn()} title="Меню" label="Меню">
                Содержимое
            </Drawer>
        );

        expect(screen.getByRole("button", { name: "Закрыть" })).toHaveFocus();
    });

    it("Закрывается по клавише Escape", async () => {
        const onClose = vi.fn();
        render(
            <Drawer open onClose={onClose} title="Меню" label="Меню">
                Содержимое
            </Drawer>
        );

        await userEvent.keyboard("{Escape}");

        expect(onClose).toHaveBeenCalledOnce();
    });

    it("Закрытая панель не реагирует на Escape", async () => {
        const onClose = vi.fn();
        render(
            <Drawer open={false} onClose={onClose} title="Меню" label="Меню">
                Содержимое
            </Drawer>
        );

        await userEvent.keyboard("{Escape}");

        expect(onClose).not.toHaveBeenCalled();
    });
});
