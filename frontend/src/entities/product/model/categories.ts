import imgPhones from "shared/icons/imgPhones.svg";
import imgLaptops from "shared/icons/imgLaptops.svg";
import imgTv from "shared/icons/imgTv.svg";
import imgLargeAppliances from "shared/icons/imgLargeAppliances.svg";
import imgSmallAppliances from "shared/icons/imgSmallAppliances.svg";
import imgAudio from "shared/icons/imgAudio.svg";

export type Category =
    "phones" | "laptops" | "tv" | "large_appliances" | "small_appliances" | "audio";

export const categories: { value: Category; name: string; image: string }[] = [
    { value: "phones", name: "Смартфоны и гаджеты", image: imgPhones },
    { value: "laptops", name: "Ноутбуки", image: imgLaptops },
    { value: "tv", name: "Телевизоры", image: imgTv },
    { value: "large_appliances", name: "Крупная техника", image: imgLargeAppliances },
    { value: "small_appliances", name: "Мелкая техника", image: imgSmallAppliances },
    { value: "audio", name: "Аудио", image: imgAudio }
];

export function isCategory(value: string | null): value is Category {
    return categories.some(category => category.value === value);
}
