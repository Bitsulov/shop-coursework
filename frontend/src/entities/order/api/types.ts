export interface OrderRequest {
    recipientName: string;
    recipientPhone: string;
    addressUuid: string;
}

export interface OrderProductResponse {
    name: string;
    price: number;
    oldPrice: number | null;
    quantity: number;
}

export interface OrderResponse {
    uuid: string;
    recipientName: string;
    recipientPhone: string;
    address: string;
    products: OrderProductResponse[];
    subtotal: number;
    discount: number;
    total: number;
    balance: number;
    deliveryFrom: string;
    deliveryTo: string;
    createdAt: string;
}
