import type { ReactNode } from "react";
import { Header } from "widgets/header";
import { Footer } from "widgets/footer";
import styles from "./layout.module.scss";

export function Layout({ children }: { children: ReactNode }) {
    return (
        <>
            <Header />
            <main id="main" className={styles.main}>
                {children}
            </main>
            <Footer />
        </>
    );
}
