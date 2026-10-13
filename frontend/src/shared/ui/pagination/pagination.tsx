import { Icon } from "../icon";
import { getPageNumbers } from "shared/lib/getPageNumbers";
import chevronLeftIcon from "shared/icons/chevronLeft.svg";
import chevronRightIcon from "shared/icons/chevronRight.svg";
import styles from "./pagination.module.scss";

interface PaginationProps {
    page: number;
    totalPages: number;
    onChange: (page: number) => void;
}

export function Pagination({ page, totalPages, onChange }: PaginationProps) {
    if (totalPages <= 1) return null;

    return (
        <nav className={styles.pagination} aria-label="Страницы">
            <button
                type="button"
                aria-label="Предыдущая страница"
                disabled={page <= 1}
                onClick={() => onChange(page - 1)}
            >
                <Icon src={chevronLeftIcon} size={18} />
            </button>
            {getPageNumbers(page, totalPages).map((number, index) =>
                number === null ? (
                    <span key={`gap-${index}`} className={styles.gap} aria-hidden="true">
                        …
                    </span>
                ) : (
                    <button
                        key={number}
                        type="button"
                        aria-label={`Страница ${number}`}
                        aria-current={number === page ? "page" : undefined}
                        onClick={() => onChange(number)}
                    >
                        {number}
                    </button>
                )
            )}
            <button
                type="button"
                aria-label="Следующая страница"
                disabled={page >= totalPages}
                onClick={() => onChange(page + 1)}
            >
                <Icon src={chevronRightIcon} size={18} />
            </button>
        </nav>
    );
}
