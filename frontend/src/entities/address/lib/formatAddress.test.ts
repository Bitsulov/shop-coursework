import { describe, expect, it } from "vitest";
import { formatAddressCity, formatAddressLine } from "./formatAddress";

const address = {
    uuid: "1",
    city: "Москва",
    street: "Тверская",
    house: "5к2",
    apartment: "12",
    postalCode: "125009",
    comment: null,
    selected: true
};

describe("Строки адреса", () => {
    it("Собирает улицу, дом и квартиру", () => {
        expect(formatAddressLine(address)).toBe("Тверская, 5к2, кв. 12");
    });

    it("Не добавляет квартиру, если её нет", () => {
        expect(formatAddressLine({ ...address, apartment: null })).toBe("Тверская, 5к2");
    });

    it("Собирает город и индекс", () => {
        expect(formatAddressCity(address)).toBe("Москва, 125009");
        expect(formatAddressCity({ ...address, postalCode: null })).toBe("Москва");
    });
});
