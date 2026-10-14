import { useLocation, useNavigate } from "react-router";
import { useDispatch, useSelector } from "react-redux";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import axios from "axios";
import { selectIsAuthenticated } from "entities/user";
import { getCart, setCartProductQuantity } from "entities/cart";
import type { ProductCardResponse } from "entities/product";
import { showToast } from "features/toast";
import { Button } from "shared/ui/button";
import { Icon } from "shared/ui/icon";
import cartIcon from "shared/icons/cart.svg";

interface AddToCartButtonProps {
    product: Pick<ProductCardResponse, "uuid" | "name" | "stock">;
    size?: "sm" | "md" | "lg";
    className?: string;
}

export function AddToCartButton({
    product,
    size = "sm",
    className
}: AddToCartButtonProps) {
    const navigate = useNavigate();
    const location = useLocation();
    const dispatch = useDispatch();
    const queryClient = useQueryClient();
    const isAuthenticated = useSelector(selectIsAuthenticated);

    const { data: cart } = useQuery({
        queryKey: ["cart"],
        queryFn: getCart,
        enabled: isAuthenticated
    });

    const addMutation = useMutation({
        mutationFn: setCartProductQuantity,
        onSuccess: data => {
            queryClient.setQueryData(["cart"], data);
            dispatch(
                showToast({
                    title: "Добавлено в корзину",
                    text: product.name,
                    type: "success"
                })
            );
        },
        onError: error => {
            const isStockError =
                axios.isAxiosError(error) && error.response?.status === 409;
            dispatch(
                showToast({
                    title: isStockError
                        ? "Больше нет в наличии"
                        : "Не удалось добавить в корзину",
                    text: product.name,
                    type: "error"
                })
            );
        }
    });

    if (product.stock <= 0) {
        return (
            <Button size={size} block className={className} disabled>
                Нет в наличии
            </Button>
        );
    }

    const onClick = () => {
        if (!isAuthenticated) {
            navigate(
                `/login?redirect=${encodeURIComponent(location.pathname + location.search)}`
            );
            return;
        }
        const quantity =
            cart?.products.find(item => item.product.uuid === product.uuid)?.quantity ??
            0;
        addMutation.mutate({ productUuid: product.uuid, quantity: quantity + 1 });
    };

    return (
        <Button
            variant="primary"
            size={size}
            block
            className={className}
            disabled={addMutation.isPending}
            aria-label={`В корзину: ${product.name}`}
            onClick={onClick}
        >
            <Icon src={cartIcon} size={size === "lg" ? 20 : 18} />В корзину
        </Button>
    );
}
