// IdGenerator.java
package util;

public class IdGenerator {
    private static int counter = 0;

    public static String generate(int userId) {
        return userId + "-" + counter++;
    }
}