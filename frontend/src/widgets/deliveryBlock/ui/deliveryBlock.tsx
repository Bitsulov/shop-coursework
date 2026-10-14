import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useDispatch } from "react-redux";
import {
    formatAddressCity,
    formatAddressLine,
    getAddresses,
    selectAddress
} from "entities/address";
import { AddressForm } from "features/addressForm";
import { showToast } from "features/toast";
import { Badge } from "shared/ui/badge";
import { Button } from "shared/ui/button";
import { Dialog } from "shared/ui/dialog";
import { Icon } from "shared/ui/icon";
import { RadioCard } from "shared/ui/radioCard";
import { Skeleton } from "shared/ui/skeleton";
import mapPinIcon from "shared/icons/mapPin.svg";
import messageIcon from "shared/icons/message.svg";
import plusIcon from "shared/icons/plus.svg";
import styles from "./deliveryBlock.module.scss";

interface DeliveryBlockProps {
    note?: string;
}

export function DeliveryBlock({ note }: DeliveryBlockProps) {
    const dispatch = useDispatch();
    const queryClient = useQueryClient();
    const [isPickerOpen, setIsPickerOpen] = useState(false);
    const [isAddOpen, setIsAddOpen] = useState(false);
    const [pickedUuid, setPickedUuid] = useState<string | null>(null);

    const { data: addresses, isPending } = useQuery({
        queryKey: ["addresses"],
        queryFn: getAddresses
    });
    const current = addresses?.find(address => address.selected) ?? addresses?.[0];

    const selectMutation = useMutation({
        mutationFn: selectAddress,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["addresses"] });
            setIsPickerOpen(false);
        },
        onError: () =>
            dispatch(showToast({ title: "Не удалось выбрать адрес", type: "error" }))
    });

    const openPicker = () => {
        setPickedUuid(current?.uuid ?? null);
        setIsAddOpen(false);
        setIsPickerOpen(true);
    };

    return (
        <section className={styles.delivery} aria-labelledby="delivery-title">
            <div className={styles.head}>
                <h2 className={styles.title} id="delivery-title">
                    Доставка
                </h2>
                {current && (
                    <button type="button" className={styles.change} onClick={openPicker}>
                        Изменить
                    </button>
                )}
            </div>

            {isPending && <Skeleton style={{ height: 64 }} />}

            {!isPending && current && (
                <div className={styles.address}>
                    <span className={styles.addressIcon} aria-hidden="true">
                        <Icon src={mapPinIcon} size={20} />
                    </span>
                    <div className={styles.addressBody}>
                        <span className={styles.line1}>{formatAddressLine(current)}</span>
                        <span className={styles.line2}>{formatAddressCity(current)}</span>
                        {current.comment && (
                            <span className={styles.comment}>
                                <Icon src={messageIcon} size={16} />
                                {current.comment}
                            </span>
                        )}
                        {note && <span className={styles.note}>{note}</span>}
                    </div>
                </div>
            )}

            {!isPending && !current && (
                <>
                    <p className={styles.muted}>
                        Куда доставить заказ? Адрес сохранится в профиле, в следующий раз
                        вводить его не придётся.
                    </p>
                    <AddressForm onSaved={() => undefined} />
                </>
            )}

            <Dialog
                open={isPickerOpen}
                onClose={() => setIsPickerOpen(false)}
                title="Адрес доставки"
                wide
            >
                <div
                    className={styles.picker}
                    role="radiogroup"
                    aria-label="Сохранённые адреса"
                >
                    {addresses?.map(address => (
                        <RadioCard
                            key={address.uuid}
                            name="delivery-address"
                            checked={pickedUuid === address.uuid}
                            onChange={() => setPickedUuid(address.uuid)}
                        >
                            <span className={styles.optionTop}>
                                <span className={styles.line1}>
                                    {formatAddressLine(address)}
                                </span>
                                {address.selected && <Badge>Основной</Badge>}
                            </span>
                            <span className={styles.line2}>
                                {formatAddressCity(address)}
                            </span>
                        </RadioCard>
                    ))}
                    {isAddOpen ? (
                        <div className={styles.addBox}>
                            <h3 className={styles.addTitle}>Новый адрес</h3>
                            <AddressForm
                                submitLabel="Добавить"
                                onCancel={() => setIsAddOpen(false)}
                                onSaved={address => {
                                    setPickedUuid(address.uuid);
                                    setIsAddOpen(false);
                                }}
                            />
                        </div>
                    ) : (
                        <button
                            type="button"
                            className={styles.addButton}
                            onClick={() => setIsAddOpen(true)}
                        >
                            <Icon src={plusIcon} size={18} />
                            Добавить новый адрес
                        </button>
                    )}
                </div>
                <div className={styles.actions}>
                    <Button onClick={() => setIsPickerOpen(false)}>Отмена</Button>
                    <Button
                        variant="primary"
                        disabled={isAddOpen || !pickedUuid || selectMutation.isPending}
                        onClick={() => {
                            if (pickedUuid && pickedUuid !== current?.uuid) {
                                selectMutation.mutate(pickedUuid);
                            } else {
                                setIsPickerOpen(false);
                            }
                        }}
                    >
                        Доставить сюда
                    </Button>
                </div>
            </Dialog>
        </section>
    );
}
