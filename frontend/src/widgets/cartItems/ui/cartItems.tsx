import { Link } from "react-router";
import { useDispatch } from "react-redux";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import axios from "axios";
import {
    removeCartProduct,
    setCartProductQuantity,
    type CartProductResponse,
    type CartResponse
} from "entities/cart";
import { showToast } from "features/toast";
import { Button } from "shared/ui/button";
import { Icon } from "shared/ui/icon";
import { Stepper } from "shared/ui/stepper";
import { formatPrice } from "shared/lib/formatPrice";
import imageIcon from "shared/icons/image.svg";
import trashIcon from "shared/icons/trash.svg";
import styles from "./cartItems.module.scss";

interface CartItemsProps {
    cart: CartResponse;
}

function CartLine({ item }: { item: CartProductResponse }) {
    const dispatch = useDispatch();
    const queryClient = useQueryClient();
    const { product, quantity } = item;
    const productUrl = `/product/${product.uuid}`;

    const quantityMutation = useMutation({
        mutationFn: setCartProductQuantity,
        onSuccess: cart => queryClient.setQueryData(["cart"], cart),
        onError: error => {
            const isStockError =
                axios.isAxiosError(error) && error.response?.status === 409;
            dispatch(
                showToast({
                    title: isStockError
                        ? "Больше нет в наличии"
                        : "Не удалось изменить количество",
                    text: product.name,
                    type: "error"
                })
            );
        }
    });

    const removeMutation = useMutation({
        mutationFn: removeCartProduct,
        onSuccess: cart => {
            queryClient.setQueryData(["cart"], cart);
            dispatch(
                showToast({
                    title: "Удалено из корзины",
                    text: product.name,
                    type: "success"
                })
            );
        },
        onError: () =>
            dispatch(
                showToast({
                    title: "Не удалось удалить товар",
                    text: product.name,
                    type: "error"
                })
            )
    });

    const isBusy = quantityMutation.isPending || removeMutation.isPending;

    return (
        <article className={styles.line}>
            <Link
                to={productUrl}
                tabIndex={-1}
                aria-hidden="true"
                className={styles.media}
            >
                {product.imageUrl ? (
                    <img src={product.imageUrl} alt="" />
                ) : (
                    <Icon className={styles.placeholder} src={imageIcon} size={32} />
                )}
            </Link>
            <div className={styles.info}>
                <Link className={styles.name} to={productUrl}>
                    {product.name}
                </Link>
                <span className={styles.muted}>{product.summary}</span>
                <span className={styles.stock}>
                    <span className={product.stock > 0 ? styles.dot : styles.dotOff} />
                    {product.stock > 0 ? "В наличии" : "Нет в наличии"}
                </span>
                <div className={styles.tools}>
                    <Button
                        variant="text"
                        size="sm"
                        disabled={isBusy}
                        onClick={() => removeMutation.mutate(product.uuid)}
                    >
                        <Icon src={trashIcon} size={18} />
                        Удалить
                    </Button>
                </div>
            </div>
            <div className={styles.quantity}>
                <Stepper
                    value={quantity}
                    max={product.stock}
                    label={`Количество: ${product.name}`}
                    disabled={isBusy}
                    onChange={value =>
                        quantityMutation.mutate({
                            productUuid: product.uuid,
                            quantity: value
                        })
                    }
                />
            </div>
            <div className={styles.sum}>
                <div className={styles.total}>{formatPrice(item.total)}</div>
                {product.oldPrice && (
                    <s className={styles.muted}>
                        {formatPrice(product.oldPrice * quantity)}
                    </s>
                )}
                {quantity > 1 && (
                    <div className={styles.unit}>{formatPrice(product.price)} за шт.</div>
                )}
            </div>
        </article>
    );
}

export function CartItems({ cart }: CartItemsProps) {
    return (
        <div className={styles.list}>
            {cart.products.map(item => (
                <CartLine key={item.product.uuid} item={item} />
            ))}
        </div>
    );
}
