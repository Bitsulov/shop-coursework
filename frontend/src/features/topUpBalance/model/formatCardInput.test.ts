import { describe, expect, it } from "vitest";
import { formatCardNumber, formatExpiry } from "./formatCardInput";

describe("Форматирование данных карты", () => {
    it("Делит номер карты на группы по четыре цифры", () => {
        expect(formatCardNumber("4000000000000001")).toBe("4000 0000 0000 0001");
    });

    it("Убирает лишние символы и обрезает номер до 16 цифр", () => {
        expect(formatCardNumber("4000-0000-0000-0001-99")).toBe("4000 0000 0000 0001");
    });

    it("Ставит косую черту после месяца", () => {
        expect(formatExpiry("1227")).toBe("12/27");
        expect(formatExpiry("1")).toBe("1");
    });
});
