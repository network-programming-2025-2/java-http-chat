// LoginFrame.java (새 파일로 생성)

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final Client client;

    private JTextField nicknameField;
    private JButton loginButton;

    public LoginFrame(Client client) {
        this.client = client;

        // --- 1. 기본 창 설정 ---
        setTitle("겜톡");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(380, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(254, 229, 0)); // 카카오 노란색 배경

        // --- 2. 메인 패널 (모든 컴포넌트를 담을 패널) ---
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout()); // 정교한 배치를 위해 GridBagLayout 사용
        mainPanel.setOpaque(false); // 배경 투명
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // 컴포넌트 간 여백
        gbc.gridwidth = GridBagConstraints.REMAINDER; // 이 컴포넌트가 한 줄을 다 차지
        gbc.fill = GridBagConstraints.HORIZONTAL; // 가로로 꽉 채움

        // --- 3. 로고 이미지 (임시 텍스트로 대체) ---
        JLabel logoLabel = new JLabel("TALK", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 48));
        gbc.insets = new Insets(50, 0, 50, 0); // 위아래 여백 많이
        mainPanel.add(logoLabel, gbc);

        // --- 4. 닉네임 입력 필드 ---
        nicknameField = new JTextField("닉네임을 입력하세요");
        nicknameField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        nicknameField.setPreferredSize(new Dimension(300, 40));
        nicknameField.setForeground(Color.GRAY);
        nicknameField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220)),
            new EmptyBorder(5, 10, 5, 10)
        ));
        gbc.insets = new Insets(5, 0, 5, 0);
        mainPanel.add(nicknameField, gbc);
        
        // --- 5. 로그인 버튼 ---
        loginButton = new JButton("로그인");
        loginButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(300, 40));
        loginButton.setBackground(new Color(240, 240, 240));
        loginButton.setBorder(new LineBorder(new Color(220, 220, 220)));
        loginButton.setFocusPainted(false);
        mainPanel.add(loginButton, gbc);

        // --- 6. 이벤트 리스너 추가 ---
        loginButton.addActionListener(_ -> onSubmit());
        nicknameField.addActionListener(_ -> onSubmit()); // 엔터키로도 로그인

        // 플레이스홀더 기능 (FocusListener)
        nicknameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (nicknameField.getText().equals("닉네임을 입력하세요")) {
                    nicknameField.setText("");
                    nicknameField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (nicknameField.getText().isEmpty()) {
                    nicknameField.setText("닉네임을 입력하세요");
                    nicknameField.setForeground(Color.GRAY);
                }
            }
        });

        add(mainPanel, BorderLayout.CENTER);
    }

    private void onSubmit() {
        String nickname = nicknameField.getText().trim();
        if (nickname.isEmpty() || nickname.equals("닉네임을 입력하세요")) {
            JOptionPane.showMessageDialog(this, "닉네임을 입력해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }

        this.client.attemptLogin(nickname);
    }
}

  