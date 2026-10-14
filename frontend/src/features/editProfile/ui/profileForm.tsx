import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useDispatch } from "react-redux";
import { updateUser, type UserDetailResponse } from "entities/user";
import { showToast } from "features/toast";
import { Button } from "shared/ui/button";
import { Field } from "shared/ui/field";
import {
    profileFormSchema,
    type ProfileFormInput,
    type ProfileFormValues
} from "../model/schema";
import styles from "./profileForm.module.scss";

interface ProfileFormProps {
    user: UserDetailResponse;
    onDone: () => void;
}

export function ProfileForm({ user, onDone }: ProfileFormProps) {
    const dispatch = useDispatch();
    const queryClient = useQueryClient();

    const {
        register,
        handleSubmit,
        formState: { errors }
    } = useForm<ProfileFormInput, unknown, ProfileFormValues>({
        resolver: zodResolver(profileFormSchema),
        defaultValues: { name: user.name, phone: user.phone ?? "" }
    });

    const saveMutation = useMutation({
        mutationFn: updateUser,
        onSuccess: updated => {
            queryClient.setQueryData(["user"], updated);
            dispatch(showToast({ title: "Данные сохранены", type: "success" }));
            onDone();
        },
        onError: () =>
            dispatch(showToast({ title: "Не удалось сохранить данные", type: "error" }))
    });

    return (
        <form
            className={styles.form}
            noValidate
            onSubmit={handleSubmit(values =>
                saveMutation.mutate({
                    details: {
                        name: values.name,
                        phone: values.phone || undefined,
                        deleteAvatar: false
                    }
                })
            )}
        >
            <div className={styles.grid}>
                <Field
                    label="Имя"
                    autoComplete="name"
                    error={errors.name?.message}
                    {...register("name")}
                />
                <Field
                    label="Телефон"
                    type="tel"
                    autoComplete="tel"
                    hint="Необязательно, в формате +7 999 123-45-67"
                    error={errors.phone?.message}
                    {...register("phone")}
                />
            </div>
            <div className={styles.actions}>
                <Button type="submit" variant="primary" disabled={saveMutation.isPending}>
                    {saveMutation.isPending ? "Сохраняем…" : "Сохранить"}
                </Button>
                <Button variant="text" onClick={onDone}>
                    Отмена
                </Button>
            </div>
        </form>
    );
}
