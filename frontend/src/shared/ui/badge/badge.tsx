import type { ReactNode } from "react";
import clsx from "clsx";
import styles from "./badge.module.scss";

interface BadgeProps {
    variant?: "neutral" | "dark" | "sale" | "success";
    children: ReactNode;
}

export function Badge({ variant = "neutral", children }: BadgeProps) {
    return <span className={clsx(styles.badge, styles[variant])}>{children}</span>;
}
