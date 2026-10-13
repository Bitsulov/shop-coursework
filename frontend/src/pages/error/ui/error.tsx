import clsx from "clsx";
import { Button } from "shared/ui/button";
import { Icon } from "shared/ui/icon";
import logo from "shared/icons/logo.svg";
import warningIcon from "shared/icons/warning.svg";
import refreshIcon from "shared/icons/refresh.svg";
import styles from "./error.module.scss";

interface ErrorPageProps {
    standalone?: boolean;
}

export function ErrorPage({ standalone = false }: ErrorPageProps) {
    return (
        <div className={clsx(standalone && styles.standalone)}>
            <section className={styles.status} aria-labelledby="error-title">
                {standalone && (
                    <a className={styles.logo} href="/" aria-label="Shop — на главную">
                        <img src={logo} alt="" />
                    </a>
                )}
                <span className={styles.icon}>
                    <Icon src={warningIcon} size={30} />
                </span>
                <h1 className={styles.title} id="error-title">
                    Что-то пошло не так
                </h1>
                <p className={styles.text}>
                    Попробуйте обновить страницу через минуту или вернитесь на главную.
                </p>
                <div className={styles.actions}>
                    <Button size="lg" onClick={() => window.location.reload()}>
                        <Icon src={refreshIcon} size={18} />
                        Обновить страницу
                    </Button>
                    <a className={styles.homeLink} href="/">
                        На главную
                    </a>
                </div>
            </section>
        </div>
    );
}
