import { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { useForm, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router";
import clsx from "clsx";
import { register as registerUser, sendCode, verify } from "entities/user";
import { showToast } from "features/toast";
import { Alert } from "shared/ui/alert";
import { Button, ButtonLink } from "shared/ui/button";
import { CodeInput } from "shared/ui/codeInput";
import { Field } from "shared/ui/field";
import { Icon } from "shared/ui/icon";
import arrowLeftIcon from "shared/icons/arrowLeft.svg";
import checkIcon from "shared/icons/check.svg";
import {
    passwordRules,
    registerFormSchema,
    type RegisterFormInput,
    type RegisterFormValues
} from "../model/schema";
import { registerErrorHandler } from "../model/registerErrorHandler";
import { verifyErrorHandler } from "../model/verifyErrorHandler";
import { sendCodeErrorHandler } from "../model/sendCodeErrorHandler";
import { verifySuccessHandler } from "../model/verifySuccessHandler";
import { maskEmail } from "../model/maskEmail";
import { resendDelayConst } from "../const/resendDelayConst";
import styles from "./registerForm.module.scss";

type Step = "form" | "code" | "done";

export function RegisterForm() {
    const dispatch = useDispatch();
    const queryClient = useQueryClient();
    const [step, setStep] = useState<Step>("form");
    const [email, setEmail] = useState("");
    const [code, setCode] = useState("");
    const [serverError, setServerError] = useState<string | null>(null);
    const [codeError, setCodeError] = useState<string | null>(null);
    const [codeSentAt, setCodeSentAt] = useState(0);
    const [now, setNow] = useState(0);

    const {
        register,
        handleSubmit,
        control,
        formState: { errors }
    } = useForm<RegisterFormInput, unknown, RegisterFormValues>({
        resolver: zodResolver(registerFormSchema),
        defaultValues: {
            name: "",
            email: "",
            phone: "",
            password: "",
            confirmPassword: ""
        }
    });
    const passwordValue = useWatch({ control, name: "password" });

    const secondsLeft = Math.max(
        0,
        Math.ceil(resendDelayConst - (now - codeSentAt) / 1000)
    );

    useEffect(() => {
        if (step !== "code") return;
        const interval = setInterval(() => setNow(Date.now()), 1000);
        return () => clearInterval(interval);
    }, [step]);

    const startCodeStep = (sentTo: string) => {
        const sentAt = Date.now();
        setEmail(sentTo);
        setCode("");
        setCodeError(null);
        setCodeSentAt(sentAt);
        setNow(sentAt);
        setStep("code");
    };

    const registerMutation = useMutation({
        mutationFn: registerUser,
        onSuccess: (_, request) => startCodeStep(request.email),
        onError: error => setServerError(registerErrorHandler(error))
    });

    const verifyMutation = useMutation({
        mutationFn: verify,
        onSuccess: async response => {
            await verifySuccessHandler(response, email, dispatch, queryClient);
            setStep("done");
        },
        onError: error => {
            setCodeError(verifyErrorHandler(error));
            setCode("");
        }
    });

    const sendCodeMutation = useMutation({
        mutationFn: sendCode,
        onSuccess: () => {
            startCodeStep(email);
            dispatch(
                showToast({ title: "Новый код отправлен", text: email, type: "success" })
            );
        },
        onError: error => setCodeError(sendCodeErrorHandler(error))
    });

    const onCodeChange = (value: string) => {
        setCode(value);
        setCodeError(null);
        if (value.length === 6) {
            verifyMutation.mutate({ email, verificationCode: value });
        }
    };

    if (step === "done") {
        return (
            <div className={clsx(styles.card, styles.done)}>
                <span className={styles.successMark}>
                    <Icon src={checkIcon} size={34} />
                </span>
                <h1 className={styles.title}>Аккаунт создан</h1>
                <p className={styles.muted}>
                    Почта {email} подтверждена, вы уже вошли в аккаунт.
                </p>
                <div className={styles.doneActions}>
                    <ButtonLink to="/catalog" size="lg" block>
                        Перейти в каталог
                    </ButtonLink>
                    <ButtonLink to="/" variant="text" block>
                        На главную
                    </ButtonLink>
                </div>
            </div>
        );
    }

    if (step === "code") {
        return (
            <div className={styles.card}>
                <Button
                    variant="text"
                    size="sm"
                    className={styles.back}
                    onClick={() => setStep("form")}
                >
                    <Icon src={arrowLeftIcon} size={18} />
                    Изменить данные
                </Button>
                <div className={styles.heading}>
                    <span className={styles.step}>Шаг 2 из 2</span>
                    <h1 className={styles.title}>Подтвердите email</h1>
                    <p className={styles.muted}>
                        Мы отправили 6-значный код на{" "}
                        <span className={styles.email}>{maskEmail(email)}</span>. Введите
                        его ниже, код действует 15 минут.
                    </p>
                </div>
                <div className={styles.codeBlock}>
                    <CodeInput
                        value={code}
                        onChange={onCodeChange}
                        isError={Boolean(codeError)}
                        disabled={verifyMutation.isPending}
                    />
                    {codeError && <Alert>{codeError}</Alert>}
                    {verifyMutation.isPending && (
                        <p className={styles.muted} role="status">
                            Проверяем код…
                        </p>
                    )}
                </div>
                <div className={styles.resend} aria-live="polite">
                    {secondsLeft > 0 ? (
                        <span>
                            Отправить код повторно можно через{" "}
                            {Math.floor(secondsLeft / 60)}:
                            {String(secondsLeft % 60).padStart(2, "0")}
                        </span>
                    ) : (
                        <Button
                            variant="text"
                            disabled={sendCodeMutation.isPending}
                            onClick={() => sendCodeMutation.mutate({ email })}
                        >
                            Отправить код повторно
                        </Button>
                    )}
                </div>
                <Alert variant="info">
                    Письма нет? Проверьте папку «Спам» или измените email.
                </Alert>
            </div>
        );
    }

    return (
        <div className={styles.card}>
            <div className={styles.heading}>
                <span className={styles.step}>Шаг 1 из 2</span>
                <h1 className={styles.title}>Регистрация</h1>
                <p className={styles.muted}>
                    После отправки формы мы пришлём код подтверждения на email.
                </p>
            </div>

            {serverError && <Alert>{serverError}</Alert>}

            <form
                className={styles.form}
                noValidate
                onSubmit={handleSubmit(data => {
                    setServerError(null);
                    registerMutation.mutate({
                        name: data.name,
                        email: data.email,
                        phone: data.phone || undefined,
                        password: data.password,
                        confirmPassword: data.confirmPassword
                    });
                })}
            >
                <Field
                    label="Имя"
                    autoComplete="name"
                    error={errors.name?.message}
                    {...register("name")}
                />
                <Field
                    label="Email"
                    type="email"
                    autoComplete="email"
                    error={errors.email?.message}
                    {...register("email")}
                />
                <Field
                    label="Телефон"
                    type="tel"
                    autoComplete="tel"
                    hint="Необязательно, в формате +7 999 123-45-67"
                    error={errors.phone?.message}
                    {...register("phone")}
                />
                <Field
                    label="Пароль"
                    type="password"
                    autoComplete="new-password"
                    error={errors.password?.message}
                    {...register("password")}
                >
                    <ul className={styles.rules} aria-label="Требования к паролю">
                        {passwordRules.map(rule => {
                            const isMet = rule.test(passwordValue ?? "");
                            return (
                                <li
                                    key={rule.text}
                                    className={clsx(isMet && styles.isMet)}
                                >
                                    <span className={styles.ruleIcon} aria-hidden="true">
                                        <Icon src={checkIcon} size={10} />
                                    </span>
                                    {rule.text}
                                </li>
                            );
                        })}
                    </ul>
                </Field>
                <Field
                    label="Повторите пароль"
                    type="password"
                    autoComplete="new-password"
                    error={errors.confirmPassword?.message}
                    {...register("confirmPassword")}
                />
                <Button
                    type="submit"
                    variant="primary"
                    size="lg"
                    block
                    className={styles.submit}
                    disabled={registerMutation.isPending}
                >
                    {registerMutation.isPending ? "Отправляем…" : "Зарегистрироваться"}
                </Button>
            </form>

            <div className={styles.divider} />
            <p className={styles.switch}>
                Уже есть аккаунт?{" "}
                <Link className={styles.link} to="/login">
                    Войти
                </Link>
            </p>
        </div>
    );
}
