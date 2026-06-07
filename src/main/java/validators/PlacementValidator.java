package validators;

public class PlacementValidator {

        private PlacementValidator() {}

        public static void validateSampleId(long sampleId) {
            if (sampleId <= 0)
                throw new IllegalArgumentException("Ошибка: sample_id должен быть положительным числом");
        }
    }

