import { ButtonLink } from "shared/ui/button";
import styles from "./notFound.module.scss";

export function NotFound() {
    return (
        <div className="container">
            <section className={styles.status} aria-labelledby="not-found-title">
                <span className={styles.code} aria-hidden="true">
                    404
                </span>
                <h1 className={styles.title} id="not-found-title">
                    Страница не найдена
                </h1>
                <p className={styles.text}>
                    Возможно, ссылка устарела или в адресе опечатка. Начните с главной или
                    откройте каталог.
                </p>
                <div className={styles.actions}>
                    <ButtonLink to="/" size="lg">
                        На главную
                    </ButtonLink>
                    <ButtonLink to="/catalog" variant="text" size="lg">
                        Открыть каталог
                    </ButtonLink>
                </div>
            </section>
        </div>
    );
}
