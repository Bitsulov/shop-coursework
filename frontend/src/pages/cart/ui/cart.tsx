import { useQuery } from "@tanstack/react-query";
import { getCart } from "entities/cart";
import { getAddresses } from "entities/address";
import { CartItems } from "widgets/cartItems";
import { DeliveryBlock } from "widgets/deliveryBlock";
import { Breadcrumbs } from "shared/ui/breadcrumbs";
import { Button, ButtonLink } from "shared/ui/button";
import { Icon } from "shared/ui/icon";
import { Skeleton } from "shared/ui/skeleton";
import { formatPrice } from "shared/lib/formatPrice";
import { getPluralForm } from "shared/lib/getPluralForm";
import arrowLeftIcon from "shared/icons/arrowLeft.svg";
import cartIcon from "shared/icons/cart.svg";
import styles from "./cart.module.scss";

export function Cart() {
    const { data: cart, isPending } = useQuery({ queryKey: ["cart"], queryFn: getCart });
    const { data: addresses } = useQuery({
        queryKey: ["addresses"],
        queryFn: getAddresses
    });
    const hasAddress = (addresses?.length ?? 0) > 0;
    const count = cart?.totalQuantity ?? 0;
    const countText = `${count} ${getPluralForm(count, ["товар", "товара", "товаров"])}`;

    return (
        <div className="container">
            <Breadcrumbs items={[{ label: "Главная", to: "/" }, { label: "Корзина" }]} />
            <div className={styles.heading}>
                <h1 className={styles.title}>Корзина</h1>
                {count > 0 && <span className={styles.count}>{countText}</span>}
            </div>

            {isPending && (
                <div
                    className={styles.cart}
                    aria-busy="true"
                    aria-label="Загрузка корзины"
                >
                    <Skeleton style={{ height: 360, borderRadius: 12 }} />
                    <Skeleton style={{ height: 280, borderRadius: 12 }} />
                </div>
            )}

            {!isPending && count === 0 && (
                <div className={styles.empty}>
                    <span className={styles.emptyIcon}>
                        <Icon src={cartIcon} size={30} />
                    </span>
                    <h2 className={styles.emptyTitle}>В корзине пока пусто</h2>
                    <p>Загляните в каталог: там подборки популярных товаров и новинок.</p>
                    <div className={styles.emptyActions}>
                        <ButtonLink to="/catalog">Перейти в каталог</ButtonLink>
                        <ButtonLink to="/" variant="text">
                            На главную
                        </ButtonLink>
                    </div>
                </div>
            )}

            {!isPending && cart && count > 0 && (
                <>
                    <div className={styles.cart}>
                        <section className={styles.items} aria-label="Товары в корзине">
                            <DeliveryBlock />
                            <CartItems cart={cart} />
                            <ButtonLink
                                to="/catalog"
                                variant="text"
                                className={styles.back}
                            >
                                <Icon src={arrowLeftIcon} size={18} />
                                Продолжить покупки
                            </ButtonLink>
                        </section>

                        <aside className={styles.summary} aria-labelledby="summary-title">
                            <h2 className={styles.summaryTitle} id="summary-title">
                                Ваш заказ
                            </h2>
                            <div className={styles.row}>
                                <span className={styles.muted}>Товары, {countText}</span>
                                <span>{formatPrice(cart.subtotal)}</span>
                            </div>
                            {cart.discount > 0 && (
                                <div className={styles.row}>
                                    <span className={styles.muted}>Скидка</span>
                                    <span>−{formatPrice(cart.discount)}</span>
                                </div>
                            )}
                            <div className={styles.row}>
                                <span className={styles.muted}>Доставка</span>
                                <span>Бесплатно</span>
                            </div>
                            <div className={styles.divider} />
                            <div className={styles.row}>
                                <span className={styles.totalLabel}>Итого</span>
                                <span className={styles.total}>
                                    {formatPrice(cart.total)}
                                </span>
                            </div>
                            {hasAddress ? (
                                <ButtonLink
                                    to="/checkout"
                                    variant="primary"
                                    size="lg"
                                    block
                                >
                                    Перейти к оформлению
                                </ButtonLink>
                            ) : (
                                <>
                                    <Button variant="primary" size="lg" block disabled>
                                        Перейти к оформлению
                                    </Button>
                                    <p className={styles.hint}>
                                        Сначала укажите адрес доставки
                                    </p>
                                </>
                            )}
                            <p className={styles.note}>
                                Получателя и оплату подтвердите на следующем шаге.
                            </p>
                        </aside>
                    </div>
                    <div className={styles.mobileBar}>
                        <div>
                            <span className={styles.note}>Итого</span>
                            <div className={styles.mobileTotal}>
                                {formatPrice(cart.total)}
                            </div>
                        </div>
                        {hasAddress ? (
                            <ButtonLink
                                to="/checkout"
                                variant="primary"
                                className={styles.grow}
                            >
                                Оформить
                            </ButtonLink>
                        ) : (
                            <Button variant="primary" className={styles.grow} disabled>
                                Укажите адрес
                            </Button>
                        )}
                    </div>
                </>
            )}
        </div>
    );
}
