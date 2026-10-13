import { useEffect, useRef, type ReactNode } from "react";
import clsx from "clsx";
import { IconButton } from "../iconButton";
import closeIcon from "shared/icons/close.svg";
import styles from "./drawer.module.scss";

interface DrawerProps {
    open: boolean;
    onClose: () => void;
    title: ReactNode;
    label: string;
    side?: "left" | "right";
    footer?: ReactNode;
    className?: string;
    children: ReactNode;
}

export function Drawer({
    open,
    onClose,
    title,
    label,
    side = "right",
    footer,
    className,
    children
}: DrawerProps) {
    const closeRef = useRef<HTMLButtonElement>(null);

    useEffect(() => {
        if (!open) return;

        closeRef.current?.focus();
        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === "Escape") onClose();
        };
        document.addEventListener("keydown", onKeyDown);

        return () => document.removeEventListener("keydown", onKeyDown);
    }, [open, onClose]);

    return (
        <div className={clsx(styles.root, open && styles.isOpen)} inert={!open}>
            <div className={styles.backdrop} aria-hidden="true" onClick={onClose} />
            <aside
                className={clsx(styles.drawer, styles[side], className)}
                role="dialog"
                aria-modal="true"
                aria-label={label}
            >
                <div className={styles.head}>
                    {title}
                    <IconButton
                        ref={closeRef}
                        icon={closeIcon}
                        label="Закрыть"
                        onClick={onClose}
                    />
                </div>
                <div className={styles.body}>{children}</div>
                {footer && <div className={styles.foot}>{footer}</div>}
            </aside>
        </div>
    );
}
