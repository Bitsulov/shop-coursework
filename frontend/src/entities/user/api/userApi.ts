import type { AxiosInstance } from "axios";
import { api } from "shared/api/instance";
import type {
    AuthResponse,
    SendCodeRequest,
    SignInRequest,
    SignUpRequest,
    UserDetailResponse,
    VerifyRequest
} from "./types";

export async function register(data: SignUpRequest) {
    const response = await api.post<string>("/auth/register", data);
    return response.data;
}

export async function sendCode(data: SendCodeRequest) {
    const response = await api.post<string>("/auth/send", data);
    return response.data;
}

export async function verify(data: VerifyRequest) {
    const response = await api.post<AuthResponse>("/auth/verify", data);
    return response.data;
}

export async function login(data: SignInRequest) {
    const response = await api.post<AuthResponse>("/auth/login", data);
    return response.data;
}

export async function getPrincipalUser(client: AxiosInstance = api) {
    const response = await client.get<UserDetailResponse>("/auth/user");
    return response.data;
}
