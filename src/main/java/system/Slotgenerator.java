package system;

public  final class Slotgenerator {
    private Slotgenerator (){}
    public static String rowToCode (int rowNumber){
        if (rowNumber<=0){
            throw new IllegalArgumentException("");
        }
        StringBuilder sb = new StringBuilder();
        int n = rowNumber;
        while (n>0){
            n--;
            char c = (char) ('A' + n % 26);
            sb.insert(0, c);
            n /= 26;
        }
        return sb.toString();
    }
    public static String extractRowPart(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Ошибка: код слота не может быть пустым");
        }

        int i = 0;
        while (i < code.length() && Character.isLetter(code.charAt(i))) {
            i++;
        }

        if (i == 0) {
            throw new IllegalArgumentException("Ошибка: код должен начинаться с буквы");
        }

        return code.substring(0, i).toUpperCase();
    }
    public static int extractColNumber(String code) {
        String rowPart = extractRowPart(code);
        String colPart = code.substring(rowPart.length());

        if (colPart.isEmpty()) {
            throw new IllegalArgumentException("Ошибка: код должен содержать номер колонки");
        }

        try {
            return Integer.parseInt(colPart);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ошибка: номер колонки должен быть числом");
        }
    }
    public static void validateCodeFormat(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Ошибка: код слота не может быть пустым");
        }

        if (!code.matches("^[A-Z]+[1-9][0-9]*$")) {
            throw new IllegalArgumentException("Ошибка: неверный формат кода (должен быть как A1, AB12, ZZ99)");
        }

        if (code.length() > 10) {
            throw new IllegalArgumentException("Ошибка: код слишком длинный");
        }
    }

}
