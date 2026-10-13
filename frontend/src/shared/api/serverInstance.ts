import axios, { type AxiosInstance } from "axios";

export function createServerInstance(token?: string | null): AxiosInstance {
    return axios.create({
        baseURL: `${process.env.API_URL ?? "http://localhost:8080"}/api`,
        headers: token ? { Authorization: `Bearer ${token}` } : undefined
    });
}
