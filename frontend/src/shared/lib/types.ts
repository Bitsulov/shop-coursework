export interface AppError {
    statusCode: number;
    message: string;
    path: string;
    timestamp: string;
}

export interface Page<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}
