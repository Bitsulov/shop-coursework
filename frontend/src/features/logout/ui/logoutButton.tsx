import { useNavigate } from "react-router";
import { useDispatch } from "react-redux";
import { useQueryClient } from "@tanstack/react-query";
import { resetUser } from "entities/user";
import { showToast } from "features/toast";
import { Button } from "shared/ui/button";
import { Icon } from "shared/ui/icon";
import { clearCookiesTokens } from "shared/lib/clearCookiesTokens";
import logoutIcon from "shared/icons/logout.svg";

export function LogoutButton() {
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const queryClient = useQueryClient();

    const logout = () => {
        clearCookiesTokens();
        dispatch(resetUser());
        queryClient.removeQueries();
        dispatch(showToast({ title: "Вы вышли из аккаунта", type: "success" }));
        navigate("/");
    };

    return (
        <Button onClick={logout}>
            <Icon src={logoutIcon} size={18} />
            Выйти
        </Button>
    );
}
