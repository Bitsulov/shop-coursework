const allowedTypes = ["image/jpeg", "image/png", "image/webp"];
const maxSize = 5 * 1024 * 1024;

export function validateAvatar(file: File): string | null {
    if (!allowedTypes.includes(file.type)) {
        return "Подойдёт фото в формате JPG, PNG или WebP";
    }
    if (file.size > maxSize) {
        return "Фото должно быть не больше 5 МБ";
    }
    return null;
}
