async function importKeyFromBase64(keyBase64: string): Promise<CryptoKey> {
    const keyBuffer = Uint8Array.from(atob(keyBase64), c => c.charCodeAt(0));
    return crypto.subtle.importKey("raw", keyBuffer, { name: "AES-GCM" }, false, [
        "encrypt",
        "decrypt"
    ]);
}

export async function encryptToString(text: string, keyBase64: string): Promise<string> {
    const key = await importKeyFromBase64(keyBase64);
    const iv = crypto.getRandomValues(new Uint8Array(12));
    const encrypted = new Uint8Array(
        await crypto.subtle.encrypt(
            { name: "AES-GCM", iv },
            key,
            new TextEncoder().encode(text)
        )
    );

    const combined = new Uint8Array(iv.length + encrypted.length);
    combined.set(iv);
    combined.set(encrypted, iv.length);

    const base64 = btoa(String.fromCharCode(...combined));
    return base64.replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

export async function decryptFromString(str: string, keyBase64: string): Promise<string> {
    const key = await importKeyFromBase64(keyBase64);

    let base64 = str.replace(/-/g, "+").replace(/_/g, "/");
    while (base64.length % 4) {
        base64 += "=";
    }

    const combined = Uint8Array.from(atob(base64), c => c.charCodeAt(0));
    const decrypted = await crypto.subtle.decrypt(
        { name: "AES-GCM", iv: combined.slice(0, 12) },
        key,
        combined.slice(12)
    );

    return new TextDecoder().decode(decrypted);
}
