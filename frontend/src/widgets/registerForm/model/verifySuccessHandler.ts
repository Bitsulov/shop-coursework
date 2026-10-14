import type { Dispatch } from "@reduxjs/toolkit";
import type { QueryClient } from "@tanstack/react-query";
import { setUserInfo, type AuthResponse } from "entities/user";
import { encryptToString } from "shared/lib/crypto";

export async function verifySuccessHandler(
    response: AuthResponse,
    email: string,
    dispatch: Dispatch,
    queryClient: QueryClient
) {
    const encryptedAccessToken = await encryptToString(
        response.accessToken,
        import.meta.env.VITE_CRYPTO_KEY
    );
    const encryptedRefreshToken = await encryptToString(
        response.refreshToken,
        import.meta.env.VITE_CRYPTO_KEY
    );

    const accessTokenExpiresIn = response.accessTokenExpiresIn;
    const refreshTokenExpiresIn = response.refreshTokenExpiresIn;
    const accessTokenExpiresAt = Date.now() + accessTokenExpiresIn;
    const refreshTokenExpiresAt = Date.now() + refreshTokenExpiresIn;

    document.cookie = `accessToken=${encryptedAccessToken}; max-age=${accessTokenExpiresIn / 1000}; SameSite=Lax; Secure; path=/`;
    document.cookie = `accessTokenExpiresAt=${accessTokenExpiresAt}; max-age=${accessTokenExpiresIn / 1000}; SameSite=Lax; Secure; path=/`;
    document.cookie = `refreshToken=${encryptedRefreshToken}; max-age=${refreshTokenExpiresIn / 1000}; SameSite=Lax; Secure; path=/`;
    document.cookie = `refreshTokenExpiresAt=${refreshTokenExpiresAt}; max-age=${refreshTokenExpiresIn / 1000}; SameSite=Lax; Secure; path=/`;

    dispatch(
        setUserInfo({
            uuid: response.uuid,
            email,
            isAuthenticated: true,
            accessToken: encryptedAccessToken,
            refreshToken: encryptedRefreshToken,
            accessTokenExpiresAt,
            refreshTokenExpiresAt
        })
    );
    await queryClient.invalidateQueries({ queryKey: ["user"] });
}
