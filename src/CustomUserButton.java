// CustomUserButton.java 파일의 내용을 아래 코드로 전체 교체하세요.

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomUserButton extends JButton {

    private String nickname; // 사용자의 닉네임을 저장할 필드 추가
    private Color defaultColor = Color.WHITE;
    private Color hoverColor = new Color(245, 245, 245);

    public CustomUserButton(String nickname) {
        super(nickname);
        this.nickname = nickname;

        setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        setBackground(defaultColor);
        setBorder(new EmptyBorder(15, 20, 15, 20));
        setHorizontalAlignment(SwingConstants.LEFT);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(defaultColor);
            }
        });
        
        // [추가] 버튼 클릭 시 어떤 동작을 할지 여기에 추가 가능
        addActionListener(e -> {
            // 예: 닉네임을 콘솔에 출력
            System.out.println(this.nickname + " 버튼 클릭됨!");
            // 나중에는 여기에 1:1 대화창을 여는 코드를 넣을 수 있습니다.
        });
    }

    public String getNickname() {
        return nickname;
    }
}
