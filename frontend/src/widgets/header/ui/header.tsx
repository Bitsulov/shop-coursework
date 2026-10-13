import { useEffect, useState } from "react";
import { Link, NavLink, useNavigation } from "react-router";
import { useSelector } from "react-redux";
import { useQuery } from "@tanstack/react-query";
import clsx from "clsx";
import { selectUser } from "entities/user";
import { getCart } from "entities/cart";
import { categories } from "entities/product";
import { Avatar } from "shared/ui/avatar";
import { ButtonLink } from "shared/ui/button";
import { Drawer } from "shared/ui/drawer";
import { Icon } from "shared/ui/icon";
import { IconButton } from "shared/ui/iconButton";
import logo from "shared/icons/logo.svg";
import menuIcon from "shared/icons/menu.svg";
import catalogIcon from "shared/icons/catalog.svg";
import cartIcon from "shared/icons/cart.svg";
import userIcon from "shared/icons/user.svg";
import chevronRightIcon from "shared/icons/chevronRight.svg";
import { supportPhone, workingHours } from "shared/config/contacts";
import styles from "./header.module.scss";

export function Header() {
    const user = useSelector(selectUser);
    const navigation = useNavigation();
    const [isCompact, setIsCompact] = useState(false);
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    const { data: cart } = useQuery({
        queryKey: ["cart"],
        queryFn: getCart,
        enabled: user.isAuthenticated
    });
    const cartCount = user.isAuthenticated ? (cart?.totalQuantity ?? 0) : 0;

    useEffect(() => {
        const onScroll = () => setIsCompact(window.scrollY > 8);
        onScroll();
        window.addEventListener("scroll", onScroll, { passive: true });

        return () => window.removeEventListener("scroll", onScroll);
    }, []);

    const closeMenu = () => setIsMenuOpen(false);

    const menuLinks = [
        { to: "/catalog", text: "Каталог" },
        ...categories.map(category => ({
            to: `/catalog?category=${category.value}`,
            text: category.name
        })),
        { to: "/cart", text: "Корзина" },
        user.isAuthenticated
            ? { to: "/profile", text: "Профиль" }
            : { to: "/login", text: "Войти" }
    ];

    return (
        <header className={clsx(styles.header, isCompact && styles.isCompact)}>
            {navigation.state !== "idle" && (
                <div className={styles.progress} aria-hidden="true">
                    <span />
                </div>
            )}
            <a className={styles.skipLink} href="#main">
                Перейти к содержимому
            </a>
            <div className="container">
                <div className={styles.row}>
                    <IconButton
                        className={styles.menuButton}
                        icon={menuIcon}
                        iconSize={24}
                        label="Открыть меню"
                        aria-expanded={isMenuOpen}
                        onClick={() => setIsMenuOpen(true)}
                    />
                    <Link className={styles.logo} to="/" aria-label="Shop — на главную">
                        <img src={logo} alt="" />
                    </Link>
                    <ButtonLink className={styles.catalogLink} to="/catalog" size="sm">
                        <Icon src={catalogIcon} size={18} />
                        Каталог
                    </ButtonLink>
                    <div className={styles.actions}>
                        <Link
                            className={styles.iconLink}
                            to="/cart"
                            aria-label={`Корзина, товаров: ${cartCount}`}
                        >
                            <Icon src={cartIcon} size={24} />
                            {cartCount > 0 && (
                                <span className={styles.count} aria-hidden="true">
                                    {cartCount}
                                </span>
                            )}
                        </Link>
                        {user.isAuthenticated ? (
                            <Link
                                className={styles.user}
                                to="/profile"
                                aria-label="Профиль"
                            >
                                <Avatar name={user.name} src={user.avatarUrl} />
                                <span className={styles.userName}>{user.name}</span>
                            </Link>
                        ) : (
                            <NavLink
                                className={styles.signIn}
                                to="/login"
                                aria-label="Войти"
                            >
                                <Icon src={userIcon} size={22} />
                                <span>Войти</span>
                            </NavLink>
                        )}
                    </div>
                </div>
            </div>

            <Drawer
                open={isMenuOpen}
                onClose={closeMenu}
                side="left"
                label="Меню"
                className={styles.menu}
                title={
                    <Link className={styles.logo} to="/" onClick={closeMenu}>
                        <img src={logo} alt="Shop" />
                    </Link>
                }
                footer={
                    <>
                        <a className={styles.menuPhone} href={`tel:${supportPhone.href}`}>
                            {supportPhone.text}
                        </a>
                        <span className={styles.muted}>{workingHours}</span>
                    </>
                }
            >
                <nav aria-label="Меню">
                    {menuLinks.map(link => (
                        <Link
                            key={link.to}
                            className={styles.menuLink}
                            to={link.to}
                            onClick={closeMenu}
                        >
                            {link.text}
                            <Icon src={chevronRightIcon} size={18} />
                        </Link>
                    ))}
                </nav>
            </Drawer>
        </header>
    );
}
