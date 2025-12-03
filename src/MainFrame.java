import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private Main mainApp;
    private JPanel userListContentPanel;
    private CardLayout chatCardLayout;
    private JPanel chatPanelContainer;
    private DefaultChatPanel defaultChatPanel;
    private GamePanel gamePanel;

    public MainFrame(Main mainApp) {
        this.mainApp = mainApp;

        setTitle("겜톡 로비 - " + mainApp.getMyNickname());
        setSize(800, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createUserListPanel(), BorderLayout.WEST);
        createChatPanelContainer();
        add(chatPanelContainer, BorderLayout.CENTER);
    }

    private void createChatPanelContainer() {
        chatCardLayout = new CardLayout();
        chatPanelContainer = new JPanel(chatCardLayout);

        defaultChatPanel = new DefaultChatPanel(mainApp);
        gamePanel = new GamePanel(mainApp);

        chatPanelContainer.add(defaultChatPanel, "CHAT");
        chatPanelContainer.add(gamePanel, "GAME");

        showChat();
    }

    public void showGame() {
        chatCardLayout.show(chatPanelContainer, "GAME");
    }

    public void showChat() {
        chatCardLayout.show(chatPanelContainer, "CHAT");
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public void processServerMessage(String message) {
        String[] parts = message.split("::", 2);
        String command = parts[0];
        String data = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "USER_LIST":
                clearUserList();
                for (String user : data.split(",")) {
                    if (!user.trim().isEmpty()) addUser(user);
                }
                break;
            case "NEW_USER":
                if (!data.equals(mainApp.getMyNickname())) addUser(data);
                break;
            case "EXIT_USER":
                removeUser(data);
                break;
        }
    }

    public void addChatMessage(ChatMessage msg) {
        defaultChatPanel.addChatMessage(msg);
    }

    public void showNotificationFor(String nickname) {
        for (Component comp : userListContentPanel.getComponents()) {
            if (comp instanceof CustomUserButton &&
                    ((CustomUserButton) comp).getNickname().equals(nickname)) {
                ((CustomUserButton) comp).setNotification(true);
                break;
            }
        }
    }

    private JPanel createUserListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(220, 0));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(new LineBorder(new Color(220, 220, 220)));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(new Color(240, 240, 240));
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(new EmptyBorder(10, 10, 10, 10));

        header.add(new JLabel("My Profile: " + mainApp.getMyNickname()));
        panel.add(header, BorderLayout.NORTH);

        userListContentPanel = new JPanel();
        userListContentPanel.setLayout(new BoxLayout(userListContentPanel, BoxLayout.Y_AXIS));
        userListContentPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(userListContentPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void addUser(String nickname) {
        userListContentPanel.add(new CustomUserButton(nickname, mainApp));
        revalidateUserList();
    }

    private void removeUser(String nickname) {
        for (Component comp : userListContentPanel.getComponents()) {
            if (comp instanceof CustomUserButton &&
                    ((CustomUserButton) comp).getNickname().equals(nickname)) {
                userListContentPanel.remove(comp);
                break;
            }
        }
        revalidateUserList();
    }

    private void clearUserList() {
        userListContentPanel.removeAll();
        revalidateUserList();
    }

    private void revalidateUserList() {
        userListContentPanel.revalidate();
        userListContentPanel.repaint();
    }
}
