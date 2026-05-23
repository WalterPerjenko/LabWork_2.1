package validators;

public final class SlotValidator {
    private SlotValidator() {}

    public static void validateDimensions(int rows, int cols) {
        if (rows <= 0 || cols <= 0) throw new IllegalArgumentException("Ошибка: количество рядов/колонок должно быть > 0");
    }
}
