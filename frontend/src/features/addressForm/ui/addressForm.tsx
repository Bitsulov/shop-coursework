import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import axios from "axios";
import {
    createAddress,
    updateAddress,
    type AddressRequest,
    type AddressResponse
} from "entities/address";
import { Alert } from "shared/ui/alert";
import { Button } from "shared/ui/button";
import { Field } from "shared/ui/field";
import { Textarea } from "shared/ui/textarea";
import { addressFormSchema, type AddressFormValues } from "../model/schema";
import styles from "./addressForm.module.scss";

interface AddressFormProps {
    address?: AddressResponse;
    submitLabel?: string;
    onSaved: (address: AddressResponse) => void;
    onCancel?: () => void;
}

function toRequest(values: AddressFormValues): AddressRequest {
    return {
        city: values.city,
        street: values.street,
        house: values.house,
        apartment: values.apartment || undefined,
        postalCode: values.postalCode || undefined,
        comment: values.comment || undefined
    };
}

export function AddressForm({
    address,
    submitLabel = "Сохранить адрес",
    onSaved,
    onCancel
}: AddressFormProps) {
    const queryClient = useQueryClient();
    const [serverError, setServerError] = useState<string | null>(null);

    const {
        register,
        handleSubmit,
        formState: { errors }
    } = useForm<AddressFormValues>({
        resolver: zodResolver(addressFormSchema),
        defaultValues: {
            city: address?.city ?? "",
            street: address?.street ?? "",
            house: address?.house ?? "",
            apartment: address?.apartment ?? "",
            postalCode: address?.postalCode ?? "",
            comment: address?.comment ?? ""
        }
    });

    const saveMutation = useMutation({
        mutationFn: (values: AddressFormValues) =>
            address
                ? updateAddress({ uuid: address.uuid, data: toRequest(values) })
                : createAddress(toRequest(values)),
        onSuccess: async saved => {
            await queryClient.invalidateQueries({ queryKey: ["addresses"] });
            onSaved(saved);
        },
        onError: error => {
            const isLimit = axios.isAxiosError(error) && error.response?.status === 409;
            setServerError(
                isLimit
                    ? "Достигнут предел числа адресов. Удалите ненужный адрес."
                    : "Не удалось сохранить адрес. Попробуйте ещё раз."
            );
        }
    });

    return (
        <form
            className={styles.form}
            noValidate
            onSubmit={handleSubmit(values => {
                setServerError(null);
                saveMutation.mutate(values);
            })}
        >
            <Field
                label="Город"
                autoComplete="address-level2"
                error={errors.city?.message}
                {...register("city")}
            />
            <Field
                label="Улица"
                autoComplete="address-line1"
                error={errors.street?.message}
                {...register("street")}
            />
            <div className={styles.row}>
                <Field label="Дом" error={errors.house?.message} {...register("house")} />
                <Field
                    label="Квартира или офис"
                    error={errors.apartment?.message}
                    {...register("apartment")}
                />
            </div>
            <Field
                label="Индекс"
                inputMode="numeric"
                autoComplete="postal-code"
                hint="Необязательно"
                error={errors.postalCode?.message}
                {...register("postalCode")}
            />
            <Textarea
                label="Комментарий для курьера"
                hint="Необязательно. Например: «Домофон не работает, позвонить за 10 минут»"
                error={errors.comment?.message}
                {...register("comment")}
            />
            {serverError && <Alert>{serverError}</Alert>}
            <div className={styles.actions}>
                {onCancel && <Button onClick={onCancel}>Отмена</Button>}
                <Button
                    type="submit"
                    variant="primary"
                    className={styles.submit}
                    disabled={saveMutation.isPending}
                >
                    {saveMutation.isPending ? "Сохраняем…" : submitLabel}
                </Button>
            </div>
        </form>
    );
}
