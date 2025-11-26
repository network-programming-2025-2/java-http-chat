import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private Main mainApp;
    private JTextField nicknameField;

    public LoginFrame(Main mainApp) {
        this.mainApp = mainApp;

        setTitle("겜톡 로그인");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("겜톡");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        nicknameField = new JTextField(15);
        nicknameField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(nicknameField, gbc);

        JButton loginButton = new JButton("로그인");
        loginButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        add(panel);

        loginButton.addActionListener(e -> attemptLoginAction());
        nicknameField.addActionListener(e -> attemptLoginAction());
    }

    private void attemptLoginAction() {
        String nickname = nicknameField.getText().trim();
        if (!nickname.isEmpty()) {
            mainApp.attemptLogin(nickname);
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "닉네임을 입력하세요.",
                    "경고",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }
}
