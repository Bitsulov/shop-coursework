import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type { PrincipalUserState } from "./types";

const initialState: PrincipalUserState = {
    uuid: "",
    name: "",
    email: "",
    avatarUrl: null,
    role: "user",
    isBlocked: false,
    isAuthenticated: false,
    accessToken: "",
    refreshToken: "",
    accessTokenExpiresAt: 0,
    refreshTokenExpiresAt: 0
};

export const userSlice = createSlice({
    name: "user",
    initialState,
    reducers: {
        setUserInfo(state, action: PayloadAction<Partial<PrincipalUserState>>) {
            Object.assign(state, action.payload);
        },
        resetUser() {
            return initialState;
        }
    },
    selectors: {
        selectUser: state => state,
        selectIsAuthenticated: state => state.isAuthenticated
    }
});

export const { setUserInfo, resetUser } = userSlice.actions;
export const { selectUser, selectIsAuthenticated } = userSlice.selectors;
