import type { ComponentPropsWithoutRef, ReactNode } from "react";
import clsx from "clsx";
import { Icon } from "../icon";
import checkIcon from "shared/icons/check.svg";
import styles from "./checkbox.module.scss";

interface CheckboxProps extends Omit<ComponentPropsWithoutRef<"input">, "type"> {
    label: ReactNode;
}

export function Checkbox({ label, className, ...props }: CheckboxProps) {
    return (
        <label className={clsx(styles.check, className)}>
            <input className={styles.input} type="checkbox" {...props} />
            <span className={styles.box}>
                <Icon src={checkIcon} size={12} />
            </span>
            {label}
        </label>
    );
}
