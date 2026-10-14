import { RegisterForm } from "widgets/registerForm";
import styles from "./register.module.scss";

export function Register() {
    return (
        <div className="container">
            <div className={styles.page}>
                <RegisterForm />
            </div>
        </div>
    );
}
