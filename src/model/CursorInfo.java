package model;

import java.awt.Color;

public class CursorInfo {
    public final int userId;
    public final String username;
    public int position;
    public String anchorCharId;
    public final Color color;

    // 4 distinct colors for max 4 concurrent editors
    private static final Color[] COLORS = {
        new Color(255, 99, 99),   // red
        new Color(99, 200, 99),   // green
        new Color(99, 99, 255),   // blue
        new Color(255, 180, 0)    // orange
    };
    
    public CursorInfo(int userId, String username, int position) {
        this(userId, username, position, null);
    }

    public CursorInfo(int userId, String username, int position, String anchorCharId) {
        this.userId = userId;
        this.username = username;
        this.position = position;
        this.anchorCharId = anchorCharId;
        int hash = String.valueOf(userId).hashCode();
        this.color = COLORS[Math.abs(hash) % COLORS.length];
        
    }
}
