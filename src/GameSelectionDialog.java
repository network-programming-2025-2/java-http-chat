import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameSelectionDialog extends JDialog {
    private Main mainApp;

    public GameSelectionDialog(Frame owner, Main mainApp) {
        super(owner, "게임 선택", true);
        this.mainApp = mainApp;

        // 전체 패널에 여백 주기
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(content);

        // 2 x 2 그리드로 버튼 정렬
        JPanel grid = new JPanel(new GridLayout(2, 2, 20, 20));

        // 숫자 야구 (현재는 다자 게임 그대로)
        JButton baseballButton = new JButton("숫자 야구");
        baseballButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        baseballButton.addActionListener(e -> {
            // 🔥 기존 방식 유지: 전체 유저 대상
            mainApp.getClient().sendMessage("GAME_CREATE_REQUEST::NUMBER_BASEBALL");
            dispose();
        });
        grid.add(baseballButton);

        // 끝말잇기 (현재는 다자 게임 그대로)
        JButton wordChainButton = new JButton("끝말잇기");
        wordChainButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        wordChainButton.addActionListener(e -> {
            mainApp.getClient().sendMessage("GAME_CREATE_REQUEST::WORD_CHAIN");
            dispose();
        });
        grid.add(wordChainButton);

        // 업다운 (앞으로 1:1도 가능하게 할 수 있지만 일단 전체 대상으로)
        JButton upDownButton = new JButton("업다운");
        upDownButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        upDownButton.addActionListener(e -> {
            mainApp.getClient().sendMessage("GAME_CREATE_REQUEST::UPDOWN");
            dispose();
        });
        grid.add(upDownButton);

        // 오목게임 (🔥 여기서만 2인용 상대 선택 적용)
        JButton omokButton = new JButton("오목게임");
        omokButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        omokButton.addActionListener(e -> {
            // 온라인 유저 목록에서 나를 제외한 사람들만 후보
            List<String> candidates = new ArrayList<>();
            for (String user : mainApp.getOnlineUsers()) {
                if (!user.equals(mainApp.getMyNickname())) {
                    candidates.add(user);
                }
            }

            if (candidates.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "함께 플레이할 상대가 없습니다.",
                        "오목",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            String opponent = PlayerSelectDialog.choosePlayer(this, candidates);
            if (opponent != null && !opponent.trim().isEmpty()) {
                // 🔥 1:1 오목 게임 생성 요청: GAME_CREATE_REQUEST::OMOK::상대닉
                mainApp.getClient().sendMessage("GAME_CREATE_REQUEST::OMOK::" + opponent.trim());
                dispose();
            }
        });
        grid.add(omokButton);

        content.add(grid, BorderLayout.CENTER);

        // 크기 조정 후 가운데 배치
        setSize(480, 260);
        setLocationRelativeTo(owner);
    }

    // 🔥 상대 선택용 다이얼로그 (콤보박스 + JOptionPane)
    static class PlayerSelectDialog {
        public static String choosePlayer(Component parent, java.util.List<String> candidates) {
            String[] arr = candidates.toArray(new String[0]);
            return (String) JOptionPane.showInputDialog(
                    parent,
                    "상대를 선택하세요:",
                    "상대 선택",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    arr,
                    arr[0]
            );
        }
    }
}
