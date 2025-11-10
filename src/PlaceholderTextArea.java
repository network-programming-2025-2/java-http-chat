// PlaceholderTextArea.java (새 파일로 생성)

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class PlaceholderTextArea extends JTextArea {
    private String placeholder;

    public PlaceholderTextArea(String placeholder) {
        this.placeholder = placeholder;
        addFocusListener(new PlaceholderFocusListener());
        showPlaceholder();
    }

    private void showPlaceholder() {
        setText(placeholder);
        setForeground(Color.GRAY);
    }

    public String getRealText() {
        if (getText().equals(placeholder)) {
            return "";
        }
        return getText();
    }
    
    private class PlaceholderFocusListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {
            if (getText().equals(placeholder)) {
                setText("");
                setForeground(Color.BLACK);
            }
        }
        @Override
        public void focusLost(FocusEvent e) {
            if (getText().trim().isEmpty()) {
                showPlaceholder();
            }
        }
    }
}
