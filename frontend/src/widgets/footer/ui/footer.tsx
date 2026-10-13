import { Link } from "react-router";
import { categories } from "entities/product";
import { supportEmail, supportPhone, workingHours } from "shared/config/contacts";
import logo from "shared/icons/logo.svg";
import styles from "./footer.module.scss";

export function Footer() {
    return (
        <footer className={styles.footer}>
            <div className="container">
                <div className={styles.grid}>
                    <div className={styles.brand}>
                        <Link
                            className={styles.logo}
                            to="/"
                            aria-label="Shop — на главную"
                        >
                            <img src={logo} alt="" />
                        </Link>
                        <div className={styles.contacts}>
                            <a className={styles.phone} href={`tel:${supportPhone.href}`}>
                                {supportPhone.text}
                            </a>
                            <a className={styles.link} href={`mailto:${supportEmail}`}>
                                {supportEmail}
                            </a>
                            <span className={styles.muted}>{workingHours}</span>
                        </div>
                    </div>
                    <nav aria-labelledby="footer-shop">
                        <h2 className={styles.title} id="footer-shop">
                            Магазин
                        </h2>
                        <div className={styles.links}>
                            <Link className={styles.link} to="/catalog">
                                Каталог
                            </Link>
                            <Link className={styles.link} to="/cart">
                                Корзина
                            </Link>
                            <Link className={styles.link} to="/profile">
                                Личный кабинет
                            </Link>
                        </div>
                    </nav>
                    <nav
                        className={styles.categories}
                        aria-labelledby="footer-categories"
                    >
                        <h2 className={styles.title} id="footer-categories">
                            Категории
                        </h2>
                        <div className={styles.categoryLinks}>
                            {categories.map(category => (
                                <Link
                                    key={category.value}
                                    className={styles.link}
                                    to={`/catalog?category=${category.value}`}
                                >
                                    {category.name}
                                </Link>
                            ))}
                        </div>
                    </nav>
                </div>
                <div className={styles.bottom}>
                    <span>© {new Date().getFullYear()} Shop</span>
                </div>
            </div>
        </footer>
    );
}
