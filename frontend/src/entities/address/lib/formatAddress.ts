import type { AddressResponse } from "../api/types";

export function formatAddressLine(address: AddressResponse): string {
    return [
        `${address.street}, ${address.house}`,
        address.apartment && `кв. ${address.apartment}`
    ]
        .filter(Boolean)
        .join(", ");
}

export function formatAddressCity(address: AddressResponse): string {
    return [address.city, address.postalCode].filter(Boolean).join(", ");
}
