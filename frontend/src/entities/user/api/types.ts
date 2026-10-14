import type { Role } from "../model/types";

export interface UserDetailResponse {
    uuid: string;
    name: string;
    avatarUrl: string | null;
    email: string;
    phone: string | null;
    balance: number;
    role: Role;
    active: boolean;
    blocked: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface SignUpRequest {
    email: string;
    password: string;
    confirmPassword: string;
    name: string;
    phone?: string;
}

export interface SendCodeRequest {
    email: string;
}

export interface VerifyRequest {
    email: string;
    verificationCode: string;
}

export interface SignInRequest {
    email: string;
    password: string;
}

export interface BalanceTopUpRequest {
    amount: number;
    cardNumber: string;
    expiry: string;
    cvc: string;
}

export interface AuthResponse {
    uuid: string;
    accessToken: string;
    refreshToken: string;
    accessTokenExpiresIn: number;
    refreshTokenExpiresIn: number;
}
