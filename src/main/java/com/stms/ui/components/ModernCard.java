package com.stms.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Modern dashboard summary card component.
 */
public class ModernCard extends JPanel {
    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSubtitle;
    private final Color accentColor;

    public ModernCard(String title, String initialValue, String subtitle, Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(190, 105));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        lblValue = new JLabel(initialValue);
        lblValue.setFont(UITheme.FONT_TITLE.deriveFont(24f));
        lblValue.setForeground(UITheme.TEXT_PRIMARY);

        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(UITheme.FONT_SMALL);
        lblSubtitle.setForeground(accentColor);

        JPanel content = new JPanel(new GridLayout(3, 1, 0, 4));
        content.setOpaque(false);
        content.add(lblTitle);
        content.add(lblValue);
        content.add(lblSubtitle);

        add(content, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    public void setSubtitle(String subtitle) {
        lblSubtitle.setText(subtitle);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Card background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

        // Top accent border stripe
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);

        // Border outline
        g2.setColor(UITheme.BORDER_COLOR);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

        g2.dispose();
        super.paintComponent(g);
    }
}
