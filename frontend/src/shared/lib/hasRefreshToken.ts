export function hasRefreshToken(request: Request): boolean {
    const cookieHeader = request.headers.get("cookie");
    return cookieHeader !== null && /(?:^|; )refreshToken=[^;]+/.test(cookieHeader);
}
