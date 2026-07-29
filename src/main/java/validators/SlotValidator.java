package validators;

public final class SlotValidator {
    private SlotValidator() {}

        public static void validateCode(String code) {
            if (code == null || code.trim().isEmpty())
                throw new IllegalArgumentException("Ошибка: код ячейки не может быть пустым");
            if (code.length() > 8)
                throw new IllegalArgumentException("Ошибка: код ячейки слишком длинный (макс. 8 символов)");
        }

        public static void validateDimensions(int rows, int cols) {
            if (rows <= 0 || cols <= 0)
                throw new IllegalArgumentException("Ошибка: количество рядов и колонок должно быть > 0");
        }
    }


