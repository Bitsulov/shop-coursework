import { useState } from "react";
import { Link } from "react-router";
import { useQuery } from "@tanstack/react-query";
import { categories, getProducts, type ProductsParams } from "entities/product";
import { ProductGrid } from "widgets/productCard";
import { ButtonLink } from "shared/ui/button";
import { Icon } from "shared/ui/icon";
import arrowRightIcon from "shared/icons/arrowRight.svg";
import imgHero from "shared/icons/imgHero.svg";
import styles from "./home.module.scss";

const tabs: { id: string; name: string; params: ProductsParams; link: string }[] = [
    { id: "new", name: "Новинки", params: { sort: "new" }, link: "/catalog" },
    {
        id: "sale",
        name: "Со скидкой",
        params: { sale: true, sort: "discount" },
        link: "/catalog?sale=true&sort=discount"
    }
];

export function Home() {
    const [activeTab, setActiveTab] = useState(tabs[0]);

    const { data, isPending } = useQuery({
        queryKey: ["products", "home", activeTab.id],
        queryFn: () => getProducts({ ...activeTab.params, size: 8 })
    });

    return (
        <div className="container">
            <section className={styles.hero} aria-labelledby="hero-title">
                <div className={styles.heroText}>
                    <span className={styles.eyebrow}>Электроника и бытовая техника</span>
                    <h1 className={styles.display} id="hero-title">
                        Техника для дома и работы
                    </h1>
                    <p className={styles.lead}>
                        Смартфоны, ноутбуки, телевизоры и бытовая техника с подробными
                        характеристиками и доставкой до двери.
                    </p>
                    <div>
                        <ButtonLink to="/catalog" size="lg">
                            Перейти в каталог
                        </ButtonLink>
                    </div>
                </div>
                <Link
                    className={styles.heroMedia}
                    to="/catalog"
                    aria-label="Открыть каталог"
                >
                    <img src={imgHero} alt="" />
                </Link>
            </section>

            <section className={styles.section} aria-labelledby="categories-title">
                <div className={styles.sectionHead}>
                    <h2 className={styles.h2} id="categories-title">
                        Популярные категории
                    </h2>
                    <Link className={styles.link} to="/catalog">
                        Все категории
                    </Link>
                </div>
                <div className={styles.categories}>
                    {categories.map(category => (
                        <Link
                            key={category.value}
                            className={styles.category}
                            to={`/catalog?category=${category.value}`}
                        >
                            <img src={category.image} alt="" />
                            <span className={styles.categoryName}>
                                {category.name}
                                <Icon src={arrowRightIcon} size={18} />
                            </span>
                        </Link>
                    ))}
                </div>
            </section>

            <section className={styles.section} aria-labelledby="products-title">
                <div className={styles.sectionHead}>
                    <div className={styles.headColumn}>
                        <h2 className={styles.h2} id="products-title">
                            Подборка товаров
                        </h2>
                        <div className={styles.tabs} role="tablist" aria-label="Подборка">
                            {tabs.map(tab => (
                                <button
                                    key={tab.id}
                                    type="button"
                                    role="tab"
                                    className={styles.tab}
                                    aria-selected={tab.id === activeTab.id}
                                    onClick={() => setActiveTab(tab)}
                                >
                                    {tab.name}
                                </button>
                            ))}
                        </div>
                    </div>
                    <Link className={styles.link} to={activeTab.link}>
                        Весь каталог
                    </Link>
                </div>
                {!isPending && data?.content.length === 0 ? (
                    <p className={styles.muted}>Здесь пока нет товаров.</p>
                ) : (
                    <ProductGrid products={data?.content} isLoading={isPending} wide />
                )}
            </section>

            <section className={styles.section} aria-labelledby="band-title">
                <div className={styles.band}>
                    <div className={styles.bandText}>
                        <h2 className={styles.h3} id="band-title">
                            Не знаете, что выбрать?
                        </h2>
                        <p className={styles.muted}>
                            Отфильтруйте каталог по цене, бренду и наличию — подходящие
                            товары останутся на экране.
                        </p>
                    </div>
                    <ButtonLink to="/catalog" size="lg">
                        Подобрать в каталоге
                    </ButtonLink>
                </div>
            </section>
        </div>
    );
}
