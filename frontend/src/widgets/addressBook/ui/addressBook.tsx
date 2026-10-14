import { useState } from "react";
import { useDispatch } from "react-redux";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import clsx from "clsx";
import {
    deleteAddress,
    formatAddressCity,
    formatAddressLine,
    getAddresses,
    selectAddress,
    type AddressResponse
} from "entities/address";
import { AddressForm } from "features/addressForm";
import { showToast } from "features/toast";
import { Badge } from "shared/ui/badge";
import { Button } from "shared/ui/button";
import { Dialog } from "shared/ui/dialog";
import { Drawer } from "shared/ui/drawer";
import { Icon } from "shared/ui/icon";
import { IconButton } from "shared/ui/iconButton";
import { Skeleton } from "shared/ui/skeleton";
import editIcon from "shared/icons/edit.svg";
import mapPinIcon from "shared/icons/mapPin.svg";
import messageIcon from "shared/icons/message.svg";
import plusIcon from "shared/icons/plus.svg";
import trashIcon from "shared/icons/trash.svg";
import styles from "./addressBook.module.scss";

type FormState =
    { mode: "closed" } | { mode: "new" } | { mode: "edit"; address: AddressResponse };

export function AddressBook() {
    const dispatch = useDispatch();
    const queryClient = useQueryClient();
    const [form, setForm] = useState<FormState>({ mode: "closed" });
    const [toDelete, setToDelete] = useState<AddressResponse | null>(null);

    const { data: addresses, isPending } = useQuery({
        queryKey: ["addresses"],
        queryFn: getAddresses
    });

    const selectMutation = useMutation({
        mutationFn: selectAddress,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["addresses"] });
            dispatch(showToast({ title: "Основной адрес изменён", type: "success" }));
        },
        onError: () =>
            dispatch(showToast({ title: "Не удалось изменить адрес", type: "error" }))
    });

    const deleteMutation = useMutation({
        mutationFn: deleteAddress,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["addresses"] });
            setToDelete(null);
            dispatch(showToast({ title: "Адрес удалён", type: "success" }));
        },
        onError: () =>
            dispatch(showToast({ title: "Не удалось удалить адрес", type: "error" }))
    });

    const closeForm = () => setForm({ mode: "closed" });
    const hasItems = (addresses?.length ?? 0) > 0;

    return (
        <section aria-labelledby="addresses-title">
            <div className={styles.head}>
                <div className={styles.headText}>
                    <h2 className={styles.title} id="addresses-title">
                        Адреса доставки
                    </h2>
                    {hasItems && (
                        <span className={styles.muted}>
                            Основной адрес подставляется при оформлении заказа
                        </span>
                    )}
                </div>
                {hasItems && (
                    <Button size="sm" onClick={() => setForm({ mode: "new" })}>
                        <Icon src={plusIcon} size={18} />
                        Добавить адрес
                    </Button>
                )}
            </div>

            {isPending && <Skeleton style={{ height: 160, borderRadius: 8 }} />}

            {!isPending && !hasItems && (
                <div className={styles.empty}>
                    <span className={styles.emptyIcon}>
                        <Icon src={mapPinIcon} size={28} />
                    </span>
                    <h3 className={styles.emptyTitle}>Адресов пока нет</h3>
                    <p>Добавьте адрес, и мы подставим его при оформлении заказа.</p>
                    <Button onClick={() => setForm({ mode: "new" })}>
                        <Icon src={plusIcon} size={18} />
                        Добавить адрес
                    </Button>
                </div>
            )}

            {hasItems && (
                <ul className={styles.list}>
                    {addresses?.map(address => (
                        <li
                            key={address.uuid}
                            className={clsx(
                                styles.address,
                                address.selected && styles.isSelected
                            )}
                        >
                            <span className={styles.icon} aria-hidden="true">
                                <Icon src={mapPinIcon} size={20} />
                            </span>
                            <div className={styles.body}>
                                <div className={styles.top}>
                                    <span className={styles.line1}>
                                        {formatAddressLine(address)}
                                    </span>
                                    {address.selected && (
                                        <Badge variant="dark">Основной</Badge>
                                    )}
                                </div>
                                <span className={styles.line2}>
                                    {formatAddressCity(address)}
                                </span>
                                {address.comment && (
                                    <span className={styles.comment}>
                                        <Icon src={messageIcon} size={16} />
                                        {address.comment}
                                    </span>
                                )}
                            </div>
                            <div className={styles.actions}>
                                {!address.selected && (
                                    <Button
                                        variant="text"
                                        size="sm"
                                        disabled={selectMutation.isPending}
                                        onClick={() =>
                                            selectMutation.mutate(address.uuid)
                                        }
                                    >
                                        Сделать основным
                                    </Button>
                                )}
                                <IconButton
                                    size="sm"
                                    icon={editIcon}
                                    iconSize={18}
                                    label={`Изменить адрес: ${formatAddressLine(address)}`}
                                    onClick={() => setForm({ mode: "edit", address })}
                                />
                                <IconButton
                                    size="sm"
                                    icon={trashIcon}
                                    iconSize={18}
                                    label={`Удалить адрес: ${formatAddressLine(address)}`}
                                    onClick={() => setToDelete(address)}
                                />
                            </div>
                        </li>
                    ))}
                </ul>
            )}

            <Drawer
                open={form.mode !== "closed"}
                onClose={closeForm}
                label="Адрес"
                className={styles.drawer}
                title={
                    <h2 className={styles.drawerTitle}>
                        {form.mode === "edit" ? "Изменить адрес" : "Новый адрес"}
                    </h2>
                }
            >
                <div className={styles.drawerBody}>
                    {form.mode !== "closed" && (
                        <AddressForm
                            key={form.mode === "edit" ? form.address.uuid : "new"}
                            address={form.mode === "edit" ? form.address : undefined}
                            onCancel={closeForm}
                            onSaved={() => {
                                closeForm();
                                dispatch(
                                    showToast({
                                        title: "Адрес сохранён",
                                        type: "success"
                                    })
                                );
                            }}
                        />
                    )}
                </div>
            </Drawer>

            <Dialog
                open={toDelete !== null}
                onClose={() => setToDelete(null)}
                title="Удалить адрес?"
            >
                <p className={styles.muted}>
                    {toDelete && formatAddressLine(toDelete)} больше не будет в списке.
                    {toDelete?.selected &&
                        " Это основной адрес, основным станет другой сохранённый адрес."}
                </p>
                <div className={styles.dialogActions}>
                    <Button onClick={() => setToDelete(null)}>Отмена</Button>
                    <Button
                        variant="danger"
                        disabled={deleteMutation.isPending}
                        onClick={() => toDelete && deleteMutation.mutate(toDelete.uuid)}
                    >
                        Удалить
                    </Button>
                </div>
            </Dialog>
        </section>
    );
}
