import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { getPrincipalUser } from "entities/user";
import { AvatarEditor } from "features/changeAvatar";
import { DeleteAccountButton } from "features/deleteAccount";
import { ProfileForm } from "features/editProfile";
import { LogoutButton } from "features/logout";
import { TopUpDialog } from "features/topUpBalance";
import { AddressBook } from "widgets/addressBook";
import { Breadcrumbs } from "shared/ui/breadcrumbs";
import { Button } from "shared/ui/button";
import { Icon } from "shared/ui/icon";
import { Skeleton } from "shared/ui/skeleton";
import { formatPrice } from "shared/lib/formatPrice";
import editIcon from "shared/icons/edit.svg";
import plusIcon from "shared/icons/plus.svg";
import styles from "./profile.module.scss";

export function Profile() {
    const [isEditing, setIsEditing] = useState(false);
    const [isTopUpOpen, setIsTopUpOpen] = useState(false);
    const { data: user } = useQuery({
        queryKey: ["user"],
        queryFn: () => getPrincipalUser()
    });

    return (
        <div className="container">
            <Breadcrumbs
                items={[{ label: "Главная", to: "/" }, { label: "Личный кабинет" }]}
            />
            <h1 className={styles.title}>Личный кабинет</h1>

            {!user ? (
                <div className={styles.grid}>
                    <Skeleton style={{ height: 420, borderRadius: 12 }} />
                    <Skeleton style={{ height: 200, borderRadius: 12 }} />
                </div>
            ) : (
                <div className={styles.grid}>
                    <div className={styles.main}>
                        <section className={styles.panel} aria-label="Фото профиля">
                            <AvatarEditor user={user} />
                        </section>

                        <section
                            className={styles.panel}
                            aria-labelledby="personal-title"
                        >
                            <div className={styles.panelHead}>
                                <h2 className={styles.panelTitle} id="personal-title">
                                    Личные данные
                                </h2>
                                {!isEditing && (
                                    <Button size="sm" onClick={() => setIsEditing(true)}>
                                        <Icon src={editIcon} size={18} />
                                        Редактировать
                                    </Button>
                                )}
                            </div>
                            {isEditing ? (
                                <ProfileForm
                                    user={user}
                                    onDone={() => setIsEditing(false)}
                                />
                            ) : (
                                <dl className={styles.details}>
                                    <dt>Имя</dt>
                                    <dd>{user.name}</dd>
                                    <dt>Email</dt>
                                    <dd>{user.email}</dd>
                                    <dt>Телефон</dt>
                                    <dd>{user.phone ?? "Не указан"}</dd>
                                </dl>
                            )}
                        </section>

                        <div className={styles.panel}>
                            <AddressBook />
                        </div>
                    </div>

                    <aside className={styles.side}>
                        <section className={styles.panel} aria-labelledby="balance-title">
                            <h2 className={styles.balanceLabel} id="balance-title">
                                Баланс счёта
                            </h2>
                            <span className={styles.balance} aria-live="polite">
                                {formatPrice(user.balance)}
                            </span>
                            <Button block onClick={() => setIsTopUpOpen(true)}>
                                <Icon src={plusIcon} size={18} />
                                Пополнить
                            </Button>
                        </section>
                        <section className={styles.panel} aria-labelledby="account-title">
                            <h2 className={styles.panelTitle} id="account-title">
                                Аккаунт
                            </h2>
                            <div className={styles.account}>
                                <LogoutButton />
                                <DeleteAccountButton balance={user.balance} />
                            </div>
                        </section>
                    </aside>

                    <TopUpDialog
                        open={isTopUpOpen}
                        onClose={() => setIsTopUpOpen(false)}
                        balance={user.balance}
                    />
                </div>
            )}
        </div>
    );
}
