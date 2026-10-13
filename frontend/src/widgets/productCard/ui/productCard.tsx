import { Link } from "react-router";
import type { ProductCardResponse } from "entities/product";
import { AddToCartButton } from "features/addToCart";
import { Badge } from "shared/ui/badge";
import { Icon } from "shared/ui/icon";
import { formatPrice } from "shared/lib/formatPrice";
import imageIcon from "shared/icons/image.svg";
import styles from "./productCard.module.scss";

interface ProductCardProps {
    product: ProductCardResponse;
}

export function ProductCard({ product }: ProductCardProps) {
    const productUrl = `/product/${product.uuid}`;
    const discount = product.oldPrice
        ? Math.round(((product.oldPrice - product.price) / product.oldPrice) * 100)
        : 0;

    return (
        <article className={styles.card}>
            <Link
                className={styles.media}
                to={productUrl}
                tabIndex={-1}
                aria-hidden="true"
            >
                {product.imageUrl ? (
                    <img
                        className={styles.image}
                        src={product.imageUrl}
                        alt=""
                        loading="lazy"
                    />
                ) : (
                    <Icon className={styles.placeholder} src={imageIcon} size={48} />
                )}
                {discount > 0 && (
                    <span className={styles.badges}>
                        <Badge variant="sale">−{discount}%</Badge>
                    </span>
                )}
            </Link>
            <div className={styles.body}>
                <Link className={styles.title} to={productUrl}>
                    {product.name}
                </Link>
                <p className={styles.summary}>{product.summary}</p>
                <div className={styles.foot}>
                    <div className={styles.price}>
                        <span className={styles.priceNow}>
                            {formatPrice(product.price)}
                        </span>
                        {product.oldPrice && (
                            <s className={styles.priceOld}>
                                <span className={styles.srOnly}>Старая цена </span>
                                {formatPrice(product.oldPrice)}
                            </s>
                        )}
                    </div>
                    <AddToCartButton className={styles.cta} product={product} />
                </div>
            </div>
        </article>
    );
}
