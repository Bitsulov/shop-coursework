import { useRef, type ClipboardEvent, type KeyboardEvent } from "react";
import clsx from "clsx";
import styles from "./codeInput.module.scss";

interface CodeInputProps {
    value: string;
    onChange: (value: string) => void;
    length?: number;
    isError?: boolean;
    disabled?: boolean;
}

export function CodeInput({
    value,
    onChange,
    length = 6,
    isError = false,
    disabled = false
}: CodeInputProps) {
    const inputsRef = useRef<(HTMLInputElement | null)[]>([]);
    const digits = Array.from({ length }, (_, index) => value[index] ?? "");

    const focusCell = (index: number) => {
        inputsRef.current[Math.max(0, Math.min(index, length - 1))]?.focus();
    };

    const setDigit = (index: number, digit: string) => {
        const next = [...digits];
        next[index] = digit;
        onChange(next.join("").slice(0, length));
    };

    const onCellChange = (index: number, raw: string) => {
        const digit = raw.replace(/\D/g, "").slice(-1);
        if (!digit) return;
        setDigit(index, digit);
        focusCell(index + 1);
    };

    const onKeyDown = (index: number, event: KeyboardEvent<HTMLInputElement>) => {
        if (event.key === "Backspace") {
            event.preventDefault();
            if (digits[index]) {
                setDigit(index, "");
            } else if (index > 0) {
                setDigit(index - 1, "");
                focusCell(index - 1);
            }
        } else if (event.key === "ArrowLeft") {
            focusCell(index - 1);
        } else if (event.key === "ArrowRight") {
            focusCell(index + 1);
        }
    };

    const onPaste = (event: ClipboardEvent<HTMLInputElement>) => {
        event.preventDefault();
        const pasted = event.clipboardData
            .getData("text")
            .replace(/\D/g, "")
            .slice(0, length);
        if (!pasted) return;
        onChange(pasted);
        focusCell(pasted.length);
    };

    return (
        <div
            className={clsx(styles.code, isError && styles.isError)}
            role="group"
            aria-label="Код подтверждения"
        >
            {digits.map((digit, index) => (
                <input
                    key={index}
                    ref={element => {
                        inputsRef.current[index] = element;
                    }}
                    className={clsx(digit && styles.isFilled)}
                    value={digit}
                    inputMode="numeric"
                    autoComplete={index === 0 ? "one-time-code" : "off"}
                    aria-label={`Цифра ${index + 1} из ${length}`}
                    aria-invalid={isError}
                    disabled={disabled}
                    onChange={event => onCellChange(index, event.target.value)}
                    onKeyDown={event => onKeyDown(index, event)}
                    onPaste={onPaste}
                    onFocus={event => event.target.select()}
                />
            ))}
        </div>
    );
}
