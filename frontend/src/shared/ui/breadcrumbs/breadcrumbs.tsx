import { Fragment } from "react";
import { Link } from "react-router";
import { Icon } from "../icon";
import chevronRightIcon from "shared/icons/chevronRight.svg";
import styles from "./breadcrumbs.module.scss";

interface BreadcrumbsProps {
    items: { label: string; to?: string }[];
}

export function Breadcrumbs({ items }: BreadcrumbsProps) {
    return (
        <nav className={styles.breadcrumbs} aria-label="Хлебные крошки">
            {items.map((item, index) => (
                <Fragment key={item.label}>
                    {index > 0 && <Icon src={chevronRightIcon} size={14} />}
                    {item.to ? (
                        <Link to={item.to}>{item.label}</Link>
                    ) : (
                        <span aria-current="page">{item.label}</span>
                    )}
                </Fragment>
            ))}
        </nav>
    );
}
