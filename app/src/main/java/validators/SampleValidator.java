package validators;

public class SampleValidator {
    private SampleValidator () {}
    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ошибка: название образца не может быть пустым");
        }
        if (name.length() > 128) {
            throw new IllegalArgumentException("Ошибка: название слишком длинное (макс. 128 символов)");
        }
    }
}

