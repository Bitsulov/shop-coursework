import axios, { type InternalAxiosRequestConfig } from "axios";
import { decryptFromString, encryptToString } from "../lib/crypto";
import { getCookieByName } from "../lib/getCookieByName";
import { clearCookiesTokens } from "../lib/clearCookiesTokens";
import { refreshIgnoreEndpoints } from "../const/const";

export const api = axios.create({
    baseURL: "/api"
});

api.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
    if (config.url && refreshIgnoreEndpoints.includes(config.url)) {
        return config;
    }

    const accessTokenEncrypted = getCookieByName("accessToken");

    if (accessTokenEncrypted) {
        const accessToken = await decryptFromString(
            accessTokenEncrypted,
            import.meta.env.VITE_CRYPTO_KEY
        );
        config.headers.Authorization = `Bearer ${accessToken}`;
    } else {
        const refreshTokenEncrypted = getCookieByName("refreshToken");

        if (refreshTokenEncrypted) {
            const refreshToken = await decryptFromString(
                refreshTokenEncrypted,
                import.meta.env.VITE_CRYPTO_KEY
            );

            try {
                const response = await api.post("/auth/refresh", { refreshToken });

                const encryptedAccessToken = await encryptToString(
                    response.data.accessToken,
                    import.meta.env.VITE_CRYPTO_KEY
                );
                const accessTokenExpiresIn = response.data.accessTokenExpiresIn;

                const encryptedRefreshToken = await encryptToString(
                    response.data.refreshToken,
                    import.meta.env.VITE_CRYPTO_KEY
                );
                const refreshTokenExpiresIn = response.data.refreshTokenExpiresIn;

                const accessTokenExpiresAt = Date.now() + accessTokenExpiresIn;
                const refreshTokenExpiresAt = Date.now() + refreshTokenExpiresIn;

                clearCookiesTokens();
                document.cookie = `accessToken=${encryptedAccessToken}; max-age=${accessTokenExpiresIn / 1000}; SameSite=Lax; Secure; path=/`;
                document.cookie = `accessTokenExpiresAt=${accessTokenExpiresAt}; max-age=${accessTokenExpiresIn / 1000}; SameSite=Lax; Secure; path=/`;
                document.cookie = `refreshToken=${encryptedRefreshToken}; max-age=${refreshTokenExpiresIn / 1000}; SameSite=Lax; Secure; path=/`;
                document.cookie = `refreshTokenExpiresAt=${refreshTokenExpiresAt}; max-age=${refreshTokenExpiresIn / 1000}; SameSite=Lax; Secure; path=/`;

                config.headers.Authorization = `Bearer ${response.data.accessToken}`;
            } catch (error) {
                if (axios.isAxiosError(error) && error.response?.status === 401) {
                    clearCookiesTokens();
                }
            }
        }
    }

    return config;
});
