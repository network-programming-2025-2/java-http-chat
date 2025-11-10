// PlaceholderTextField.java (새 파일로 생성)

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class PlaceholderTextField extends JTextField {

    private String placeholder;

    public PlaceholderTextField(String placeholder) {
        this.placeholder = placeholder;
        addFocusListener(new PlaceholderFocusListener());
        showPlaceholder(); // 초기 상태 설정
    }

    private void showPlaceholder() {
        setText(placeholder);
        setForeground(Color.GRAY);
    }

    // 포커스 리스너 내부 클래스
    private class PlaceholderFocusListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {
            // 포커스를 얻었을 때, 텍스트가 플레이스홀더와 같다면 비워줌
            if (getText().equals(placeholder)) {
                setText("");
                setForeground(Color.BLACK);
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            // 포커스를 잃었을 때, 텍스트가 비어있다면 플레이스홀더를 다시 보여줌
            if (getText().trim().isEmpty()) {
                showPlaceholder();
            }
        }
    }
}
