export type Category =
    "phones" | "laptops" | "tv" | "large_appliances" | "small_appliances" | "audio";

export const categories: { value: Category; name: string }[] = [
    { value: "phones", name: "Смартфоны и гаджеты" },
    { value: "laptops", name: "Ноутбуки" },
    { value: "tv", name: "Телевизоры" },
    { value: "large_appliances", name: "Крупная техника" },
    { value: "small_appliances", name: "Мелкая техника" },
    { value: "audio", name: "Аудио" }
];
