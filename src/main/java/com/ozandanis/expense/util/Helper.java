package com.ozandanis.expense.util;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class Helper {
    private static final Color ERROR_COLOR = new Color(255, 235, 238);
    private static final Color WARNING_COLOR = new Color(255, 249, 196);
    private static final Color INFO_COLOR = new Color(227, 242, 253);
    private static final Color SUCCESS_COLOR = new Color(232, 245, 233);

    public enum MessageType {
        ERROR(ERROR_COLOR, "Hata"),
        WARNING(WARNING_COLOR, "Uyarı"),
        INFO(INFO_COLOR, "Bilgi"),
        SUCCESS(SUCCESS_COLOR, "Başarılı"),
        PLAIN(null, "Mesaj");

        private final Color bgColor;
        private final String title;

        MessageType(Color bgColor, String title) {
            this.bgColor = bgColor;
            this.title = title;
        }
    }

    // Temel gösterim metodu
    private static int showOptionDialog(Component parent, String message, String title,
                                        MessageType type, Object[] options, Object defaultOption) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (type.bgColor != null) {
            panel.setBackground(type.bgColor);
            panel.setOpaque(true);
        }

        JLabel messageLabel = new JLabel(message);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(messageLabel, BorderLayout.CENTER);

        return JOptionPane.showOptionDialog(parent, panel, title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, defaultOption);
    }

    // Hızlı erişim metodları
    public static void showError(Component parent, String message) {
        showOptionDialog(parent, message, MessageType.ERROR.title,
                MessageType.ERROR, new Object[]{"Tamam"}, "Tamam");
    }

    public static void showInfo(Component parent, String message) {
        showOptionDialog(parent, message, MessageType.INFO.title,
                MessageType.INFO, new Object[]{"Tamam"}, "Tamam");
    }

    public static boolean showConfirm(Component parent, String message) {
        int result = showOptionDialog(parent, message, "Onay",
                MessageType.WARNING,
                new Object[]{"Evet", "Hayır"}, "Hayır");
        return result == 0;
    }

    public static void showSuccess(Component parent, String message) {
        showOptionDialog(parent, message, MessageType.SUCCESS.title,
                MessageType.SUCCESS, new Object[]{"Tamam"}, "Tamam");
    }

    // Özelleştirilmiş giriş kutusu
    public static String showInput(Component parent, String message, String defaultValue) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(INFO_COLOR);
        panel.setOpaque(true);

        JLabel label = new JLabel(message);
        JTextField textField = new JTextField(defaultValue);
        textField.setPreferredSize(new Dimension(200, 25));

        panel.add(label, BorderLayout.NORTH);
        panel.add(textField, BorderLayout.CENTER);

        int result = JOptionPane.showOptionDialog(parent, panel, "Giriş",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, new Object[]{"Tamam", "İptal"}, "Tamam");

        return result == 0 ? textField.getText() : null;
    }

}