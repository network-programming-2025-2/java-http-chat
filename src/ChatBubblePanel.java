// ChatBubblePanel.java

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChatBubblePanel extends JTextArea {
    private Color bubbleColor;
    private boolean isMine;

    public ChatBubblePanel(String message, boolean isMine) {
        super(message);
        this.isMine = isMine;
        this.bubbleColor = isMine ? new Color(255, 235, 51) : Color.WHITE;
        
        setEditable(false);
        setLineWrap(true);
        setWrapStyleWord(true);
        // ▼▼▼ [수정] 폰트 크기 및 여백 조정 ▼▼▼
        setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        setBorder(new EmptyBorder(8, 12, 8, 12));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(bubbleColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

        // ▼▼▼ [추가] 상대방 말풍선에만 옅은 회색 테두리 추가 ▼▼▼
        if (!isMine) {
            g2.setColor(new Color(220, 220, 220));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        }
        
        g2.dispose();
        super.paintComponent(g);
    }
}
