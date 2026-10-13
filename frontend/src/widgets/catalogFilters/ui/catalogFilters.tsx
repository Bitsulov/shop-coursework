import { useState, type KeyboardEvent, type ReactNode } from "react";
import clsx from "clsx";
import type { ProductFiltersResponse, ProductsParams } from "entities/product";
import { Checkbox } from "shared/ui/checkbox";
import { Chip } from "shared/ui/chip";
import { Icon } from "shared/ui/icon";
import chevronDownIcon from "shared/icons/chevronDown.svg";
import { pricePresets } from "../config/pricePresets";
import styles from "./catalogFilters.module.scss";

interface CatalogFiltersProps {
    options?: ProductFiltersResponse;
    params: ProductsParams;
    onChange: (patch: Partial<ProductsParams>) => void;
}

function Facet({ title, children }: { title: string; children: ReactNode }) {
    const [isOpen, setIsOpen] = useState(true);

    return (
        <div className={clsx(styles.facet, !isOpen && styles.isCollapsed)}>
            <button
                type="button"
                className={styles.facetTitle}
                aria-expanded={isOpen}
                onClick={() => setIsOpen(!isOpen)}
            >
                {title}
                <Icon src={chevronDownIcon} size={18} />
            </button>
            <div className={styles.facetBody}>
                <div>{children}</div>
            </div>
        </div>
    );
}

function toggleValue(values: string[] = [], value: string) {
    return values.includes(value)
        ? values.filter(item => item !== value)
        : [...values, value];
}

function PriceFields({
    minPrice,
    maxPrice,
    onChange
}: {
    minPrice?: number;
    maxPrice?: number;
    onChange: CatalogFiltersProps["onChange"];
}) {
    const [min, setMin] = useState(minPrice?.toString() ?? "");
    const [max, setMax] = useState(maxPrice?.toString() ?? "");
    const isInvalid = min !== "" && max !== "" && Number(min) > Number(max);

    const apply = () => {
        if (isInvalid) return;
        const nextMin = min === "" ? undefined : Number(min);
        const nextMax = max === "" ? undefined : Number(max);
        if (nextMin !== minPrice || nextMax !== maxPrice) {
            onChange({ minPrice: nextMin, maxPrice: nextMax });
        }
    };

    const onKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
        if (event.key === "Enter") apply();
    };

    return (
        <>
            <div className={styles.range}>
                <label className={styles.rangeField}>
                    <span aria-hidden="true">от</span>
                    <input
                        className={clsx(styles.input, isInvalid && styles.isInvalid)}
                        inputMode="numeric"
                        aria-label="Цена от, ₽"
                        value={min}
                        onChange={event => setMin(event.target.value.replace(/\D/g, ""))}
                        onBlur={apply}
                        onKeyDown={onKeyDown}
                    />
                </label>
                <label className={styles.rangeField}>
                    <span aria-hidden="true">до</span>
                    <input
                        className={clsx(styles.input, isInvalid && styles.isInvalid)}
                        inputMode="numeric"
                        aria-label="Цена до, ₽"
                        value={max}
                        onChange={event => setMax(event.target.value.replace(/\D/g, ""))}
                        onBlur={apply}
                        onKeyDown={onKeyDown}
                    />
                </label>
            </div>
            {isInvalid && (
                <p className={styles.error} role="alert">
                    «От» больше, чем «до»
                </p>
            )}
        </>
    );
}

export function CatalogFilters({ options, params, onChange }: CatalogFiltersProps) {
    return (
        <div className={styles.filters}>
            <Facet title="Цена, ₽">
                <PriceFields
                    key={`${params.minPrice}-${params.maxPrice}`}
                    minPrice={params.minPrice}
                    maxPrice={params.maxPrice}
                    onChange={onChange}
                />
                <div className={styles.presets}>
                    {pricePresets.map(preset => {
                        const isSelected =
                            params.minPrice === preset.minPrice &&
                            params.maxPrice === preset.maxPrice;
                        return (
                            <Chip
                                key={preset.label}
                                size="sm"
                                selected={isSelected}
                                onClick={() =>
                                    onChange(
                                        isSelected
                                            ? { minPrice: undefined, maxPrice: undefined }
                                            : {
                                                  minPrice: preset.minPrice,
                                                  maxPrice: preset.maxPrice
                                              }
                                    )
                                }
                            >
                                {preset.label}
                            </Chip>
                        );
                    })}
                </div>
            </Facet>

            {options && options.brands.length > 0 && (
                <Facet title="Бренд">
                    <div className={styles.options} role="group" aria-label="Бренд">
                        {options.brands.map(brand => (
                            <Checkbox
                                key={brand}
                                label={brand}
                                checked={params.brand?.includes(brand) ?? false}
                                onChange={() =>
                                    onChange({ brand: toggleValue(params.brand, brand) })
                                }
                            />
                        ))}
                    </div>
                </Facet>
            )}

            {options && options.colors.length > 0 && (
                <Facet title="Цвет">
                    <div className={styles.options} role="group" aria-label="Цвет">
                        {options.colors.map(color => (
                            <Checkbox
                                key={color}
                                label={color}
                                checked={params.color?.includes(color) ?? false}
                                onChange={() =>
                                    onChange({ color: toggleValue(params.color, color) })
                                }
                            />
                        ))}
                    </div>
                </Facet>
            )}

            <Facet title="Наличие и акции">
                <div className={styles.options}>
                    <Checkbox
                        label="Только в наличии"
                        checked={params.inStock ?? false}
                        onChange={() => onChange({ inStock: !params.inStock })}
                    />
                    <Checkbox
                        label="Со скидкой"
                        checked={params.sale ?? false}
                        onChange={() => onChange({ sale: !params.sale })}
                    />
                </div>
            </Facet>
        </div>
    );
}
