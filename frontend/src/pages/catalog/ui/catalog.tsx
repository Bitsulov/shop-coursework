import { useState } from "react";
import { Link, useSearchParams } from "react-router";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import {
    categories,
    getProductFilters,
    getProducts,
    parseCatalogParams,
    toCatalogSearchParams,
    type ProductsParams,
    type ProductSort
} from "entities/product";
import { CatalogFilters } from "widgets/catalogFilters";
import { ProductGrid } from "widgets/productCard";
import { Breadcrumbs } from "shared/ui/breadcrumbs";
import { Button } from "shared/ui/button";
import { Chip } from "shared/ui/chip";
import { Drawer } from "shared/ui/drawer";
import { Icon } from "shared/ui/icon";
import { Pagination } from "shared/ui/pagination";
import { Select } from "shared/ui/select";
import { formatPrice } from "shared/lib/formatPrice";
import { getPluralForm } from "shared/lib/getPluralForm";
import closeIcon from "shared/icons/close.svg";
import filterIcon from "shared/icons/filter.svg";
import searchIcon from "shared/icons/search.svg";
import styles from "./catalog.module.scss";

const pageSize = 24;

const sortOptions: { value: ProductSort; label: string }[] = [
    { value: "new", label: "Сначала новинки" },
    { value: "price_asc", label: "Сначала дешевле" },
    { value: "price_desc", label: "Сначала дороже" },
    { value: "discount", label: "По размеру скидки" }
];

export function Catalog() {
    const [searchParams, setSearchParams] = useSearchParams();
    const [isFiltersOpen, setIsFiltersOpen] = useState(false);
    const params = parseCatalogParams(searchParams);
    const category = categories.find(item => item.value === params.category);

    const { data, isPending } = useQuery({
        queryKey: ["products", params],
        queryFn: () =>
            getProducts({ ...params, page: (params.page ?? 1) - 1, size: pageSize }),
        placeholderData: keepPreviousData
    });

    const { data: filterOptions } = useQuery({
        queryKey: ["productFilters", params.category],
        queryFn: () => getProductFilters(params.category)
    });

    const updateParams = (patch: Partial<ProductsParams>) => {
        setSearchParams(toCatalogSearchParams({ ...params, page: 1, ...patch }), {
            preventScrollReset: true
        });
    };

    const resetFilters = () => {
        setSearchParams(
            toCatalogSearchParams({ category: params.category, sort: params.sort }),
            { preventScrollReset: true }
        );
    };

    const categoryLink = (value?: string) =>
        `/catalog?${toCatalogSearchParams({ category: value as ProductsParams["category"], sort: params.sort })}`;

    const activeFilters = [
        ...(params.brand ?? []).map(brand => ({
            label: brand,
            remove: () =>
                updateParams({ brand: params.brand?.filter(item => item !== brand) })
        })),
        ...(params.color ?? []).map(color => ({
            label: color,
            remove: () =>
                updateParams({ color: params.color?.filter(item => item !== color) })
        })),
        ...(params.minPrice !== undefined || params.maxPrice !== undefined
            ? [
                  {
                      label: [
                          params.minPrice !== undefined &&
                              `от ${formatPrice(params.minPrice)}`,
                          params.maxPrice !== undefined &&
                              `до ${formatPrice(params.maxPrice)}`
                      ]
                          .filter(Boolean)
                          .join(" "),
                      remove: () =>
                          updateParams({ minPrice: undefined, maxPrice: undefined })
                  }
              ]
            : []),
        ...(params.inStock
            ? [{ label: "В наличии", remove: () => updateParams({ inStock: false }) }]
            : []),
        ...(params.sale
            ? [{ label: "Со скидкой", remove: () => updateParams({ sale: false }) }]
            : [])
    ];

    const total = data?.totalElements ?? 0;
    const title = category?.name ?? "Каталог";
    const isEmpty = !isPending && total === 0;

    const filters = (
        <CatalogFilters options={filterOptions} params={params} onChange={updateParams} />
    );

    return (
        <div className="container">
            <Breadcrumbs
                items={[
                    { label: "Главная", to: "/" },
                    ...(category
                        ? [{ label: "Каталог", to: "/catalog" }, { label: category.name }]
                        : [{ label: "Каталог" }])
                ]}
            />

            <div className={styles.heading}>
                <h1 className={styles.title}>{title}</h1>
                {data && (
                    <span className={styles.count} aria-live="polite">
                        {total} {getPluralForm(total, ["товар", "товара", "товаров"])}
                    </span>
                )}
            </div>

            <div className={styles.pills} role="group" aria-label="Категории">
                <Link
                    to={categoryLink()}
                    className={styles.pill}
                    aria-current={!category ? "page" : undefined}
                >
                    Все товары
                </Link>
                {categories.map(item => (
                    <Link
                        key={item.value}
                        to={categoryLink(item.value)}
                        className={styles.pill}
                        aria-current={item.value === params.category ? "page" : undefined}
                    >
                        {item.name}
                    </Link>
                ))}
            </div>

            <div className={styles.catalog}>
                <aside className={styles.side} aria-label="Категории и фильтры">
                    <nav className={styles.categoryNav} aria-label="Категории">
                        <Link
                            to={categoryLink()}
                            aria-current={!category ? "page" : undefined}
                        >
                            Все товары
                        </Link>
                        {categories.map(item => (
                            <Link
                                key={item.value}
                                to={categoryLink(item.value)}
                                aria-current={
                                    item.value === params.category ? "page" : undefined
                                }
                            >
                                {item.name}
                            </Link>
                        ))}
                    </nav>
                    <div className={styles.divider} />
                    {filters}
                </aside>

                <div className={styles.main}>
                    <div className={styles.toolbar}>
                        <Button
                            size="sm"
                            className={styles.filterButton}
                            aria-expanded={isFiltersOpen}
                            onClick={() => setIsFiltersOpen(true)}
                        >
                            <Icon src={filterIcon} size={18} />
                            Фильтры
                            {activeFilters.length > 0 && (
                                <span className={styles.filterCount}>
                                    {activeFilters.length}
                                </span>
                            )}
                        </Button>
                        {activeFilters.length > 0 && (
                            <div className={styles.chips} aria-label="Выбранные фильтры">
                                {activeFilters.map(filter => (
                                    <Chip
                                        key={filter.label}
                                        removable
                                        aria-label={`Убрать фильтр: ${filter.label}`}
                                        onClick={filter.remove}
                                    >
                                        {filter.label}
                                        <Icon src={closeIcon} size={16} />
                                    </Chip>
                                ))}
                                <Button variant="text" size="sm" onClick={resetFilters}>
                                    Сбросить все
                                </Button>
                            </div>
                        )}
                        <label className={styles.srOnly} htmlFor="catalog-sort">
                            Сортировка
                        </label>
                        <Select
                            id="catalog-sort"
                            className={styles.sort}
                            ghost
                            options={sortOptions}
                            value={params.sort}
                            onChange={event =>
                                updateParams({ sort: event.target.value as ProductSort })
                            }
                        />
                    </div>

                    {isEmpty ? (
                        <div className={styles.empty}>
                            <span className={styles.emptyIcon}>
                                <Icon src={searchIcon} size={30} />
                            </span>
                            <h2 className={styles.emptyTitle}>Ничего не нашлось</h2>
                            <p>
                                Под выбранные фильтры не подходит ни один товар.
                                Попробуйте расширить диапазон цены или убрать часть
                                условий.
                            </p>
                            {activeFilters.length > 0 && (
                                <Button onClick={resetFilters}>Сбросить фильтры</Button>
                            )}
                        </div>
                    ) : (
                        <>
                            <ProductGrid
                                products={data?.content}
                                isLoading={isPending}
                                skeletonCount={6}
                            />
                            <Pagination
                                page={params.page ?? 1}
                                totalPages={data?.totalPages ?? 0}
                                onChange={page =>
                                    setSearchParams(
                                        toCatalogSearchParams({ ...params, page })
                                    )
                                }
                            />
                        </>
                    )}
                </div>
            </div>

            <Drawer
                open={isFiltersOpen}
                onClose={() => setIsFiltersOpen(false)}
                label="Фильтры"
                title={<h2 className={styles.drawerTitle}>Фильтры</h2>}
                footer={
                    <div className={styles.drawerActions}>
                        <Button
                            variant="text"
                            disabled={activeFilters.length === 0}
                            onClick={resetFilters}
                        >
                            Сбросить
                        </Button>
                        <Button
                            className={styles.drawerShow}
                            onClick={() => setIsFiltersOpen(false)}
                        >
                            {total > 0
                                ? `Показать ${total} ${getPluralForm(total, ["товар", "товара", "товаров"])}`
                                : "Нет подходящих товаров"}
                        </Button>
                    </div>
                }
            >
                <div className={styles.drawerBody}>{filters}</div>
            </Drawer>
        </div>
    );
}
