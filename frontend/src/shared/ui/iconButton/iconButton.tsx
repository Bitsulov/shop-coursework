import type { ComponentProps } from "react";
import clsx from "clsx";
import { Icon } from "../icon";
import styles from "./iconButton.module.scss";

interface IconButtonProps extends ComponentProps<"button"> {
    icon: string;
    label: string;
    size?: "sm" | "md";
    iconSize?: number;
}

export function IconButton({
    icon,
    label,
    size = "md",
    iconSize = 22,
    type = "button",
    className,
    ...props
}: IconButtonProps) {
    return (
        <button
            type={type}
            aria-label={label}
            className={clsx(styles.iconButton, styles[size], className)}
            {...props}
        >
            <Icon src={icon} size={iconSize} />
        </button>
    );
}
