import { useEffect, type ReactNode } from "react";
import { useDispatch } from "react-redux";
import { useQuery } from "@tanstack/react-query";
import axios from "axios";
import { getPrincipalUser, resetUser, setUserInfo } from "entities/user";

interface InitProviderProps {
    children: ReactNode;
}

export function InitProvider({ children }: InitProviderProps) {
    const dispatch = useDispatch();

    const { data, error } = useQuery({
        queryKey: ["user"],
        queryFn: () => getPrincipalUser()
    });

    useEffect(() => {
        if (data) {
            dispatch(
                setUserInfo({
                    uuid: data.uuid,
                    name: data.name,
                    email: data.email,
                    avatarUrl: data.avatarUrl,
                    role: data.role,
                    isBlocked: data.blocked,
                    isAuthenticated: true
                })
            );
        }
    }, [data, dispatch]);

    useEffect(() => {
        if (axios.isAxiosError(error) && error.response?.status === 401) {
            dispatch(resetUser());
        }
    }, [error, dispatch]);

    return children;
}
