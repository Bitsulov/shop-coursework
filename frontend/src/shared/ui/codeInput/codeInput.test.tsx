import { useState } from "react";
import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { CodeInput } from "./codeInput";

function ControlledCodeInput({ onChange }: { onChange: (value: string) => void }) {
    const [value, setValue] = useState("");

    return (
        <CodeInput
            value={value}
            onChange={next => {
                setValue(next);
                onChange(next);
            }}
        />
    );
}

describe("Ввод кода", () => {
    it("Переводит фокус на следующую ячейку после цифры", async () => {
        render(<ControlledCodeInput onChange={vi.fn()} />);

        await userEvent.type(screen.getByLabelText("Цифра 1 из 6"), "4");

        expect(screen.getByLabelText("Цифра 2 из 6")).toHaveFocus();
    });

    it("Не принимает буквы", async () => {
        const onChange = vi.fn();
        render(<ControlledCodeInput onChange={onChange} />);

        await userEvent.type(screen.getByLabelText("Цифра 1 из 6"), "a");

        expect(onChange).not.toHaveBeenCalled();
    });

    it("Раскладывает вставленный код по ячейкам", async () => {
        const onChange = vi.fn();
        render(<ControlledCodeInput onChange={onChange} />);

        await userEvent.click(screen.getByLabelText("Цифра 1 из 6"));
        await userEvent.paste("12 34 56");

        expect(onChange).toHaveBeenLastCalledWith("123456");
        expect(screen.getByLabelText("Цифра 6 из 6")).toHaveValue("6");
    });
});
