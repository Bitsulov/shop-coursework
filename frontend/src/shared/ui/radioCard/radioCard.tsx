import type { ComponentPropsWithoutRef, ReactNode } from "react";
import clsx from "clsx";
import styles from "./radioCard.module.scss";

interface RadioCardProps extends Omit<ComponentPropsWithoutRef<"input">, "type"> {
    children: ReactNode;
}

export function RadioCard({ children, className, ...props }: RadioCardProps) {
    return (
        <label className={clsx(styles.card, className)}>
            <input className={styles.input} type="radio" {...props} />
            <span className={styles.dot} aria-hidden="true" />
            <span className={styles.content}>{children}</span>
        </label>
    );
}
