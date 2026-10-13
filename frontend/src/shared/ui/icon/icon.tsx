import clsx from "clsx";
import styles from "./icon.module.scss";

interface IconProps {
    src: string;
    size?: number;
    className?: string;
}

export function Icon({ src, size = 24, className }: IconProps) {
    return (
        <span
            aria-hidden="true"
            className={clsx(styles.icon, className)}
            style={{
                width: size,
                height: size,
                maskImage: `url("${src}")`,
                WebkitMaskImage: `url("${src}")`
            }}
        />
    );
}
