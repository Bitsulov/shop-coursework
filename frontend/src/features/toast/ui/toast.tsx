import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import clsx from "clsx";
import { Icon } from "shared/ui/icon";
import checkIcon from "shared/icons/check.svg";
import warningIcon from "shared/icons/warning.svg";
import { hideToast, selectCurrentToast } from "../model/toastSlice";
import { hidingTimeConst, showingTimeConst } from "../const/showingTimeConst";
import styles from "./toast.module.scss";

export function Toast() {
    const dispatch = useDispatch();
    const currentToast = useSelector(selectCurrentToast);
    const [leavingId, setLeavingId] = useState<string | null>(null);
    const isLeaving = currentToast !== null && leavingId === currentToast.id;

    useEffect(() => {
        if (!currentToast) return;

        const hideTimer = setTimeout(
            () => setLeavingId(currentToast.id),
            showingTimeConst
        );
        const removeTimer = setTimeout(
            () => dispatch(hideToast()),
            showingTimeConst + hidingTimeConst
        );

        return () => {
            clearTimeout(hideTimer);
            clearTimeout(removeTimer);
        };
    }, [currentToast, dispatch]);

    return (
        <div className={styles.region} aria-live="polite">
            {currentToast && (
                <div
                    key={currentToast.id}
                    className={clsx(styles.toast, isLeaving && styles.isLeaving)}
                    role="status"
                >
                    <span className={styles.icon}>
                        <Icon
                            src={
                                currentToast.type === "success" ? checkIcon : warningIcon
                            }
                            size={16}
                        />
                    </span>
                    <div className={styles.body}>
                        <div className={styles.title}>{currentToast.title}</div>
                        {currentToast.text && (
                            <div className={styles.text}>{currentToast.text}</div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
