import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChatBubblePanel extends JPanel {
    private boolean isMine;

    public ChatBubblePanel(String message, boolean isMine) {
        this.isMine = isMine;
        setLayout(new BorderLayout());
        setOpaque(false);
        JLabel messageLabel = new JLabel("<html><body style='width: 150px;'>" + message.replaceAll("\n", "<br>") + "</body></html>");
        messageLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        messageLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        messageLabel.setForeground(Color.BLACK);
        add(messageLabel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isMine ? new Color(255, 235, 51) : Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        g2.dispose();
        super.paintComponent(g);
    }
}
