import { describe, expect, it } from "vitest";
import { formatDeliveryDates } from "./formatDeliveryDates";

describe("Даты доставки", () => {
    it("Показывает диапазон дней внутри одного месяца", () => {
        expect(formatDeliveryDates("2026-10-15T09:00:00", "2026-10-17T21:00:00")).toBe(
            "15–17 октября"
        );
    });

    it("Показывает один день, если доставка в течение дня", () => {
        expect(formatDeliveryDates("2026-10-15T09:00:00", "2026-10-15T21:00:00")).toBe(
            "15 октября"
        );
    });

    it("Показывает оба месяца, если диапазон их пересекает", () => {
        expect(formatDeliveryDates("2026-10-30T09:00:00", "2026-11-02T21:00:00")).toBe(
            "30 октября – 2 ноября"
        );
    });
});
