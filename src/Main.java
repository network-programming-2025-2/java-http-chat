import javax.swing.*;
import java.util.*;

public class Main {
    private String myNickname;
    private LoginFrame loginFrame;
    private MainFrame mainFrame;
    private Map<String, PrivateChatFrame> privateChatFrames = new HashMap<>();
    private Client client;
    private Map<String, List<ChatMessage>> chatHistories = new HashMap<>();

    public Main() {
        this.client = new Client(this);
        this.loginFrame = new LoginFrame(this);
        loginFrame.setVisible(true);
    }

    public void attemptLogin(String nickname) {
        this.myNickname = nickname;
        if (!client.connect(nickname)) {
            this.myNickname = null;
        }
    }

    public void handleServerMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("[클라이언트 수신] " + message);
            String[] parts = message.split("::", 2);
            String command = parts[0];
            String data = parts.length > 1 ? parts[1] : "";

            if ("LOGIN_SUCCESS".equals(command)) {
                loginFrame.dispose();
                mainFrame = new MainFrame(this);
                mainFrame.setVisible(true);
                return;
            }

            if ("LOGIN_FAIL".equals(command)) {
                loginFailed(data);
                return;
            }

            if (mainFrame == null) return;

            switch (command) {
                case "GAME_START_SUCCESS":
                    mainFrame.showGame();
                    mainFrame.getGamePanel().setGameMode(data);
                    mainFrame.getGamePanel().setInputEnabled(false);
                    break;

                case "GAME_INFO":
                    mainFrame.getGamePanel().setInfoText(data);
                    mainFrame.getGamePanel().setInputEnabled(
                            data.contains(myNickname + "님의 차례입니다")
                    );
                    break;

                case "GAME_BOARD_UPDATE":
                    mainFrame.getGamePanel().updateHistory(data);
                    break;

                case "GAME_END":
                    mainFrame.getGamePanel().setInfoText("게임 종료! 결과: " + data);
                    mainFrame.getGamePanel().setInputEnabled(false);
                    JOptionPane.showMessageDialog(
                            mainFrame,
                            "게임 종료!\n결과: " + data,
                            "게임 종료",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    mainFrame.showChat();
                    break;

                case "GAME_REVEAL":
                    // 형식: GAME_REVEAL::정답::요청자
                    String[] r = data.split("::", 2);
                    String answer = r[0];
                    String requester = (r.length > 1) ? r[1] : "";
                    JOptionPane.showMessageDialog(
                            mainFrame,
                            "정답: " + answer + "\n(요청자: " + requester + ")",
                            "정답 공개",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    mainFrame.getGamePanel().setInfoText("정답: " + answer);
                    mainFrame.getGamePanel().setInputEnabled(false);
                    break;

                case "GAME_TIMER":
                    try {
                        int sec = Integer.parseInt(data);
                        mainFrame.getGamePanel().updateTimer(sec);
                    } catch (NumberFormatException ignored) {}
                    break;

                case "USER_LIST":
                case "NEW_USER":
                case "EXIT_USER":
                    mainFrame.processServerMessage(message);
                    break;

                case "PUBLIC_MSG":
                    String[] msgP = data.split("::", 2);
                    ChatMessage pubMsg = new ChatMessage(
                            msgP[0],
                            msgP[1],
                            msgP[0].equals(myNickname)
                    );
                    chatHistories
                            .computeIfAbsent("전체", k -> new ArrayList<>())
                            .add(pubMsg);
                    mainFrame.addChatMessage(pubMsg);
                    break;

                case "PRIVATE_MSG":
                    String[] pmP = data.split("::", 3);
                    String sender = pmP[0];
                    String receiver = pmP[1];
                    String pm = pmP[2];

                    if (sender.equals(myNickname) || receiver.equals(myNickname)) {
                        String partner = sender.equals(myNickname) ? receiver : sender;
                        ChatMessage prvMsg = new ChatMessage(
                                sender, pm, sender.equals(myNickname)
                        );
                        chatHistories
                                .computeIfAbsent(partner, k -> new ArrayList<>())
                                .add(prvMsg);

                        PrivateChatFrame targetFrame = privateChatFrames.get(partner);
                        if (targetFrame != null) {
                            targetFrame.addChatMessage(prvMsg);
                        } else if (!sender.equals(myNickname)) {
                            mainFrame.showNotificationFor(partner);
                            startPrivateChat(partner);
                        }
                    }
                    break;

                case "SERVER_DOWN":
                    JOptionPane.showMessageDialog(
                            null,
                            "서버와의 연결이 끊어졌습니다.",
                            "연결 오류",
                            JOptionPane.ERROR_MESSAGE
                    );
                    System.exit(0);
                    break;
            }
        });
    }

    public void startPrivateChat(String partner) {
        if (myNickname.equals(partner)) return;

        privateChatFrames.computeIfAbsent(partner, p -> {
            PrivateChatFrame newChatFrame = new PrivateChatFrame(this, p);
            List<ChatMessage> history = chatHistories.get(p);
            if (history != null) history.forEach(newChatFrame::addChatMessage);
            newChatFrame.setVisible(true);
            return newChatFrame;
        }).toFront();
    }

    public void closePrivateChat(String partner) {
        privateChatFrames.remove(partner);
    }

    public void loginFailed(String reason) {
        JOptionPane.showMessageDialog(
                loginFrame,
                reason,
                "로그인 실패",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public Client getClient() { return client; }
    public String getMyNickname() { return myNickname; }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {}
        SwingUtilities.invokeLater(Main::new);
    }
}
