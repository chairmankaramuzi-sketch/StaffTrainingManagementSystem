package com.stms.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Styling theme and helper UI builders for a modern, sleek Swing application.
 */
public class UITheme {
    // Elegant Palette
    public static final Color BG_DARK = new Color(0x0F, 0x17, 0x2A);        // Slate 900
    public static final Color BG_LIGHT = new Color(0xF8, 0xFA, 0xFC);       // Slate 50
    public static final Color BG_SURFACE = new Color(0xFF, 0xFF, 0xFF);     // White
    public static final Color BORDER_COLOR = new Color(0xE2, 0xE8, 0xF0);   // Slate 200

    public static final Color PRIMARY = new Color(0x3B, 0x82, 0xF6);        // Blue 500
    public static final Color PRIMARY_HOVER = new Color(0x25, 0x63, 0xEB);  // Blue 600
    public static final Color SECONDARY = new Color(0x64, 0x74, 0x8B);      // Slate 500

    public static final Color SUCCESS = new Color(0x10, 0xB9, 0x81);        // Emerald 500
    public static final Color WARNING = new Color(0xF5, 0x9E, 0x0B);        // Amber 500
    public static final Color DANGER = new Color(0xEF, 0x44, 0x44);         // Red 500
    public static final Color PURPLE = new Color(0x8B, 0x5C, 0xF6);         // Purple 500

    public static final Color TEXT_PRIMARY = new Color(0x1E, 0x29, 0x3B);   // Slate 800
    public static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);     // Slate 500
    public static final Color TEXT_LIGHT = new Color(0xF8, 0xFA, 0xFC);     // Slate 50

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    public static JButton createButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_BOLD);
        button.setForeground(fgColor);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_REGULAR);
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 32));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return tf;
    }

    public static <E> JComboBox<E> createComboBox(E[] items) {
        JComboBox<E> cb = new JComboBox<>(items);
        cb.setFont(FONT_REGULAR);
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(cb.getPreferredSize().width, 32));
        return cb;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xEE, 0xF2, 0xFF));
        table.setSelectionForeground(TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(0xF1, 0xF5, 0xF9));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                if (!isSel) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFA, 0xFA, 0xFC));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }
}
