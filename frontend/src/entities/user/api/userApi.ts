import type { AxiosInstance } from "axios";
import { api } from "shared/api/instance";
import type {
    AuthResponse,
    BalanceTopUpRequest,
    SendCodeRequest,
    SignInRequest,
    SignUpRequest,
    UpdateUserRequest,
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

export async function topUpBalance(data: BalanceTopUpRequest) {
    const response = await api.post<UserDetailResponse>("/users/balance", data);
    return response.data;
}

export async function updateUser({ details, avatar }: UpdateUserRequest) {
    const formData = new FormData();
    formData.append(
        "details",
        new Blob([JSON.stringify(details)], { type: "application/json" })
    );
    if (avatar) {
        formData.append("avatar", avatar);
    }

    const response = await api.put<UserDetailResponse>("/users", formData);
    return response.data;
}

export async function deleteAccount(password: string) {
    await api.delete("/users", { data: { password } });
}

export async function getPrincipalUser(client: AxiosInstance = api) {
    const response = await client.get<UserDetailResponse>("/auth/user");
    return response.data;
}
