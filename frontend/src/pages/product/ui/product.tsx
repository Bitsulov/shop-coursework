import { Link, useParams } from "react-router";
import { useSelector } from "react-redux";
import { useQuery } from "@tanstack/react-query";
import { selectIsAuthenticated } from "entities/user";
import { getCart } from "entities/cart";
import { categories, getProduct, getRelatedProducts } from "entities/product";
import { AddToCartButton } from "features/addToCart";
import { ProductGrid } from "widgets/productCard";
import { Badge } from "shared/ui/badge";
import { Breadcrumbs } from "shared/ui/breadcrumbs";
import { ButtonLink } from "shared/ui/button";
import { Icon } from "shared/ui/icon";
import { Skeleton } from "shared/ui/skeleton";
import { formatPrice } from "shared/lib/formatPrice";
import cartIcon from "shared/icons/cart.svg";
import imageIcon from "shared/icons/image.svg";
import truckIcon from "shared/icons/truck.svg";
import styles from "./product.module.scss";

export function Product() {
    const { uuid = "" } = useParams();
    const isAuthenticated = useSelector(selectIsAuthenticated);

    const { data: product } = useQuery({
        queryKey: ["product", uuid],
        queryFn: () => getProduct(uuid)
    });

    const { data: related } = useQuery({
        queryKey: ["product", uuid, "related"],
        queryFn: () => getRelatedProducts(uuid)
    });

    const { data: cart } = useQuery({
        queryKey: ["cart"],
        queryFn: getCart,
        enabled: isAuthenticated
    });

    if (!product) {
        return (
            <div className="container">
                <div
                    className={styles.page}
                    aria-busy="true"
                    aria-label="Загрузка товара"
                >
                    <Skeleton className={styles.stageSkeleton} />
                    <div className={styles.buySkeleton}>
                        <Skeleton style={{ width: "95%", height: 32 }} />
                        <Skeleton style={{ width: "60%", height: 32 }} />
                        <Skeleton style={{ width: "40%", height: 38, marginTop: 16 }} />
                        <Skeleton style={{ height: 52, marginTop: 16 }} />
                    </div>
                </div>
            </div>
        );
    }

    const category = categories.find(item => item.value === product.category);
    const isInCart = cart?.products.some(item => item.product.uuid === product.uuid);
    const discount = product.oldPrice
        ? Math.round(((product.oldPrice - product.price) / product.oldPrice) * 100)
        : 0;
    const paragraphs = (product.description ?? "")
        .split(/\n+/)
        .map(paragraph => paragraph.trim())
        .filter(Boolean);

    return (
        <div className="container">
            <title>{`${product.name} — Shop`}</title>
            <Breadcrumbs
                items={[
                    { label: "Главная", to: "/" },
                    { label: "Каталог", to: "/catalog" },
                    ...(category
                        ? [
                              {
                                  label: category.name,
                                  to: `/catalog?category=${category.value}`
                              }
                          ]
                        : []),
                    { label: product.name }
                ]}
            />

            <div className={styles.page}>
                <div className={styles.stage}>
                    {product.imageUrl ? (
                        <img src={product.imageUrl} alt={product.name} />
                    ) : (
                        <Icon className={styles.placeholder} src={imageIcon} size={72} />
                    )}
                </div>

                <div className={styles.buy}>
                    <div className={styles.titleBlock}>
                        {discount > 0 && (
                            <div>
                                <Badge variant="sale">−{discount}%</Badge>
                            </div>
                        )}
                        <h1 className={styles.title}>{product.name}</h1>
                        <span className={styles.muted}>{product.brand}</span>
                    </div>

                    <div className={styles.priceBlock}>
                        <div className={styles.price}>
                            <span className={styles.priceNow}>
                                {formatPrice(product.price)}
                            </span>
                            {product.oldPrice && (
                                <s className={styles.priceOld}>
                                    {formatPrice(product.oldPrice)}
                                </s>
                            )}
                        </div>
                        {product.oldPrice && (
                            <span className={styles.muted}>
                                Экономия {formatPrice(product.oldPrice - product.price)}
                            </span>
                        )}
                    </div>

                    <div className={styles.cta}>
                        {isInCart ? (
                            <ButtonLink to="/cart" size="lg" block>
                                <Icon src={cartIcon} size={20} />В корзине · Перейти
                            </ButtonLink>
                        ) : (
                            <AddToCartButton product={product} size="lg" />
                        )}
                    </div>

                    <div className={styles.stock}>
                        <div>
                            <span
                                className={
                                    product.stock > 0
                                        ? styles.dot
                                        : `${styles.dot} ${styles.dotOff}`
                                }
                            />
                            <span className={styles.medium}>
                                {product.stock > 0 ? "В наличии" : "Нет в наличии"}
                            </span>
                        </div>
                        <div>
                            <Icon src={truckIcon} size={22} />
                            <span>Доставка курьером, дату покажем при оформлении</span>
                        </div>
                    </div>

                    {product.specs.length > 0 && (
                        <div>
                            <h2 className={styles.subtitle}>Коротко о товаре</h2>
                            <dl className={styles.specs}>
                                {product.specs.slice(0, 4).map(spec => (
                                    <SpecRow
                                        key={spec.name}
                                        name={spec.name}
                                        value={spec.value}
                                    />
                                ))}
                            </dl>
                            <a className={styles.link} href="#specs">
                                Все характеристики
                            </a>
                        </div>
                    )}
                </div>
            </div>

            <nav className={styles.anchors} aria-label="Разделы">
                {paragraphs.length > 0 && <a href="#about">Описание</a>}
                {product.specs.length > 0 && <a href="#specs">Характеристики</a>}
                {related && related.length > 0 && <a href="#related">Похожие товары</a>}
            </nav>

            {paragraphs.length > 0 && (
                <section
                    id="about"
                    className={styles.section}
                    aria-labelledby="about-title"
                >
                    <h2 className={styles.sectionTitle} id="about-title">
                        Описание
                    </h2>
                    <div className={styles.prose}>
                        {paragraphs.map(paragraph => (
                            <p key={paragraph}>{paragraph}</p>
                        ))}
                    </div>
                </section>
            )}

            {product.specs.length > 0 && (
                <section
                    id="specs"
                    className={styles.section}
                    aria-labelledby="specs-title"
                >
                    <h2 className={styles.sectionTitle} id="specs-title">
                        Характеристики
                    </h2>
                    <dl className={`${styles.specs} ${styles.specsFull}`}>
                        <SpecRow name="Бренд" value={product.brand} />
                        {product.color && <SpecRow name="Цвет" value={product.color} />}
                        {product.specs.map(spec => (
                            <SpecRow
                                key={spec.name}
                                name={spec.name}
                                value={spec.value}
                            />
                        ))}
                    </dl>
                </section>
            )}

            {related && related.length > 0 && (
                <section
                    id="related"
                    className={styles.section}
                    aria-labelledby="related-title"
                >
                    <div className={styles.sectionHead}>
                        <h2 className={styles.sectionTitle} id="related-title">
                            Похожие товары
                        </h2>
                        <Link
                            className={styles.link}
                            to={`/catalog?category=${product.category}`}
                        >
                            Все в категории
                        </Link>
                    </div>
                    <ProductGrid products={related.slice(0, 4)} wide />
                </section>
            )}
        </div>
    );
}

function SpecRow({ name, value }: { name: string; value: string }) {
    return (
        <div className={styles.specRow}>
            <dt>{name}</dt>
            <dd>{value}</dd>
        </div>
    );
}
