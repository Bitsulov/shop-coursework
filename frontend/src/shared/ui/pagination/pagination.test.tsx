import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { Pagination } from "./pagination";

describe("Пагинация", () => {
    it("Не отображается, если страница одна", () => {
        const { container } = render(
            <Pagination page={1} totalPages={1} onChange={vi.fn()} />
        );

        expect(container).toBeEmptyDOMElement();
    });

    it("Отмечает текущую страницу и блокирует переход назад с первой", () => {
        render(<Pagination page={1} totalPages={3} onChange={vi.fn()} />);

        expect(screen.getByRole("button", { name: "Страница 1" })).toHaveAttribute(
            "aria-current",
            "page"
        );
        expect(
            screen.getByRole("button", { name: "Предыдущая страница" })
        ).toBeDisabled();
    });

    it("Сообщает номер выбранной страницы", async () => {
        const onChange = vi.fn();
        render(<Pagination page={2} totalPages={3} onChange={onChange} />);

        await userEvent.click(screen.getByRole("button", { name: "Страница 3" }));
        await userEvent.click(
            screen.getByRole("button", { name: "Предыдущая страница" })
        );

        expect(onChange.mock.calls).toEqual([[3], [1]]);
    });
});
