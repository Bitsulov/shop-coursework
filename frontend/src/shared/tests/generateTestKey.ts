export async function generateTestKey(): Promise<string> {
    const key = await crypto.subtle.generateKey({ name: "AES-GCM", length: 256 }, true, [
        "encrypt",
        "decrypt"
    ]);
    const raw = new Uint8Array(await crypto.subtle.exportKey("raw", key));

    return btoa(String.fromCharCode(...raw));
}
