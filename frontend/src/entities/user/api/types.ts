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
