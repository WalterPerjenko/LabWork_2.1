package validators;
import domain.*;

public final class ContainerValidator {
        private ContainerValidator() {}

        public static void validateName(String name) {
            if (name == null || name.trim().isEmpty())
                throw new IllegalArgumentException("Ошибка: название контейнера не может быть пустым");
            if (name.length() > 64)
                throw new IllegalArgumentException("Ошибка: название слишком длинное (макс. 64 символа)");
        }

        public static void validateType(ContainerType type) {
            if (type == null)
                throw new IllegalArgumentException("Ошибка: тип контейнера не может быть null");
        }

        public static void validateUpdate(String newName, ContainerType newType) {
            if (newName != null) validateName(newName);
            if (newType != null) validateType(newType);
        }
    }