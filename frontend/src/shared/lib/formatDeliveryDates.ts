export function formatDeliveryDates(from: string, to: string): string {
    const start = new Date(from);
    const end = new Date(to);
    const dayMonth = new Intl.DateTimeFormat("ru-RU", { day: "numeric", month: "long" });

    if (start.toDateString() === end.toDateString()) {
        return dayMonth.format(start);
    }
    if (start.getMonth() === end.getMonth()) {
        return `${start.getDate()}–${dayMonth.format(end)}`;
    }
    return `${dayMonth.format(start)} – ${dayMonth.format(end)}`;
}
