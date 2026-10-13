import { createSlice, nanoid, type PayloadAction } from "@reduxjs/toolkit";

export interface ToastItem {
    id: string;
    title: string;
    text?: string;
    type: "success" | "error";
}

interface ToastState {
    queue: ToastItem[];
}

const initialState: ToastState = {
    queue: []
};

export const toastSlice = createSlice({
    name: "toast",
    initialState,
    reducers: {
        showToast: {
            reducer(state, action: PayloadAction<ToastItem>) {
                const isDuplicate = state.queue.some(
                    toast =>
                        toast.title === action.payload.title &&
                        toast.text === action.payload.text &&
                        toast.type === action.payload.type
                );
                if (!isDuplicate) {
                    state.queue.push(action.payload);
                }
            },
            prepare(payload: Omit<ToastItem, "id">) {
                return { payload: { ...payload, id: nanoid() } };
            }
        },
        hideToast(state) {
            state.queue.splice(0, 1);
        }
    },
    selectors: {
        selectCurrentToast: state => state.queue[0] ?? null
    }
});

export const { showToast, hideToast } = toastSlice.actions;
export const { selectCurrentToast } = toastSlice.selectors;
