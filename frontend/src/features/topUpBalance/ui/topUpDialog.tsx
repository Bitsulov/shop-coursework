import { useState } from "react";
import { useForm, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { topUpBalance, type UserDetailResponse } from "entities/user";
import { Alert } from "shared/ui/alert";
import { Button } from "shared/ui/button";
import { Chip } from "shared/ui/chip";
import { Dialog } from "shared/ui/dialog";
import { Field } from "shared/ui/field";
import { Icon } from "shared/ui/icon";
import { formatPrice } from "shared/lib/formatPrice";
import checkIcon from "shared/icons/check.svg";
import {
    createTopUpSchema,
    type TopUpFormInput,
    type TopUpFormValues
} from "../model/schema";
import { formatCardNumber, formatExpiry } from "../model/formatCardInput";
import { topUpErrorHandler } from "../model/topUpErrorHandler";
import styles from "./topUpDialog.module.scss";

interface TopUpDialogProps {
    open: boolean;
    onClose: () => void;
    balance: number;
    initialAmount?: number;
}

const presets = [1000, 5000, 10000, 50000];

function TopUpForm({
    balance,
    initialAmount,
    onDone,
    onClose
}: {
    balance: number;
    initialAmount?: number;
    onDone: (user: UserDetailResponse, amount: number) => void;
    onClose: () => void;
}) {
    const queryClient = useQueryClient();
    const [serverError, setServerError] = useState<string | null>(null);

    const {
        register,
        handleSubmit,
        setValue,
        control,
        formState: { errors }
    } = useForm<TopUpFormInput, unknown, TopUpFormValues>({
        resolver: zodResolver(createTopUpSchema()),
        defaultValues: {
            amount: initialAmount ? String(Math.max(100, Math.ceil(initialAmount))) : "",
            cardNumber: "",
            expiry: "",
            cvc: ""
        }
    });
    const amountValue = useWatch({ control, name: "amount" });

    const topUpMutation = useMutation({
        mutationFn: topUpBalance,
        onSuccess: (user, request) => {
            queryClient.setQueryData(["user"], user);
            onDone(user, request.amount);
        },
        onError: error => setServerError(topUpErrorHandler(error))
    });

    return (
        <form
            className={styles.form}
            noValidate
            onSubmit={handleSubmit(values => {
                setServerError(null);
                topUpMutation.mutate(values);
            })}
        >
            <p className={styles.muted}>Сейчас на счёте {formatPrice(balance)}</p>
            <Field
                label="Сумма, ₽"
                inputMode="numeric"
                autoComplete="off"
                hint="От 100 до 500 000 ₽"
                error={errors.amount?.message}
                {...register("amount", {
                    onChange: event =>
                        setValue(
                            "amount",
                            event.target.value.replace(/\D/g, "").slice(0, 6)
                        )
                })}
            />
            <div className={styles.presets} role="group" aria-label="Быстрый выбор суммы">
                {presets.map(preset => (
                    <Chip
                        key={preset}
                        selected={amountValue === String(preset)}
                        onClick={() =>
                            setValue("amount", String(preset), { shouldValidate: true })
                        }
                    >
                        {formatPrice(preset)}
                    </Chip>
                ))}
            </div>
            <fieldset className={styles.card}>
                <legend className={styles.legend}>Банковская карта</legend>
                <Field
                    label="Номер карты"
                    inputMode="numeric"
                    autoComplete="cc-number"
                    error={errors.cardNumber?.message}
                    {...register("cardNumber", {
                        onChange: event =>
                            setValue("cardNumber", formatCardNumber(event.target.value))
                    })}
                />
                <div className={styles.row}>
                    <Field
                        label="ММ/ГГ"
                        inputMode="numeric"
                        autoComplete="cc-exp"
                        error={errors.expiry?.message}
                        {...register("expiry", {
                            onChange: event =>
                                setValue("expiry", formatExpiry(event.target.value))
                        })}
                    />
                    <Field
                        label="CVC"
                        type="password"
                        inputMode="numeric"
                        autoComplete="cc-csc"
                        maxLength={3}
                        error={errors.cvc?.message}
                        {...register("cvc")}
                    />
                </div>
            </fieldset>
            {serverError && <Alert>{serverError}</Alert>}
            <div className={styles.actions}>
                <Button onClick={onClose}>Отмена</Button>
                <Button
                    type="submit"
                    variant="primary"
                    disabled={topUpMutation.isPending}
                >
                    {topUpMutation.isPending ? "Пополняем…" : "Пополнить"}
                </Button>
            </div>
        </form>
    );
}

export function TopUpDialog({ open, onClose, balance, initialAmount }: TopUpDialogProps) {
    const [result, setResult] = useState<{ amount: number; balance: number } | null>(
        null
    );
    const [formKey, setFormKey] = useState(0);

    const close = () => {
        onClose();
        setResult(null);
        setFormKey(key => key + 1);
    };

    return (
        <Dialog
            open={open}
            onClose={close}
            title={result ? "Баланс пополнен" : "Пополнить баланс"}
            wide
        >
            {result ? (
                <div className={styles.done}>
                    <span className={styles.successMark}>
                        <Icon src={checkIcon} size={30} />
                    </span>
                    <p className={styles.muted}>
                        Зачислено {formatPrice(result.amount)}. Теперь на счёте{" "}
                        <span className={styles.strong}>
                            {formatPrice(result.balance)}
                        </span>
                        .
                    </p>
                    <Button variant="primary" block onClick={close}>
                        Готово
                    </Button>
                </div>
            ) : (
                <TopUpForm
                    key={`${formKey}-${initialAmount ?? ""}`}
                    balance={balance}
                    initialAmount={initialAmount}
                    onClose={close}
                    onDone={(user, amount) =>
                        setResult({ amount, balance: user.balance })
                    }
                />
            )}
        </Dialog>
    );
}
