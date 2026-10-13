import { DevelopmentNotice } from "features/developmentNotice";
import styles from "./home.module.scss";

export function Home() {
    return (
        <div className={`container ${styles.home}`}>
            <h1>Shop</h1>
            <DevelopmentNotice />
        </div>
    );
}
