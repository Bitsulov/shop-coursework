import type { ReactNode } from "react";
import { Header } from "widgets/header";
import { Footer } from "widgets/footer";

export function Layout({ children }: { children: ReactNode }) {
    return (
        <>
            <Header />
            <main>{children}</main>
            <Footer />
        </>
    );
}
