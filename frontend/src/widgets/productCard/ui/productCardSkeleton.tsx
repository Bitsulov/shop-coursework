import { Skeleton } from "shared/ui/skeleton";
import styles from "./productCardSkeleton.module.scss";

export function ProductCardSkeleton() {
    return (
        <div className={styles.skeleton} aria-hidden="true">
            <Skeleton className={styles.media} />
            <Skeleton className={styles.line} style={{ width: "92%", marginTop: 6 }} />
            <Skeleton className={styles.line} style={{ width: "60%" }} />
            <Skeleton className={styles.title} style={{ width: "40%", marginTop: 8 }} />
        </div>
    );
}
