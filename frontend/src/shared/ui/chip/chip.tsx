import type { ComponentPropsWithoutRef } from "react";
import clsx from "clsx";
import styles from "./chip.module.scss";

interface ChipProps extends ComponentPropsWithoutRef<"button"> {
    selected?: boolean;
    removable?: boolean;
    size?: "sm" | "md";
}

export function Chip({
    selected,
    removable,
    size = "md",
    type = "button",
    className,
    ...props
}: ChipProps) {
    return (
        <button
            type={type}
            aria-pressed={removable ? undefined : selected}
            className={clsx(
                styles.chip,
                styles[size],
                removable && styles.removable,
                className
            )}
            {...props}
        />
    );
}
