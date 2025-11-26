import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GamePanel extends JPanel {
    private Main mainApp;
    private JLabel infoLabel;
    private JLabel timerLabel;
    private JTextArea historyArea;
    private JTextField inputField;
    private JButton submitButton;
    private JButton revealButton; // 숫자야구 정답 확인 버튼
    private String currentGameType = "NONE"; // NUMBER_BASEBALL, WORD_CHAIN, NONE

    public GamePanel(Main mainApp) {
        this.mainApp = mainApp;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 상단 안내 라벨
        infoLabel = new JLabel("게임 대기 중...", SwingConstants.CENTER);
        infoLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        add(infoLabel, BorderLayout.NORTH);

        // 가운데 히스토리 영역
        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(historyArea);
        add(scrollPane, BorderLayout.CENTER);

        // 오른쪽 타이머 라벨
        timerLabel = new JLabel("남은 시간: -", SwingConstants.CENTER);
        timerLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        add(timerLabel, BorderLayout.EAST);

        // 하단 입력 + 버튼 영역
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));

        inputField = new JTextField();
        inputField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        submitButton = new JButton("입력");
        revealButton = new JButton("정답확인"); // 숫자야구에서만 활성화

        // 엔터 키로 입력
        inputField.addActionListener(e -> submitAction());
        submitButton.addActionListener(e -> submitAction());

        // 정답 확인은 서버에 요청
        revealButton.addActionListener(e ->
                mainApp.getClient().sendMessage("GAME_REVEAL_REQUEST::")
        );

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnPanel.add(submitButton);
        btnPanel.add(revealButton);

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(btnPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // 처음에는 입력 비활성화
        setInputEnabled(false);
        revealButton.setEnabled(false);
    }

    /** 입력값 서버로 전송 */
    private void submitAction() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        mainApp.getClient().sendMessage("GAME_ACTION::" + text);
        inputField.setText("");
    }

    /** 상단 안내 문구 설정 */
    public void setInfoText(String text) {
        infoLabel.setText(
                "<html><div style='text-align: center;'>" +
                        text.replaceAll("\n", "<br>") +
                        "</div></html>"
        );
    }

    /** 게임 기록 갱신 (서버에서 \\n 형태로 온 걸 \n으로 복원) */
    public void updateHistory(String history) {
        String decoded = history.replace("\\n", "\n");
        historyArea.setText(decoded);
        historyArea.setCaretPosition(historyArea.getDocument().getLength());
        historyArea.revalidate();
        historyArea.repaint();
    }

    /** 입력 / 버튼 활성화 여부 */
    public void setInputEnabled(boolean enabled) {
        inputField.setEnabled(enabled);
        submitButton.setEnabled(enabled);
    }

    /** 타이머 UI 갱신 */
    public void updateTimer(int sec) {
        if (sec < 0) {
            timerLabel.setText("남은 시간: -");
        } else {
            timerLabel.setText("남은 시간: " + sec + "초");
        }
    }

    /** 🔥 Main에서 호출하는 메서드: 게임 타입에 따라 UI 초기화 */
    public void setGameMode(String type) {
        this.currentGameType = type;
        historyArea.setText("");
        updateTimer(-1);

        if ("NUMBER_BASEBALL".equals(type)) {
            setInfoText("숫자 야구 게임에 오신 것을 환영합니다!");
            revealButton.setEnabled(true);   // 숫자야구에서는 정답확인 가능
        } else if ("WORD_CHAIN".equals(type)) {
            setInfoText("끝말잇기 게임에 오신 것을 환영합니다!");
            revealButton.setEnabled(false);  // 끝말잇기는 정답 개념 X
        } else {
            setInfoText("게임 대기 중...");
            revealButton.setEnabled(false);
        }
    }
}
