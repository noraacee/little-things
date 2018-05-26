package badtzmarupekkle.littlethings.util;

public class Validation {

    private static final String SECRET = "PektzMaru";

    public static boolean isValidString(String string) {
        if (string == null || string.length() == 0)
            return false;
        return true;
    }

    public static boolean validateUser(String secret) {
        if (!isValidString(secret) || !secret.equals(SECRET))
            return false;
        return true;
    }
}
