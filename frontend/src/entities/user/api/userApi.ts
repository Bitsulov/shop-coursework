import type { AxiosInstance } from "axios";
import { api } from "shared/api/instance";
import type { UserDetailResponse } from "./types";

export async function getPrincipalUser(client: AxiosInstance = api) {
    const response = await client.get<UserDetailResponse>("/auth/user");
    return response.data;
}
