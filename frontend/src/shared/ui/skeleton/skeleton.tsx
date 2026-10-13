import type { CSSProperties } from "react";
import clsx from "clsx";
import styles from "./skeleton.module.scss";

interface SkeletonProps {
    className?: string;
    style?: CSSProperties;
}

export function Skeleton({ className, style }: SkeletonProps) {
    return <div className={clsx(styles.skeleton, className)} style={style} />;
}
