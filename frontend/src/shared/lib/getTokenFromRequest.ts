import { decryptFromString } from "./crypto";

function getCookieValueFromHeader(cookieHeader: string, name: string): string | null {
    const match = cookieHeader.match(new RegExp(`(?:^|; )${name}=([^;]*)`));
    return match ? decodeURIComponent(match[1]) : null;
}

export async function getTokenFromRequest(request: Request): Promise<string | null> {
    const cookieHeader = request.headers.get("cookie");
    if (!cookieHeader) return null;

    const encryptedAccessToken = getCookieValueFromHeader(cookieHeader, "accessToken");
    if (!encryptedAccessToken) return null;

    try {
        return await decryptFromString(
            encryptedAccessToken,
            import.meta.env.VITE_CRYPTO_KEY
        );
    } catch {
        return null;
    }
}
