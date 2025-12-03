import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private Main mainApp;
    private JTextField nicknameField;

    public LoginFrame(Main mainApp) {
        this.mainApp = mainApp;

        setTitle("겜톡 로그인");
        setSize(360, 520);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 노란 배경
        JPanel bgPanel = new JPanel(new BorderLayout());
        bgPanel.setBackground(new Color(254, 229, 0));
        bgPanel.setBorder(new EmptyBorder(40, 30, 40, 30));
        setContentPane(bgPanel);

        // 가운데 세로 박스 레이아웃
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        bgPanel.add(centerPanel, BorderLayout.CENTER);

        // 위아래 여백으로 전체 중앙 정렬 느낌
        centerPanel.add(Box.createVerticalGlue());

        // TALK 라벨
        JLabel titleLabel = new JLabel("TALK");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 40));
        titleLabel.setForeground(new Color(48, 48, 48));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(titleLabel);

        centerPanel.add(Box.createVerticalStrut(60));

        // 입력 영역 패널
        JPanel inputPanel = new JPanel();
        inputPanel.setOpaque(false);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 닉네임 텍스트필드 감싸는 흰 박스
        JPanel nickWrapper = new JPanel(new BorderLayout());
        nickWrapper.setMaximumSize(new Dimension(260, 38));
        nickWrapper.setBackground(Color.WHITE);
        nickWrapper.setBorder(new LineBorder(new Color(220, 220, 220)));

        nicknameField = new JTextField();
        nicknameField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        nicknameField.setBorder(new EmptyBorder(5, 8, 5, 8));
        nickWrapper.add(nicknameField, BorderLayout.CENTER);

        // 로그인 버튼
        JButton loginButton = new JButton("로그인");
        loginButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        loginButton.setMaximumSize(new Dimension(260, 40));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setFocusPainted(false);
        loginButton.setBackground(Color.WHITE);
        loginButton.setBorder(new LineBorder(new Color(220, 220, 220)));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        inputPanel.add(nickWrapper);
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(loginButton);

        centerPanel.add(inputPanel);

        centerPanel.add(Box.createVerticalGlue());

        // 하단 안내 텍스트
        JLabel hintLabel = new JLabel("닉네임만으로 간편 로그인", SwingConstants.CENTER);
        hintLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        hintLabel.setForeground(new Color(80, 80, 80));
        hintLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        bgPanel.add(hintLabel, BorderLayout.SOUTH);

        // 이벤트 연결
        loginButton.addActionListener(e -> attemptLoginAction());
        nicknameField.addActionListener(e -> attemptLoginAction());
    }

    private void attemptLoginAction() {
        String nickname = nicknameField.getText().trim();
        if (!nickname.isEmpty()) {
            mainApp.attemptLogin(nickname);
        } else {
            JOptionPane.showMessageDialog(this, "닉네임을 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
        }
    }
}
