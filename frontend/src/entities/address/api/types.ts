export interface AddressResponse {
    uuid: string;
    city: string;
    street: string;
    house: string;
    apartment: string | null;
    postalCode: string | null;
    comment: string | null;
    selected: boolean;
}

export interface AddressRequest {
    city: string;
    street: string;
    house: string;
    apartment?: string;
    postalCode?: string;
    comment?: string;
}
