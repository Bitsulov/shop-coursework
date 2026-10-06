import { useState, type ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

function createQueryClient() {
    return new QueryClient({
        defaultOptions: {
            queries: {
                staleTime: 60 * 1000,
                refetchOnWindowFocus: false
            }
        }
    });
}

export function QueryProvider({ children }: { children: ReactNode }) {
    const [queryClient] = useState(createQueryClient);

    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
}
