import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { Avatar } from "./avatar";

describe("Аватар", () => {
    it("Без фото показывает инициалы первых двух слов имени", () => {
        render(<Avatar name="анна сергеевна петрова" />);

        expect(screen.getByText("АС")).toBeInTheDocument();
    });

    it("С фото показывает изображение вместо инициалов", () => {
        const { container } = render(<Avatar name="Анна" src="/avatar.png" />);

        expect(container.querySelector("img")).toHaveAttribute("src", "/avatar.png");
        expect(screen.queryByText("А")).not.toBeInTheDocument();
    });
});
