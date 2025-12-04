import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel {
    private Main mainApp;

    private JLabel infoLabel;
    private JLabel timerLabel;

    // 센터는 카드 레이아웃으로: 텍스트형 게임 / 오목 전환
    private CardLayout centerCardLayout;
    private JPanel centerCardPanel;

    // 텍스트 게임용
    private JTextArea historyArea;
    private JScrollPane historyScrollPane;

    // 오목 보드
    private OmokBoardPanel omokBoardPanel;

    // 하단 입력 영역
    private JTextField inputField;
    private JButton submitButton;
    private JButton revealButton; // 숫자야구 정답 확인 버튼

    private String currentGameType = "NONE"; // NUMBER_BASEBALL, WORD_CHAIN, UPDOWN, OMOK, NONE

    public GamePanel(Main mainApp) {
        this.mainApp = mainApp;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 상단 안내 라벨
        infoLabel = new JLabel("게임 대기 중...", SwingConstants.CENTER);
        infoLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        add(infoLabel, BorderLayout.NORTH);

        // 오른쪽 타이머 라벨
        timerLabel = new JLabel("남은 시간: -", SwingConstants.CENTER);
        timerLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        add(timerLabel, BorderLayout.EAST);

        // 센터 카드 레이아웃
        centerCardLayout = new CardLayout();
        centerCardPanel = new JPanel(centerCardLayout);

        // 1) 텍스트 게임용 패널
        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        historyScrollPane = new JScrollPane(historyArea);
        JPanel textGamePanel = new JPanel(new BorderLayout());
        textGamePanel.add(historyScrollPane, BorderLayout.CENTER);

        // 2) 오목용 패널
        omokBoardPanel = new OmokBoardPanel(mainApp);
        JPanel omokPanel = new JPanel(new BorderLayout());
        omokPanel.add(omokBoardPanel, BorderLayout.CENTER);

        centerCardPanel.add(textGamePanel, "TEXT");
        centerCardPanel.add(omokPanel, "OMOK");

        add(centerCardPanel, BorderLayout.CENTER);

        // 하단 입력 + 버튼 영역
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));

        inputField = new JTextField();
        inputField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        submitButton = new JButton("입력");
        revealButton = new JButton("정답확인"); // 숫자야구에서만 활성화

        // 엔터 키로 입력
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    submitAction();
                }
            }
        });
        submitButton.addActionListener(e -> submitAction());

        // 정답 확인은 서버에 요청 (숫자야구 전용)
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

        // 입력 내용에 따라 버튼 색 변경 (선택)
        inputField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void insertUpdate(DocumentEvent e) { update(); }
            private void update() {
                boolean active = !inputField.getText().trim().isEmpty();
                submitButton.setEnabled(active);
            }
        });
    }

    /** 입력값 서버로 전송 (텍스트형 게임용) */
    private void submitAction() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        // 모든 텍스트형 게임은 GAME_ACTION으로 처리 (숫자야구, 끝말잇기, 업다운)
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

    /** 게임 기록 갱신 (서버에서 \\n 형태로 온 걸 \n으로 복원) – 텍스트형 게임 전용 */
    public void updateHistory(String history) {
        if (!"OMOK".equals(currentGameType)) {
            String decoded = history.replace("\\n", "\n");
            historyArea.setText(decoded);
            historyArea.setCaretPosition(historyArea.getDocument().getLength());
            historyArea.revalidate();
            historyArea.repaint();
        }
    }

    /** 입력 / 버튼 활성화 여부 (텍스트 게임용) */
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

    /** 현재 게임 타입 조회 (Main에서 사용) */
    public String getCurrentGameType() {
        return currentGameType;
    }

    /** Main에서 호출: 게임 타입에 따라 UI 초기화 */
    public void setGameMode(String type) {
        this.currentGameType = type;
        updateTimer(-1);

        if ("NUMBER_BASEBALL".equals(type)) {
            centerCardLayout.show(centerCardPanel, "TEXT");
            historyArea.setText("");
            setInfoText("숫자 야구 게임에 오신 것을 환영합니다!\n서로 다른 4자리 숫자를 맞혀보세요.");
            revealButton.setEnabled(true);   // 숫자야구에서만 정답확인 가능
            setInputEnabled(false);          // 턴 안내에서 켜짐

        } else if ("WORD_CHAIN".equals(type)) {
            centerCardLayout.show(centerCardPanel, "TEXT");
            historyArea.setText("");
            setInfoText("끝말잇기 게임에 오신 것을 환영합니다!\n한글 단어로만 이어가세요.");
            revealButton.setEnabled(false);
            setInputEnabled(false);          // 턴 안내에서 켜짐

        } else if ("UPDOWN".equals(type)) {
            centerCardLayout.show(centerCardPanel, "TEXT");
            historyArea.setText("");
            setInfoText("업다운 게임에 오신 것을 환영합니다!\n1~100 사이 숫자를 맞혀보세요.");
            revealButton.setEnabled(false);
            // 업다운은 시작과 동시에 입력 가능 (Main에서 켜줌)

        } else if ("OMOK".equals(type)) {
            centerCardLayout.show(centerCardPanel, "OMOK");
            omokBoardPanel.resetBoard();
            setInfoText(
                    "오목 게임에 오신 것을 환영합니다!\n" +
                            "흑/백이 번갈아가며 돌을 둡니다.\n" +
                            "마우스로 원하는 위치를 클릭해서 돌을 두세요."
            );
            revealButton.setEnabled(false);
            setInputEnabled(false); // 오목은 클릭만 사용

        } else {
            centerCardLayout.show(centerCardPanel, "TEXT");
            historyArea.setText("");
            setInfoText("게임 대기 중...");
            revealButton.setEnabled(false);
            setInputEnabled(false);
        }
    }

    /** 서버에서 오목 수가 확정되었을 때 호출 (보드에 반영) */
    public void applyOmokMove(int row, int col, boolean isBlack) {
        if (omokBoardPanel != null) {
            omokBoardPanel.applyMove(row, col, isBlack);
        }
    }

    /** 서버에서 플레이어 색깔 정보 전달 시 (선택적으로 처리) */
    public void setOmokRole(String role) {
        if ("BLACK".equals(role)) {
            setInfoText("당신은 흑(●)입니다.\n차례 안내에 따라 마우스로 돌을 두세요.");
        } else if ("WHITE".equals(role)) {
            setInfoText("당신은 백(○)입니다.\n차례 안내에 따라 마우스로 돌을 두세요.");
        } else {
            setInfoText("관전자 모드입니다.\n보드 진행만 관전할 수 있습니다.");
        }
    }
}
