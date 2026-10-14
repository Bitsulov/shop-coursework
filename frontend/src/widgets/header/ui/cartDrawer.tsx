import { Link } from "react-router";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { removeCartProduct, type CartResponse } from "entities/cart";
import { ButtonLink } from "shared/ui/button";
import { Drawer } from "shared/ui/drawer";
import { Icon } from "shared/ui/icon";
import { IconButton } from "shared/ui/iconButton";
import { formatPrice } from "shared/lib/formatPrice";
import cartIcon from "shared/icons/cart.svg";
import imageIcon from "shared/icons/image.svg";
import trashIcon from "shared/icons/trash.svg";
import styles from "./cartDrawer.module.scss";

interface CartDrawerProps {
    open: boolean;
    onClose: () => void;
    cart?: CartResponse;
}

export function CartDrawer({ open, onClose, cart }: CartDrawerProps) {
    const queryClient = useQueryClient();
    const count = cart?.totalQuantity ?? 0;

    const removeMutation = useMutation({
        mutationFn: removeCartProduct,
        onSuccess: data => queryClient.setQueryData(["cart"], data)
    });

    return (
        <Drawer
            open={open}
            onClose={onClose}
            label="Корзина"
            title={
                <h2 className={styles.title}>
                    Корзина <span className={styles.titleCount}>· {count}</span>
                </h2>
            }
            footer={
                count > 0 && cart ? (
                    <>
                        <div className={styles.total}>
                            <span>Итого</span>
                            <span>{formatPrice(cart.total)}</span>
                        </div>
                        <ButtonLink
                            to="/checkout"
                            variant="primary"
                            size="lg"
                            block
                            onClick={onClose}
                        >
                            Оформить заказ
                        </ButtonLink>
                        <ButtonLink to="/cart" variant="text" block onClick={onClose}>
                            Перейти в корзину
                        </ButtonLink>
                    </>
                ) : undefined
            }
        >
            {count > 0 && cart ? (
                cart.products.map(({ product, quantity }) => (
                    <div key={product.uuid} className={styles.item}>
                        {product.imageUrl ? (
                            <img className={styles.image} src={product.imageUrl} alt="" />
                        ) : (
                            <span className={styles.image}>
                                <Icon src={imageIcon} size={24} />
                            </span>
                        )}
                        <div className={styles.info}>
                            <Link
                                className={styles.name}
                                to={`/product/${product.uuid}`}
                                onClick={onClose}
                            >
                                {product.name}
                            </Link>
                            <span className={styles.meta}>
                                {quantity} × {formatPrice(product.price)}
                            </span>
                        </div>
                        <IconButton
                            size="sm"
                            icon={trashIcon}
                            iconSize={18}
                            label={`Удалить ${product.name}`}
                            disabled={removeMutation.isPending}
                            onClick={() => removeMutation.mutate(product.uuid)}
                        />
                    </div>
                ))
            ) : (
                <div className={styles.empty}>
                    <span className={styles.emptyIcon}>
                        <Icon src={cartIcon} size={30} />
                    </span>
                    <h3 className={styles.emptyTitle}>В корзине пока пусто</h3>
                    <p>Добавьте товары из каталога, они появятся здесь.</p>
                    <ButtonLink to="/catalog" onClick={onClose}>
                        Перейти в каталог
                    </ButtonLink>
                </div>
            )}
        </Drawer>
    );
}
