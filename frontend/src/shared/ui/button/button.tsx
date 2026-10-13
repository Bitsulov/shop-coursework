import type { ComponentPropsWithoutRef } from "react";
import { Link, type LinkProps } from "react-router";
import clsx from "clsx";
import styles from "./button.module.scss";

interface ButtonStyleProps {
    variant?: "primary" | "secondary" | "text" | "danger";
    size?: "sm" | "md" | "lg";
    block?: boolean;
}

interface ButtonProps extends ComponentPropsWithoutRef<"button">, ButtonStyleProps {}

interface ButtonLinkProps extends LinkProps, ButtonStyleProps {}

function buttonClassName({
    variant = "secondary",
    size = "md",
    block
}: ButtonStyleProps) {
    return clsx(styles.button, styles[variant], styles[size], block && styles.block);
}

export function Button({
    variant,
    size,
    block,
    type = "button",
    className,
    ...props
}: ButtonProps) {
    return (
        <button
            type={type}
            className={clsx(buttonClassName({ variant, size, block }), className)}
            {...props}
        />
    );
}

export function ButtonLink({
    variant,
    size,
    block,
    className,
    ...props
}: ButtonLinkProps) {
    return (
        <Link
            className={clsx(buttonClassName({ variant, size, block }), className)}
            {...props}
        />
    );
}
