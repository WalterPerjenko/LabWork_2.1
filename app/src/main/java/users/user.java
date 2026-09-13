package users;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

public class user {
    private final String login;
    private final String passwordHash;
    private final Instant createdAt;

    public user(String login, String password) {
        this.login = login;
        this.passwordHash = hashPassword(password);
        this.createdAt = Instant.now();
    }

    // Конструктор для загрузки из файла
    public user(String login, String passwordHash, Instant createdAt) {
        this.login = login;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public String getLogin() { return login; }
    public String getPasswordHash() { return passwordHash; }
    public Instant getCreatedAt() { return createdAt; }

    public boolean checkPassword(String password) {
        return this.passwordHash.equals(hashPassword(password));
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка хеширования пароля", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof user)) return false;
        user u = (user) o;
        return login.equals(u.login);
    }

    @Override
    public int hashCode() {
        return login.hashCode();
    }
}
