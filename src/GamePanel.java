import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class GamePanel extends JPanel {
    private Main mainApp;
    private JLabel infoLabel;
    private JLabel timerLabel;
    private JTextArea historyArea;
    private JTextField inputField;
    private JButton submitButton;
    private JButton revealButton; // 숫자야구 정답 확인 버튼
    private String currentGameType = "NONE"; // NUMBER_BASEBALL, WORD_CHAIN, UPDOWN, OMOK, NONE

    // 오목 보드 관련
    private OmokBoardPanel omokBoardPanel;
    private JPanel rightPanel;

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

        // 오른쪽(타이머 + 오목 판)
        timerLabel = new JLabel("남은 시간: -", SwingConstants.CENTER);
        timerLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(245, 222, 179));
        // 오목할 때 넉넉하게 공간 확보
        rightPanel.setPreferredSize(new Dimension(620, 0));

        // 위쪽 타이머
        JPanel timerWrapper = new JPanel(new BorderLayout());
        timerWrapper.setOpaque(false);
        timerWrapper.setBorder(new EmptyBorder(10, 10, 10, 10));
        timerWrapper.add(timerLabel, BorderLayout.NORTH);
        rightPanel.add(timerWrapper, BorderLayout.NORTH);

        // 가운데 오목판
        omokBoardPanel = new OmokBoardPanel(mainApp);
        omokBoardPanel.setBorder(new LineBorder(new Color(180, 140, 90)));
        omokBoardPanel.setVisible(false); // 기본은 안 보이게
        rightPanel.add(omokBoardPanel, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.EAST);

        // 하단 입력 + 버튼 영역
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));

        inputField = new JTextField();
        inputField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        submitButton = new JButton("입력");
        revealButton = new JButton("정답확인"); // 숫자야구에서만 활성화

        // 엔터 키로 입력
        inputField.addActionListener(e -> submitAction());
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

    // ================================
    // 🔥 Main → GamePanel → OmokBoardPanel 연동용 메서드
    // ================================

    /** 플레이어의 돌 색 지정 (흑/백) */
    public void setOmokPlayerColor(boolean isBlack) {
        omokBoardPanel.setPlayerStone(isBlack);
    }

    /** 서버로부터 받은 착수를 보드에 반영 */
    public void applyOmokMove(int x, int y, boolean isBlack) {
        omokBoardPanel.placeStone(x, y, isBlack);
    }

    /** 턴 정보 설정 */
    public void setOmokTurn(boolean myTurn) {
        omokBoardPanel.setTurn(myTurn);
    }


    /** 🔥 Main에서 호출하는 메서드: 게임 타입에 따라 UI 초기화 */
    public void setGameMode(String type) {
        this.currentGameType = type;
        historyArea.setText("");
        updateTimer(-1);

        // 기본값
        omokBoardPanel.setVisible(false);
        setInputEnabled(true);         // 대부분의 게임은 텍스트 입력 사용
        revealButton.setEnabled(false);

        if ("NUMBER_BASEBALL".equals(type)) {
            setInfoText("숫자 야구 게임에 오신 것을 환영합니다!\n서로 다른 4자리 숫자를 맞혀보세요.");
            revealButton.setEnabled(true);   // 숫자야구에서만 정답확인 가능

        } else if ("WORD_CHAIN".equals(type)) {
            setInfoText("끝말잇기 게임에 오신 것을 환영합니다!\n한글 단어로만 이어가세요.");

        } else if ("UPDOWN".equals(type)) {
            setInfoText("업다운 게임에 오신 것을 환영합니다!\n1~100 사이 숫자를 맞혀보세요.");

        } else if ("OMOK".equals(type)) {
            // 창 자동 확대 (최대화 안 눌러도 넉넉하게 보이도록)
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) {
                int targetW = 1200;
                int targetH = 800;
                int newW = Math.max(w.getWidth(), targetW);
                int newH = Math.max(w.getHeight(), targetH);
                if (newW != w.getWidth() || newH != w.getHeight()) {
                    w.setSize(newW, newH);
                }
            }

            omokBoardPanel.resetBoard();
            omokBoardPanel.setVisible(true);

            setInfoText(
                    "오목 게임에 오신 것을 환영합니다!\n" +
                            "흑/백이 번갈아가며 돌을 둡니다.\n" +
                            "판을 클릭해서 수를 두세요.\n" +
                            "5목이 먼저 만들어지면 승리입니다."
            );

            // 오목은 클릭으로만 두니까 텍스트 입력 막기
            setInputEnabled(false);
            revealButton.setEnabled(false);

        } else {
            setInfoText("게임 대기 중...");
        }
    }
}
