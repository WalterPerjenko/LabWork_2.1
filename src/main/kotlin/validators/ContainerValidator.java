package validators;
import Domain.*;

public final class ContainerValidator {
    private ContainerValidator() {}

    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Ошибка: название не может быть пустым");
        if (name.length() > 64) throw new IllegalArgumentException("Ошибка: название слишком длинное (макс. 64)");
    }

    public static void validateType(String typeStr) {
        try {
            ContainerType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Ошибка: тип должен быть из списка: FREEZER, FRIDGE, BOX");
        }
    }
}