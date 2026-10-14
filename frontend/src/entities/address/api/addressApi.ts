import { api } from "shared/api/instance";
import type { AddressRequest, AddressResponse } from "./types";

export async function getAddresses() {
    const response = await api.get<AddressResponse[]>("/users/addresses");
    return response.data;
}

export async function createAddress(data: AddressRequest) {
    const response = await api.post<AddressResponse>("/users/addresses", data);
    return response.data;
}

export async function updateAddress({
    uuid,
    data
}: {
    uuid: string;
    data: AddressRequest;
}) {
    const response = await api.put<AddressResponse>(`/users/addresses/${uuid}`, data);
    return response.data;
}

export async function selectAddress(uuid: string) {
    const response = await api.patch<AddressResponse>(`/users/addresses/${uuid}/select`);
    return response.data;
}

export async function deleteAddress(uuid: string) {
    await api.delete(`/users/addresses/${uuid}`);
}
