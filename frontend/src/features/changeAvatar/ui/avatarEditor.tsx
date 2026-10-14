import { useState, type ChangeEvent } from "react";
import { useDispatch } from "react-redux";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { updateUser, type UserDetailResponse } from "entities/user";
import { showToast } from "features/toast";
import { Avatar } from "shared/ui/avatar";
import { Button } from "shared/ui/button";
import { Dialog } from "shared/ui/dialog";
import { Icon } from "shared/ui/icon";
import trashIcon from "shared/icons/trash.svg";
import uploadIcon from "shared/icons/upload.svg";
import { validateAvatar } from "../model/validateAvatar";
import styles from "./avatarEditor.module.scss";

interface AvatarEditorProps {
    user: UserDetailResponse;
}

export function AvatarEditor({ user }: AvatarEditorProps) {
    const dispatch = useDispatch();
    const queryClient = useQueryClient();
    const [fileError, setFileError] = useState<string | null>(null);
    const [isDeleteOpen, setIsDeleteOpen] = useState(false);

    const avatarMutation = useMutation({
        mutationFn: updateUser,
        onSuccess: (updated, request) => {
            queryClient.setQueryData(["user"], updated);
            setIsDeleteOpen(false);
            dispatch(
                showToast({
                    title: request.details.deleteAvatar
                        ? "Фото удалено"
                        : "Фото обновлено",
                    type: "success"
                })
            );
        },
        onError: () =>
            setFileError("Не удалось сохранить фото. Попробуйте другое изображение")
    });

    const details = {
        name: user.name,
        phone: user.phone ?? undefined
    };

    const onFile = (event: ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        event.target.value = "";
        if (!file) return;

        const error = validateAvatar(file);
        setFileError(error);
        if (!error) {
            avatarMutation.mutate({
                details: { ...details, deleteAvatar: false },
                avatar: file
            });
        }
    };

    return (
        <div className={styles.editor}>
            <Avatar name={user.name} src={user.avatarUrl} size={96} />
            <div className={styles.side}>
                <div>
                    <div className={styles.name}>{user.name}</div>
                    <div className={styles.muted}>{user.email}</div>
                </div>
                <div className={styles.buttons}>
                    <label className={styles.upload}>
                        <Icon src={uploadIcon} size={18} />
                        {avatarMutation.isPending
                            ? "Загружаем…"
                            : user.avatarUrl
                              ? "Заменить фото"
                              : "Загрузить фото"}
                        <input
                            type="file"
                            accept="image/png, image/jpeg, image/webp"
                            disabled={avatarMutation.isPending}
                            onChange={onFile}
                        />
                    </label>
                    {user.avatarUrl && (
                        <Button
                            variant="text"
                            size="sm"
                            className={styles.danger}
                            onClick={() => setIsDeleteOpen(true)}
                        >
                            <Icon src={trashIcon} size={18} />
                            Удалить фото
                        </Button>
                    )}
                </div>
                <span className={styles.hint}>JPG, PNG или WebP, до 5 МБ</span>
                {fileError && (
                    <span className={styles.error} role="alert">
                        {fileError}
                    </span>
                )}
            </div>

            <Dialog
                open={isDeleteOpen}
                onClose={() => setIsDeleteOpen(false)}
                title="Удалить фото профиля?"
            >
                <p className={styles.muted}>
                    Вместо фото будут показаны инициалы. Остальные данные профиля не
                    изменятся.
                </p>
                <div className={styles.actions}>
                    <Button onClick={() => setIsDeleteOpen(false)}>Отмена</Button>
                    <Button
                        variant="danger"
                        disabled={avatarMutation.isPending}
                        onClick={() =>
                            avatarMutation.mutate({
                                details: { ...details, deleteAvatar: true }
                            })
                        }
                    >
                        Удалить фото
                    </Button>
                </div>
            </Dialog>
        </div>
    );
}
