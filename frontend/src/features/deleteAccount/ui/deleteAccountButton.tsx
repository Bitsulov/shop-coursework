import { useState } from "react";
import { useNavigate } from "react-router";
import { useDispatch } from "react-redux";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import axios from "axios";
import { deleteAccount, resetUser } from "entities/user";
import { showToast } from "features/toast";
import { Button } from "shared/ui/button";
import { Dialog } from "shared/ui/dialog";
import { Field } from "shared/ui/field";
import { clearCookiesTokens } from "shared/lib/clearCookiesTokens";
import { formatPrice } from "shared/lib/formatPrice";
import styles from "./deleteAccountButton.module.scss";

interface DeleteAccountButtonProps {
    balance: number;
}

export function DeleteAccountButton({ balance }: DeleteAccountButtonProps) {
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const queryClient = useQueryClient();
    const [isOpen, setIsOpen] = useState(false);
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | undefined>();

    const deleteMutation = useMutation({
        mutationFn: deleteAccount,
        onSuccess: () => {
            clearCookiesTokens();
            dispatch(resetUser());
            queryClient.removeQueries();
            dispatch(showToast({ title: "Аккаунт удалён", type: "success" }));
            navigate("/");
        },
        onError: err => {
            const isWrongPassword =
                axios.isAxiosError(err) && err.response?.status === 403;
            setError(
                isWrongPassword
                    ? "Неверный пароль. Попробуйте ещё раз"
                    : "Не удалось удалить аккаунт. Попробуйте позже"
            );
        }
    });

    const close = () => {
        setIsOpen(false);
        setPassword("");
        setError(undefined);
    };

    return (
        <>
            <Button
                variant="text"
                className={styles.danger}
                onClick={() => setIsOpen(true)}
            >
                Удалить аккаунт
            </Button>
            <Dialog open={isOpen} onClose={close} title="Удалить аккаунт?">
                <form
                    className={styles.form}
                    noValidate
                    onSubmit={event => {
                        event.preventDefault();
                        if (password) deleteMutation.mutate(password);
                    }}
                >
                    <p className={styles.text}>
                        Адреса, корзина и баланс {formatPrice(balance)} будут удалены без
                        возможности восстановления. Для подтверждения введите текущий
                        пароль.
                    </p>
                    <Field
                        label="Текущий пароль"
                        type="password"
                        autoComplete="current-password"
                        value={password}
                        error={error}
                        onChange={event => {
                            setPassword(event.target.value);
                            setError(undefined);
                        }}
                    />
                    <div className={styles.actions}>
                        <Button onClick={close}>Отмена</Button>
                        <Button
                            type="submit"
                            variant="danger"
                            disabled={!password || deleteMutation.isPending}
                        >
                            {deleteMutation.isPending ? "Удаляем…" : "Удалить аккаунт"}
                        </Button>
                    </div>
                </form>
            </Dialog>
        </>
    );
}
