import { useId, type ComponentProps, type ReactNode } from "react";
import clsx from "clsx";
import styles from "./textarea.module.scss";

interface TextareaProps extends ComponentProps<"textarea"> {
    label: string;
    error?: string;
    hint?: ReactNode;
}

export function Textarea({ label, error, hint, id, className, ...props }: TextareaProps) {
    const generatedId = useId();
    const inputId = id ?? generatedId;

    return (
        <div className={clsx(styles.field, error && styles.isInvalid, className)}>
            <div className={styles.control}>
                <textarea
                    id={inputId}
                    className={styles.input}
                    placeholder=" "
                    rows={3}
                    aria-invalid={Boolean(error)}
                    aria-describedby={`${inputId}-message`}
                    {...props}
                />
                <label className={styles.label} htmlFor={inputId}>
                    {label}
                </label>
            </div>
            <p id={`${inputId}-message`} className={error ? styles.error : styles.hint}>
                {error ?? hint}
            </p>
        </div>
    );
}
