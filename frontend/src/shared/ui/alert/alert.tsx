import type { ReactNode } from "react";
import clsx from "clsx";
import { Icon } from "../icon";
import alertCircleIcon from "shared/icons/alertCircle.svg";
import styles from "./alert.module.scss";

interface AlertProps {
    variant?: "error" | "info";
    children: ReactNode;
}

export function Alert({ variant = "error", children }: AlertProps) {
    return (
        <div
            className={clsx(styles.alert, styles[variant])}
            role={variant === "error" ? "alert" : "status"}
        >
            <Icon src={alertCircleIcon} size={18} />
            <span>{children}</span>
        </div>
    );
}
