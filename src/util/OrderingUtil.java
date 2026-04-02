// OrderingUtil.java
package util;

import java.util.List;

public class OrderingUtil {

    public static int compare(int clockA, int userIdA, int clockB, int userIdB) {
        if (clockA != clockB)
            return Integer.compare(clockA, clockB); // lower clock first
        return Integer.compare(userIdB, userIdA); // higher userId wins tiebreak
    }
}