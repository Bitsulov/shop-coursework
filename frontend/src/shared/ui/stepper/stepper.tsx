import { Icon } from "../icon";
import minusIcon from "shared/icons/minus.svg";
import plusIcon from "shared/icons/plus.svg";
import styles from "./stepper.module.scss";

interface StepperProps {
    value: number;
    min?: number;
    max?: number;
    label: string;
    disabled?: boolean;
    onChange: (value: number) => void;
}

export function Stepper({
    value,
    min = 1,
    max = Infinity,
    label,
    disabled = false,
    onChange
}: StepperProps) {
    return (
        <div className={styles.stepper} role="group" aria-label={label}>
            <button
                type="button"
                aria-label="Уменьшить"
                disabled={disabled || value <= min}
                onClick={() => onChange(value - 1)}
            >
                <Icon src={minusIcon} size={18} />
            </button>
            <span className={styles.value} aria-live="polite">
                {value}
            </span>
            <button
                type="button"
                aria-label="Увеличить"
                disabled={disabled || value >= max}
                onClick={() => onChange(value + 1)}
            >
                <Icon src={plusIcon} size={18} />
            </button>
        </div>
    );
}
