import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getCart, type CartResponse } from "entities/cart";
import { getAddresses } from "entities/address";
import { createOrder, type OrderResponse } from "entities/order";
import { getPrincipalUser, type UserDetailResponse } from "entities/user";
import { TopUpDialog } from "features/topUpBalance";
import { DeliveryBlock } from "widgets/deliveryBlock";
import { Alert } from "shared/ui/alert";
import { Breadcrumbs } from "shared/ui/breadcrumbs";
import { Button, ButtonLink } from "shared/ui/button";
import { Field } from "shared/ui/field";
import { Icon } from "shared/ui/icon";
import { Skeleton } from "shared/ui/skeleton";
import { formatPrice } from "shared/lib/formatPrice";
import { formatDeliveryDates } from "shared/lib/formatDeliveryDates";
import { getPluralForm } from "shared/lib/getPluralForm";
import checkIcon from "shared/icons/check.svg";
import imageIcon from "shared/icons/image.svg";
import walletIcon from "shared/icons/wallet.svg";
import {
    recipientSchema,
    type RecipientInput,
    type RecipientValues
} from "../model/schema";
import { orderErrorHandler } from "../model/orderErrorHandler";
import styles from "./checkout.module.scss";

function OrderDone({ order }: { order: OrderResponse }) {
    return (
        <div className={styles.done}>
            <span className={styles.successMark}>
                <Icon src={checkIcon} size={34} />
            </span>
            <h1 className={styles.title}>Заказ оформлен</h1>
            <p className={styles.muted}>
                Спасибо! Оплата прошла. Накануне доставки курьер позвонит по номеру{" "}
                {order.recipientPhone}.
            </p>
            <dl className={styles.details}>
                <dt>Номер заказа</dt>
                <dd>№ {order.uuid.slice(0, 8).toUpperCase()}</dd>
                <dt>Оплачено с баланса</dt>
                <dd>{formatPrice(order.total)}</dd>
                <dt>Остаток на балансе</dt>
                <dd>{formatPrice(order.balance)}</dd>
                <dt>Доставка</dt>
                <dd>
                    {formatDeliveryDates(order.deliveryFrom, order.deliveryTo)}, курьером
                </dd>
                <dt>Адрес</dt>
                <dd>{order.address}</dd>
                <dt>Получатель</dt>
                <dd>{order.recipientName}</dd>
            </dl>
            <div className={styles.doneActions}>
                <ButtonLink to="/catalog" size="lg">
                    Продолжить покупки
                </ButtonLink>
                <ButtonLink to="/" variant="text" size="lg">
                    На главную
                </ButtonLink>
            </div>
        </div>
    );
}

function CheckoutForm({
    cart,
    user,
    onDone
}: {
    cart: CartResponse;
    user: UserDetailResponse;
    onDone: (order: OrderResponse) => void;
}) {
    const queryClient = useQueryClient();
    const [serverError, setServerError] = useState<string | null>(null);
    const [isTopUpOpen, setIsTopUpOpen] = useState(false);

    const { data: addresses } = useQuery({
        queryKey: ["addresses"],
        queryFn: getAddresses
    });
    const address = addresses?.find(item => item.selected) ?? addresses?.[0];

    const {
        register,
        handleSubmit,
        formState: { errors }
    } = useForm<RecipientInput, unknown, RecipientValues>({
        resolver: zodResolver(recipientSchema),
        defaultValues: { recipientName: user.name, recipientPhone: user.phone ?? "" }
    });

    const orderMutation = useMutation({
        mutationFn: createOrder,
        onSuccess: async order => {
            onDone(order);
            await Promise.all([
                queryClient.invalidateQueries({ queryKey: ["cart"] }),
                queryClient.invalidateQueries({ queryKey: ["user"] })
            ]);
        },
        onError: error => setServerError(orderErrorHandler(error))
    });

    const lack = Math.max(0, cart.total - user.balance);
    const rest = user.balance - cart.total;
    const units = `${cart.totalQuantity} ${getPluralForm(cart.totalQuantity, ["товар", "товара", "товаров"])}`;
    const payHint = !address
        ? "Укажите адрес доставки, чтобы оплатить заказ."
        : lack > 0
          ? "Пополните баланс, чтобы оплатить заказ."
          : "Деньги спишутся с баланса аккаунта.";

    return (
        <div className={styles.layout}>
            <div className={styles.main}>
                <form
                    id="checkout-form"
                    className={styles.panel}
                    aria-labelledby="recipient-title"
                    noValidate
                    onSubmit={handleSubmit(values => {
                        if (!address) return;
                        setServerError(null);
                        orderMutation.mutate({ ...values, addressUuid: address.uuid });
                    })}
                >
                    <div className={styles.panelHead}>
                        <h2 className={styles.panelTitle} id="recipient-title">
                            Получатель
                        </h2>
                        <span className={styles.note}>Данные из профиля</span>
                    </div>
                    <div className={styles.fields}>
                        <Field
                            label="Имя и фамилия"
                            autoComplete="name"
                            error={errors.recipientName?.message}
                            {...register("recipientName")}
                        />
                        <Field
                            label="Телефон"
                            type="tel"
                            autoComplete="tel"
                            hint="Курьер позвонит накануне доставки"
                            error={errors.recipientPhone?.message}
                            {...register("recipientPhone")}
                        />
                    </div>
                </form>

                <DeliveryBlock />

                <section className={styles.panel} aria-labelledby="payment-title">
                    <h2 className={styles.panelTitle} id="payment-title">
                        Оплата
                    </h2>
                    <div className={styles.payCard}>
                        <span className={styles.payIcon} aria-hidden="true">
                            <Icon src={walletIcon} size={20} />
                        </span>
                        <div className={styles.payBody}>
                            <span className={styles.medium}>С баланса аккаунта</span>
                            <span className={styles.note}>
                                На счёте {formatPrice(user.balance)}
                            </span>
                        </div>
                        <div className={styles.payRest}>
                            {lack > 0 ? (
                                <>
                                    <span className={styles.danger}>Не хватает</span>
                                    <span className={styles.dangerStrong}>
                                        {formatPrice(lack)}
                                    </span>
                                </>
                            ) : (
                                <>
                                    <span className={styles.note}>Останется</span>
                                    <span className={styles.medium}>
                                        {formatPrice(rest)}
                                    </span>
                                </>
                            )}
                        </div>
                    </div>
                    {lack > 0 && (
                        <>
                            <Alert>
                                На балансе не хватает {formatPrice(lack)}. Пополните счёт
                                или уберите часть товаров из корзины.
                            </Alert>
                            <Button onClick={() => setIsTopUpOpen(true)}>
                                Пополнить на {formatPrice(lack)}
                            </Button>
                        </>
                    )}
                </section>
            </div>

            <aside className={styles.summary} aria-labelledby="order-title">
                <h2 className={styles.panelTitle} id="order-title">
                    Ваш заказ
                </h2>
                <ul className={styles.items}>
                    {cart.products.map(item => (
                        <li key={item.product.uuid} className={styles.item}>
                            {item.product.imageUrl ? (
                                <img
                                    className={styles.itemImage}
                                    src={item.product.imageUrl}
                                    alt=""
                                />
                            ) : (
                                <span className={styles.itemImage}>
                                    <Icon src={imageIcon} size={24} />
                                </span>
                            )}
                            <span className={styles.itemInfo}>
                                <span className={styles.itemName}>
                                    {item.product.name}
                                </span>
                                <span className={styles.note}>{item.quantity} шт.</span>
                            </span>
                            <span className={styles.itemTotal}>
                                {formatPrice(item.total)}
                            </span>
                        </li>
                    ))}
                </ul>
                <ButtonLink to="/cart" variant="text" size="sm" className={styles.edit}>
                    Изменить состав
                </ButtonLink>
                <div className={styles.divider} />
                <div className={styles.row}>
                    <span className={styles.note}>Товары, {units}</span>
                    <span>{formatPrice(cart.subtotal)}</span>
                </div>
                {cart.discount > 0 && (
                    <div className={styles.row}>
                        <span className={styles.note}>Скидка</span>
                        <span>−{formatPrice(cart.discount)}</span>
                    </div>
                )}
                <div className={styles.row}>
                    <span className={styles.note}>Доставка</span>
                    <span>Бесплатно</span>
                </div>
                <div className={styles.divider} />
                <div className={styles.row}>
                    <span className={styles.totalLabel}>Итого</span>
                    <span className={styles.total}>{formatPrice(cart.total)}</span>
                </div>
                {serverError && <Alert>{serverError}</Alert>}
                <Button
                    type="submit"
                    form="checkout-form"
                    variant="primary"
                    size="lg"
                    block
                    disabled={!address || lack > 0 || orderMutation.isPending}
                >
                    {orderMutation.isPending
                        ? "Оплачиваем…"
                        : `Оплатить ${formatPrice(cart.total)}`}
                </Button>
                <p className={styles.note}>{payHint}</p>
            </aside>

            <TopUpDialog
                open={isTopUpOpen}
                onClose={() => setIsTopUpOpen(false)}
                balance={user.balance}
                initialAmount={lack}
            />
        </div>
    );
}

export function Checkout() {
    const [order, setOrder] = useState<OrderResponse | null>(null);
    const { data: cart, isPending: isCartPending } = useQuery({
        queryKey: ["cart"],
        queryFn: getCart
    });
    const { data: user } = useQuery({
        queryKey: ["user"],
        queryFn: () => getPrincipalUser()
    });

    if (order) {
        return (
            <div className="container">
                <OrderDone order={order} />
            </div>
        );
    }

    return (
        <div className="container">
            <Breadcrumbs
                items={[
                    { label: "Главная", to: "/" },
                    { label: "Корзина", to: "/cart" },
                    { label: "Оформление заказа" }
                ]}
            />
            <h1 className={styles.pageTitle}>Оформление заказа</h1>

            {(isCartPending || !user) && (
                <Skeleton style={{ height: 480, borderRadius: 12 }} />
            )}

            {!isCartPending && cart && cart.totalQuantity === 0 && (
                <div className={styles.empty}>
                    <p className={styles.muted}>В корзине нет товаров для оформления.</p>
                    <ButtonLink to="/catalog">Перейти в каталог</ButtonLink>
                </div>
            )}

            {cart && cart.totalQuantity > 0 && user && (
                <CheckoutForm cart={cart} user={user} onDone={setOrder} />
            )}
        </div>
    );
}
