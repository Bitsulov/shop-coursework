import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router";
import { useDispatch } from "react-redux";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { login } from "entities/user";
import { Alert } from "shared/ui/alert";
import { Button } from "shared/ui/button";
import { Field } from "shared/ui/field";
import { authFormSchema, type AuthFormValues } from "../model/schema";
import { loginSuccessHandler } from "../model/loginSuccessHandler";
import { loginErrorHandler } from "../model/loginErrorHandler";
import styles from "./authForm.module.scss";

export function AuthForm() {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [searchParams] = useSearchParams();
    const [serverError, setServerError] = useState<string | null>(null);

    const redirect = searchParams.get("redirect");
    const redirectTo =
        redirect?.startsWith("/") && !redirect.startsWith("//") ? redirect : "/";

    const {
        register,
        handleSubmit,
        formState: { errors }
    } = useForm<AuthFormValues>({
        resolver: zodResolver(authFormSchema),
        defaultValues: { email: "", password: "" }
    });

    const loginMutation = useMutation({
        mutationFn: login,
        onSuccess: (response, request) =>
            loginSuccessHandler(
                response,
                request,
                dispatch,
                queryClient,
                navigate,
                redirectTo
            ),
        onError: error => setServerError(loginErrorHandler(error))
    });

    return (
        <div className={styles.card}>
            <div className={styles.heading}>
                <h1 className={styles.title}>Вход</h1>
                <p className={styles.muted}>
                    Войдите, чтобы оформлять заказы и пополнять баланс.
                </p>
            </div>

            {serverError && <Alert>{serverError}</Alert>}

            <form
                className={styles.form}
                noValidate
                onSubmit={handleSubmit(data => {
                    setServerError(null);
                    loginMutation.mutate(data);
                })}
            >
                <Field
                    label="Email"
                    type="email"
                    autoComplete="email"
                    error={errors.email?.message}
                    {...register("email")}
                />
                <Field
                    label="Пароль"
                    type="password"
                    autoComplete="current-password"
                    error={errors.password?.message}
                    {...register("password")}
                />
                <Button
                    type="submit"
                    variant="primary"
                    size="lg"
                    block
                    className={styles.submit}
                    disabled={loginMutation.isPending}
                >
                    {loginMutation.isPending ? "Входим…" : "Войти"}
                </Button>
            </form>

            <div className={styles.divider} />
            <p className={styles.switch}>
                Нет аккаунта?{" "}
                <Link className={styles.link} to="/register">
                    Зарегистрироваться
                </Link>
            </p>
        </div>
    );
}
