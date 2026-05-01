// OrderingUtil.java
package util;

public class OrderingUtil {

    public static int compare(int clockA, int userIdA, int clockB, int userIdB) {
        if (clockA != clockB)
            return Integer.compare(clockB, clockA); // higher clock first
        return Integer.compare(userIdA, userIdB); // lower userId first
    }
}
