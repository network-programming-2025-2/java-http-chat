import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PrivateChatFrame extends JFrame {
    private Main mainApp;
    private String partnerNickname;

    private JPanel chatDisplayContainer;
    private JScrollPane chatScrollPane;
    private PlaceholderTextArea messageArea;
    private JButton sendButton;

    public PrivateChatFrame(Main mainApp, String partnerNickname) {
        this.mainApp = mainApp;
        this.partnerNickname = partnerNickname;

        setTitle(partnerNickname + "님과의 대화");
        setSize(500, 700);
        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createChatPanel(), BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                mainApp.closePrivateChat(partnerNickname);
            }
        });
    }

    public void addChatMessage(ChatMessage msg) {
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);

        ChatBubblePanel bubble =
                new ChatBubblePanel(msg.getMessage(), msg.isMine());

        JLabel timestampLabel = new JLabel(msg.getTimestamp());
        timestampLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 9));
        timestampLabel.setForeground(Color.GRAY);
        timestampLabel.setBorder(new EmptyBorder(0, 5, 5, 5));

        if (msg.isMine()) {
            JPanel myMessagePanel = new JPanel();
            myMessagePanel.setLayout(new BoxLayout(myMessagePanel, BoxLayout.Y_AXIS));
            myMessagePanel.setOpaque(false);

            bubble.setAlignmentX(Component.RIGHT_ALIGNMENT);
            timestampLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

            myMessagePanel.add(bubble);
            myMessagePanel.add(timestampLabel);

            JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            flowPanel.setOpaque(false);
            flowPanel.add(myMessagePanel);

            wrapperPanel.add(flowPanel, BorderLayout.CENTER);
            wrapperPanel.setBorder(new EmptyBorder(5, 50, 5, 10));
        } else {
            JPanel container = new JPanel();
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            container.setOpaque(false);
            container.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel senderLabel = new JLabel(msg.getSender());
            senderLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            senderLabel.setBorder(new EmptyBorder(0, 5, 3, 0));

            JPanel bubbleAndTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            bubbleAndTimePanel.setOpaque(false);
            bubbleAndTimePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            bubbleAndTimePanel.add(bubble);
            bubbleAndTimePanel.add(timestampLabel);

            container.add(senderLabel);
            container.add(bubbleAndTimePanel);

            wrapperPanel.add(container, BorderLayout.WEST);
            wrapperPanel.setBorder(new EmptyBorder(5, 10, 5, 50));
        }

        chatDisplayContainer.add(wrapperPanel);
        chatDisplayContainer.revalidate();
        chatDisplayContainer.repaint();

        SwingUtilities.invokeLater(() ->
                chatScrollPane.getVerticalScrollBar()
                        .setValue(chatScrollPane.getVerticalScrollBar().getMaximum()));
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(245, 245, 245));
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 0, new Color(220, 220, 220))
        );

        JLabel nameLabel = new JLabel(partnerNickname);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        nameLabel.setBorder(new EmptyBorder(0, 20, 0, 0));
        header.add(nameLabel, BorderLayout.CENTER);

        chatDisplayContainer = new JPanel();
        chatDisplayContainer.setLayout(new BoxLayout(chatDisplayContainer, BoxLayout.Y_AXIS));
        chatDisplayContainer.setBackground(new Color(172, 184, 196));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(chatDisplayContainer, BorderLayout.NORTH);

        chatScrollPane = new JScrollPane(wrapper);
        chatScrollPane.setBorder(null);
        chatScrollPane.getViewport().setBackground(new Color(172, 184, 196));
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel bottomOuter = new JPanel(new BorderLayout());
        bottomOuter.setBackground(Color.WHITE);
        bottomOuter.setBorder(new EmptyBorder(5, 10, 10, 10));

        RoundedPanel bottomInner = new RoundedPanel(new BorderLayout(), 15);
        bottomInner.setBackground(Color.WHITE);
        bottomInner.setBorder(new LineBorder(new Color(220, 220, 220)));

        messageArea = new PlaceholderTextArea("메시지 입력");
        messageArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        messageArea.setBorder(new EmptyBorder(8, 10, 8, 10));
        messageArea.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (e.isShiftDown()) {
                        messageArea.append("\n");
                    } else {
                        e.consume();
                        sendMessage();
                    }
                }
            }
        });

        JScrollPane msgScroll = new JScrollPane(messageArea);
        msgScroll.setBorder(null);

        bottomInner.add(msgScroll, BorderLayout.CENTER);
        bottomInner.add(createControlPanel(), BorderLayout.SOUTH);

        bottomOuter.add(bottomInner, BorderLayout.CENTER);

        panel.add(header, BorderLayout.NORTH);
        panel.add(chatScrollPane, BorderLayout.CENTER);
        panel.add(bottomOuter, BorderLayout.SOUTH);

        return panel;
    }

    private void sendMessage() {
        String message = messageArea.getRealText();
        if (!message.trim().isEmpty()) {
            mainApp.getClient().sendMessage(
                    "PRIVATE_MSG::" + partnerNickname + "::" + message
            );
            messageArea.setText("");
            messageArea.requestFocusInWindow();
        }
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        controlPanel.setOpaque(false);
        controlPanel.setBorder(new EmptyBorder(3, 4, 3, 4));

        controlPanel.add(Box.createHorizontalGlue());

        sendButton = new JButton("전송");
        sendButton.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        sendButton.setPreferredSize(new Dimension(40, 25));
        sendButton.setMaximumSize(new Dimension(40, 25));
        sendButton.setFocusPainted(false);
        sendButton.setBorder(null);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        updateSendButtonState(false);

        controlPanel.add(sendButton);

        messageArea.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void insertUpdate(DocumentEvent e) { update(); }
            private void update() {
                updateSendButtonState(!messageArea.getRealText().trim().isEmpty());
            }
        });

        sendButton.addActionListener(e -> sendMessage());

        return controlPanel;
    }

    private void updateSendButtonState(boolean active) {
        sendButton.setEnabled(active);
        sendButton.setBackground(active ? new Color(255, 235, 51)
                : new Color(240, 240, 240));
    }
}
