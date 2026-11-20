package com.netmonitor.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable dashboard card component for displaying key performance indicators (KPIs).
 */
public class MetricCard extends JPanel {

    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel subLabel;
    private final Color accentColor;

    public MetricCard(String title, String initialValue, String initialSub, Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new BorderLayout(8, 6));
        setBackground(UITheme.BG_CARD);
        setBorder(UITheme.createCardBorder());

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setOpaque(false);

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(UITheme.FONT_SUBHEADER);
        titleLabel.setForeground(UITheme.TEXT_MUTED);
        headerPanel.add(titleLabel);

        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(UITheme.FONT_METRIC);
        valueLabel.setForeground(accentColor);

        subLabel = new JLabel(initialSub);
        subLabel.setFont(UITheme.FONT_SMALL);
        subLabel.setForeground(UITheme.TEXT_SECONDARY);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.add(valueLabel);
        centerPanel.add(Box.createVerticalStrut(4));
        centerPanel.add(subLabel);

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void updateValues(String value, String sub) {
        valueLabel.setText(value);
        subLabel.setText(sub);
    }

    public void setValueColor(Color color) {
        valueLabel.setForeground(color);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw left accent bar
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 8, 4, getHeight() - 16, 4, 4);
        g2.dispose();
    }
}
