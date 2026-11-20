package com.netmonitor.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Modern Dark Palette and Typography styling for Java Swing desktop interface.
 */
public class UITheme {

    public static final Color BG_DARK = new Color(15, 23, 42);          // Slate 900
    public static final Color BG_CARD = new Color(30, 41, 59);          // Slate 800
    public static final Color BG_CARD_LIGHT = new Color(51, 65, 85);    // Slate 700
    public static final Color BG_INPUT = new Color(30, 41, 59);         // Input background
    public static final Color TEXT_PRIMARY = new Color(248, 250, 252);  // Slate 50
    public static final Color TEXT_SECONDARY = new Color(203, 213, 225);// Slate 300
    public static final Color TEXT_MUTED = new Color(148, 163, 184);    // Slate 400

    public static final Color ACCENT_CYAN = new Color(56, 189, 248);    // Download / Primary
    public static final Color ACCENT_EMERALD = new Color(16, 185, 129); // Upload / Success
    public static final Color ACCENT_AMBER = new Color(245, 158, 11);   // Warning / Latency
    public static final Color ACCENT_ROSE = new Color(244, 63, 94);     // Danger / Alerts
    public static final Color ACCENT_PURPLE = new Color(168, 85, 247);  // Packet / Test

    public static final Color BORDER_COLOR = new Color(51, 65, 85);

    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_SUBHEADER = new Font("SansSerif", Font.BOLD, 12);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_METRIC = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 12);

    public static Border createCardBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        );
    }

    public static Border createPaddedBorder(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    public static JButton createStyledButton(String text, Color accentColor) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_HEADER);
        btn.setBackground(accentColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(accentColor.darker(), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void styleTable(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setRowHeight(28);
        table.setFont(FONT_BODY);
        table.setSelectionBackground(new Color(56, 189, 248, 60));
        table.setSelectionForeground(TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_CARD_LIGHT);
        header.setForeground(TEXT_PRIMARY);
        header.setFont(FONT_HEADER);
        header.setPreferredSize(new Dimension(header.getWidth(), 32));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
    }
}
