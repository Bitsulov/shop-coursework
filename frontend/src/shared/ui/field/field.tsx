import { useId, useState, type ComponentProps, type ReactNode } from "react";
import clsx from "clsx";
import { Icon } from "../icon";
import { IconButton } from "../iconButton";
import alertCircleIcon from "shared/icons/alertCircle.svg";
import eyeIcon from "shared/icons/eye.svg";
import eyeOffIcon from "shared/icons/eyeOff.svg";
import styles from "./field.module.scss";

interface FieldProps extends ComponentProps<"input"> {
    label: string;
    error?: string;
    hint?: ReactNode;
    children?: ReactNode;
}

export function Field({
    label,
    error,
    hint,
    type = "text",
    id,
    className,
    children,
    ...props
}: FieldProps) {
    const generatedId = useId();
    const inputId = id ?? generatedId;
    const messageId = `${inputId}-message`;
    const isPassword = type === "password";
    const [isPasswordVisible, setIsPasswordVisible] = useState(false);

    return (
        <div className={clsx(styles.field, error && styles.isInvalid, className)}>
            <div className={styles.control}>
                <input
                    id={inputId}
                    className={clsx(styles.input, isPassword && styles.withButton)}
                    type={isPassword && isPasswordVisible ? "text" : type}
                    placeholder=" "
                    aria-invalid={Boolean(error)}
                    aria-describedby={messageId}
                    {...props}
                />
                <label className={styles.label} htmlFor={inputId}>
                    {label}
                </label>
                {isPassword && (
                    <IconButton
                        className={styles.toggle}
                        icon={isPasswordVisible ? eyeOffIcon : eyeIcon}
                        label={isPasswordVisible ? "Скрыть пароль" : "Показать пароль"}
                        aria-pressed={isPasswordVisible}
                        onClick={() => setIsPasswordVisible(!isPasswordVisible)}
                    />
                )}
            </div>
            {children}
            <div id={messageId}>
                {error ? (
                    <p className={styles.error}>
                        <Icon src={alertCircleIcon} size={16} />
                        {error}
                    </p>
                ) : (
                    hint && <p className={styles.hint}>{hint}</p>
                )}
            </div>
        </div>
    );
}
