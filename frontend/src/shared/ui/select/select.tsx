import type { ComponentPropsWithoutRef } from "react";
import clsx from "clsx";
import { Icon } from "../icon";
import chevronDownIcon from "shared/icons/chevronDown.svg";
import styles from "./select.module.scss";

interface SelectProps extends ComponentPropsWithoutRef<"select"> {
    options: { value: string; label: string }[];
    ghost?: boolean;
}

export function Select({ options, ghost, className, ...props }: SelectProps) {
    return (
        <div className={clsx(styles.select, ghost && styles.ghost, className)}>
            <select {...props}>
                {options.map(option => (
                    <option key={option.value} value={option.value}>
                        {option.label}
                    </option>
                ))}
            </select>
            <Icon className={styles.chevron} src={chevronDownIcon} size={18} />
        </div>
    );
}
