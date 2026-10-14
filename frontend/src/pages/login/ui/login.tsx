import { AuthForm } from "widgets/authForm";
import styles from "./login.module.scss";

export function Login() {
    return (
        <div className="container">
            <div className={styles.page}>
                <AuthForm />
            </div>
        </div>
    );
}
