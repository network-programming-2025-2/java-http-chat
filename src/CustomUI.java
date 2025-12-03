import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class CustomUserButton extends JButton {
    private String nickname;
    private JLabel notificationLabel;

    public CustomUserButton(String nickname, Main mainApp) {
        super();
        this.nickname = nickname;
        setLayout(new BorderLayout());
        JLabel nameLabel = new JLabel(nickname);
        nameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        notificationLabel = new JLabel();
        notificationLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        notificationLabel.setForeground(Color.RED);
        add(nameLabel, BorderLayout.CENTER);
        add(notificationLabel, BorderLayout.EAST);
        setHorizontalAlignment(SwingConstants.LEFT);
        setPreferredSize(new Dimension(0, 50));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 15, 10, 15));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(true);
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { setBackground(new Color(245, 245, 245)); }
            public void mouseExited(MouseEvent e) { setBackground(Color.WHITE); }
        });
        addActionListener(e -> {
            mainApp.startPrivateChat(this.nickname);
            setNotification(false);
        });
    }

    public String getNickname() { return nickname; }
    public void setNotification(boolean hasNew) { notificationLabel.setText(hasNew ? " (N)" : ""); }
}

class PlaceholderTextArea extends JTextArea {
    private String placeholder;
    public PlaceholderTextArea(String placeholder) { this.placeholder = placeholder; }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (placeholder == null || placeholder.isEmpty() || !getText().isEmpty()) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getDisabledTextColor());
        g2.drawString(placeholder, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);
    }
    public String getRealText() { return super.getText(); }
}

class RoundedPanel extends JPanel {
    private int cornerRadius;
    public RoundedPanel(LayoutManager layout, int radius) { super(layout); this.cornerRadius = radius; }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension arcs = new Dimension(cornerRadius, cornerRadius);
        int width = getWidth(); int height = getHeight();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);
        g2.setColor(getForeground());
        g2.drawRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);
    }
}
