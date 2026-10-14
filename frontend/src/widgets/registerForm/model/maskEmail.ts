export function maskEmail(email: string): string {
    const [name, domain] = email.split("@");
    if (!domain) return email;

    const visible = name.slice(0, Math.min(2, name.length));
    return `${visible}${"•".repeat(Math.max(name.length - visible.length, 1))}@${domain}`;
}
