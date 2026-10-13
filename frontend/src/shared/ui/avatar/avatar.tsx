import clsx from "clsx";
import styles from "./avatar.module.scss";

interface AvatarProps {
    name: string;
    src?: string | null;
    size?: number;
    className?: string;
}

function getInitials(name: string) {
    return name
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map(part => part[0].toUpperCase())
        .join("");
}

export function Avatar({ name, src, size = 32, className }: AvatarProps) {
    return (
        <span
            className={clsx(styles.avatar, className)}
            style={{ width: size, height: size, fontSize: Math.round(size * 0.4) }}
        >
            {src ? (
                <img src={src} alt="" />
            ) : (
                <span aria-hidden="true">{getInitials(name)}</span>
            )}
        </span>
    );
}
