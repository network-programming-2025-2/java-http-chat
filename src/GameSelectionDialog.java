import javax.swing.*;
import java.awt.*;

public class GameSelectionDialog extends JDialog {
    private Main mainApp;

    public GameSelectionDialog(Frame owner, Main mainApp) {
        super(owner, "게임 선택", true);
        this.mainApp = mainApp;

        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

        JButton baseballButton = new JButton("숫자 야구");
        baseballButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        baseballButton.setPreferredSize(new Dimension(150, 80));
        baseballButton.addActionListener(e -> {
            mainApp.getClient().sendMessage("GAME_CREATE_REQUEST::NUMBER_BASEBALL");
            dispose();
        });
        add(baseballButton);

        JButton wordChainButton = new JButton("끝말잇기");
        wordChainButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        wordChainButton.setPreferredSize(new Dimension(150, 80));
        wordChainButton.addActionListener(e -> {
            mainApp.getClient().sendMessage("GAME_CREATE_REQUEST::WORD_CHAIN");
            dispose();
        });
        add(wordChainButton);

        setSize(400, 180);
        setLocationRelativeTo(owner);
    }
}
