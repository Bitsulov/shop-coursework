import { useEffect, useId, useRef, type ReactNode } from "react";
import clsx from "clsx";
import { IconButton } from "../iconButton";
import closeIcon from "shared/icons/close.svg";
import styles from "./dialog.module.scss";

interface DialogProps {
    open: boolean;
    onClose: () => void;
    title: ReactNode;
    wide?: boolean;
    children: ReactNode;
}

export function Dialog({ open, onClose, title, wide = false, children }: DialogProps) {
    const titleId = useId();
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
            <div
                className={clsx(styles.dialog, wide && styles.wide)}
                role="dialog"
                aria-modal="true"
                aria-labelledby={titleId}
            >
                <div className={styles.head}>
                    <h2 className={styles.title} id={titleId}>
                        {title}
                    </h2>
                    <IconButton
                        ref={closeRef}
                        icon={closeIcon}
                        label="Закрыть"
                        onClick={onClose}
                    />
                </div>
                {children}
            </div>
        </div>
    );
}
