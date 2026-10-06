export type Role = "user" | "admin";

export interface PrincipalUserState {
    uuid: string;
    name: string;
    email: string;
    avatarUrl: string | null;
    role: Role;
    isBlocked: boolean;
    isAuthenticated: boolean;
    accessToken: string;
    refreshToken: string;
    accessTokenExpiresAt: number;
    refreshTokenExpiresAt: number;
}
