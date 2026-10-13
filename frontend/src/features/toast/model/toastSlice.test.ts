import { describe, expect, it } from "vitest";
import { hideToast, showToast, toastSlice } from "./toastSlice";

const reducer = toastSlice.reducer;

describe("Очередь уведомлений", () => {
    it("Добавляет уведомление в очередь", () => {
        const state = reducer(undefined, showToast({ title: "Готово", type: "success" }));

        expect(state.queue).toHaveLength(1);
        expect(state.queue[0]).toMatchObject({ title: "Готово", type: "success" });
    });

    it("Не добавляет такое же уведомление повторно", () => {
        let state = reducer(undefined, showToast({ title: "Готово", type: "success" }));
        state = reducer(state, showToast({ title: "Готово", type: "success" }));

        expect(state.queue).toHaveLength(1);
    });

    it("Добавляет уведомление с тем же заголовком, но другим текстом", () => {
        let state = reducer(
            undefined,
            showToast({ title: "Добавлено", text: "Чайник", type: "success" })
        );
        state = reducer(
            state,
            showToast({ title: "Добавлено", text: "Пылесос", type: "success" })
        );

        expect(state.queue).toHaveLength(2);
    });

    it("Убирает первое уведомление из очереди", () => {
        let state = reducer(undefined, showToast({ title: "Первое", type: "success" }));
        state = reducer(state, showToast({ title: "Второе", type: "error" }));
        state = reducer(state, hideToast());

        expect(state.queue.map(toast => toast.title)).toEqual(["Второе"]);
    });

    it("Не падает при пустой очереди", () => {
        expect(reducer(undefined, hideToast()).queue).toEqual([]);
    });
});
