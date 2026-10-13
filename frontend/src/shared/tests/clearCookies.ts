export function clearCookies() {
    document.cookie.split(";").forEach(cookie => {
        const name = cookie.split("=")[0].trim();
        if (name) {
            document.cookie = `${name}=; max-age=0; path=/`;
        }
    });
}
