// MainFrame.java (사이즈 최종 조정본)

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class MainFrame extends JFrame {
	private final String nickname;


	private JPanel userListPanel;
	private JPanel userListHeaderPanel;
	private JPanel userListContentPanel;
	private JScrollPane userListScrollPane;
	private JPanel chatPanel;
	private JPanel chatHeaderPanel;
	private JPanel chatDisplayContainer;
	private JScrollPane chatScrollPane;
	private PlaceholderTextArea messageArea;
	private JButton sendButton;

	public MainFrame(String nickname) {
		this.nickname = nickname;

		setTitle("겜톡");
		// ▼▼▼ [수정] 실제 카톡과 유사한 창 크기 설정 ▼▼▼
		setSize(620, 750);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		createUserListPanel();
		add(userListPanel, BorderLayout.WEST);
		createChatPanel();
		add(chatPanel, BorderLayout.CENTER);
		addChatMessage(new ChatMessage("박동찬", "Sunshine coast", false));
		addChatMessage(
				new ChatMessage("나", "이 메시지는 내용이 매우 길어서 말풍선이 여러 줄로 표시되어야 합니다. 그래야 잘림 현상이 발생하는지 테스트할 수 있습니다.", true));
		setVisible(true);
	}
	 private void createUserListPanel() {
	        userListPanel = new JPanel(new BorderLayout());
	        userListPanel.setPreferredSize(new Dimension(220, 0));
	        userListPanel.setBackground(new Color(230, 230, 230));
	        userListPanel.setBorder(new LineBorder(new Color(210, 210, 210)));
	        userListHeaderPanel = new JPanel();
	        userListHeaderPanel.setBackground(new Color(240, 240, 240));
	        userListHeaderPanel.setPreferredSize(new Dimension(0, 70));
	        userListHeaderPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
	        userListHeaderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
	        userListHeaderPanel.add(new JLabel("My Profile"));
	        userListPanel.add(userListHeaderPanel, BorderLayout.NORTH);

	        // 2-2. 왼쪽 사용자 목록 (초기에는 비어있음)
	        userListContentPanel = new JPanel();
	        userListContentPanel.setLayout(new BoxLayout(userListContentPanel, BoxLayout.Y_AXIS));
	        userListContentPanel.setBackground(Color.WHITE);

	        // [핵심 변경] "팀원 1,2,3..."을 만들던 for 루프를 완전히 삭제합니다.

	        userListScrollPane = new JScrollPane(userListContentPanel);
	        userListScrollPane.setBorder(null);
	        userListPanel.add(userListScrollPane, BorderLayout.CENTER);
	    }
	    
	    // ▼▼▼ 이 메소드를 MainFrame 클래스 안에 새로 추가하세요. ▼▼▼
	    /**
	     * 왼쪽 사용자 목록에 새로운 사용자를 추가하는 메소드
	     * @param nickname 추가할 사용자의 닉네임
	     */
	    public void addUser(String nickname) {
	        CustomUserButton userButton = new CustomUserButton(nickname);
	        userListContentPanel.add(userButton);
	        
	        // UI를 새로고침하여 변경사항을 즉시 반영
	        userListContentPanel.revalidate();
	        userListContentPanel.repaint();
	    }
	    
	    public void clearUserList() {
	        userListContentPanel.removeAll(); // 모든 버튼 제거
	        
	        // UI를 새로고침하여 변경사항을 즉시 반영
	        userListContentPanel.revalidate();
	        userListContentPanel.repaint();
	    }
	private void createChatHeader() {
		JPanel chatHeader = new JPanel(new BorderLayout());

	}

	private void createChatPanel() {
		chatPanel = new JPanel(new BorderLayout());
		chatPanel.setBackground(Color.WHITE);

		// --- 1. 헤더 부분 (이전과 동일) ---
		chatHeaderPanel = new JPanel(new BorderLayout());
		chatHeaderPanel.setBackground(new Color(245, 245, 245));
		chatHeaderPanel.setPreferredSize(new Dimension(0, 70));
		chatHeaderPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
		JPanel leftHeaderPanel = new JPanel(new BorderLayout());
		leftHeaderPanel.setOpaque(false);
		leftHeaderPanel.setBorder(new EmptyBorder(0, 20, 0, 0));
		JPanel nameAndCountPanel = new JPanel();
		nameAndCountPanel.setOpaque(false);
		nameAndCountPanel.setLayout(new BoxLayout(nameAndCountPanel, BoxLayout.Y_AXIS));
		nameAndCountPanel.add(Box.createVerticalGlue());
		JLabel chatPartnerName = new JLabel("박동찬");
		chatPartnerName.setFont(new Font("맑은 고딕", Font.BOLD, 14));
		JLabel memberCount = new JLabel("2");
		memberCount.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
		memberCount.setForeground(Color.GRAY);
		nameAndCountPanel.add(chatPartnerName);
		nameAndCountPanel.add(Box.createRigidArea(new Dimension(0, 4)));
		nameAndCountPanel.add(memberCount);
		nameAndCountPanel.add(Box.createVerticalGlue());
		leftHeaderPanel.add(nameAndCountPanel, BorderLayout.CENTER);
		JPanel rightHeaderPanel = new JPanel();
		rightHeaderPanel.setLayout(new BoxLayout(rightHeaderPanel, BoxLayout.X_AXIS));
		rightHeaderPanel.setOpaque(false);
		rightHeaderPanel.setBorder(new EmptyBorder(0, 0, 0, 15));
		JButton searchButton = new JButton("🔍");
		JButton callButton = new JButton("📞");
		JButton videoCallButton = new JButton("📺");
		JButton menuButton = new JButton("≡");
		JButton[] iconButtons = { searchButton, callButton, videoCallButton, menuButton };
		for (JButton button : iconButtons) {
			button.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
			button.setOpaque(false);
			button.setContentAreaFilled(false);
			button.setBorderPainted(false);
			button.setFocusPainted(false);
			button.setCursor(new Cursor(Cursor.HAND_CURSOR));
			rightHeaderPanel.add(button);
			if (button != menuButton) {
				rightHeaderPanel.add(Box.createRigidArea(new Dimension(15, 0)));
			}
		}
		chatHeaderPanel.add(leftHeaderPanel, BorderLayout.CENTER);
		chatHeaderPanel.add(rightHeaderPanel, BorderLayout.EAST);
		chatPanel.add(chatHeaderPanel, BorderLayout.NORTH);

		// --- 2. 채팅 내용 표시 부분 (이전과 동일) ---
		chatDisplayContainer = new JPanel();
		chatDisplayContainer.setLayout(new BoxLayout(chatDisplayContainer, BoxLayout.Y_AXIS));
		chatDisplayContainer.setBackground(new Color(172, 184, 196));
		chatScrollPane = new JScrollPane(chatDisplayContainer);
		chatScrollPane.setBorder(null);
		chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		chatPanel.add(chatScrollPane, BorderLayout.CENTER);

		// --- ▼▼▼ 메시지 입력부 사이즈 초극소화 조정 ▼▼▼ ---
		JPanel bottomOuterPanel = new JPanel(new BorderLayout());
		bottomOuterPanel.setBackground(Color.WHITE);
		bottomOuterPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

		RoundedPanel bottomInnerPanel = new RoundedPanel(new BorderLayout(), 15);
		bottomInnerPanel.setBackground(Color.WHITE);
		bottomInnerPanel.setBorder(new LineBorder(new Color(220, 220, 220)));

		// 3-1. 메시지 입력창
		messageArea = new PlaceholderTextArea("메시지 입력");
		messageArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
		messageArea.setBorder(new EmptyBorder(8, 10, 8, 10));
		JScrollPane messageScrollPane = new JScrollPane(messageArea);
		messageScrollPane.setBorder(null);
		messageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		// 3-2. 하단 컨트롤 패널 (BoxLayout 사용)
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
		controlPanel.setOpaque(false);
		// ▼▼▼ [최종 수정] 패널 내부 좌우 여백 거의 제거 ▼▼▼
		controlPanel.setBorder(new EmptyBorder(3, 4, 3, 4));

		// 3-2-1. 아이콘 툴바
		String[] iconTexts = { "😊", "📅", "💬", "📄", "🖼️", "🇹", "🎮" };
		for (String text : iconTexts) {
			JButton iconButton = new JButton(text);
			// ▼▼▼ [최종 수정] 아이콘 폰트 크기 극소화 및 버튼 여백 제거 ▼▼▼
			iconButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
			iconButton.setMargin(new Insets(0, 0, 0, 0));
			iconButton.setOpaque(false);
			iconButton.setContentAreaFilled(false);
			iconButton.setBorderPainted(false);
			iconButton.setFocusPainted(false);
			iconButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
			controlPanel.add(iconButton);
			// ▼▼▼ [최종 수정] 아이콘 사이 간격 극소화 ▼▼▼
			controlPanel.add(Box.createRigidArea(new Dimension(3, 0)));
		}

		// 3-2-2. 빈 공간 (스프링)
		controlPanel.add(Box.createHorizontalGlue());

		// 3-2-3. 투명도 슬라이더
		JSlider transparencySlider = new JSlider(0, 100);
		transparencySlider.setOpaque(false);
		// ▼▼▼ [최종 수정] 슬라이더 크기 극소화 ▼▼▼
		transparencySlider.setPreferredSize(new Dimension(30, 20));
		transparencySlider.setMaximumSize(new Dimension(30, 20));
		controlPanel.add(transparencySlider);
		controlPanel.add(Box.createRigidArea(new Dimension(5, 0)));

		// 3-2-4. 전송 버튼
		sendButton = new JButton("전송");
		// ▼▼▼ [최종 수정] 전송 버튼 폰트 및 크기 극소화 ▼▼▼
		sendButton.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
		sendButton.setPreferredSize(new Dimension(40, 25));
		sendButton.setMaximumSize(new Dimension(40, 25));
		sendButton.setFocusPainted(false);
		sendButton.setBorder(null);
		sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		updateSendButtonState(false);
		controlPanel.add(sendButton);

		// 3-3. 최종 조립
		bottomInnerPanel.add(messageScrollPane, BorderLayout.CENTER);
		bottomInnerPanel.add(controlPanel, BorderLayout.SOUTH);
		bottomOuterPanel.add(bottomInnerPanel, BorderLayout.CENTER);
		chatPanel.add(bottomOuterPanel, BorderLayout.SOUTH);

		// 3-4. 이벤트 리스너 추가 (이전과 동일)
		messageArea.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update();
			}

			public void removeUpdate(DocumentEvent e) {
				update();
			}

			public void insertUpdate(DocumentEvent e) {
				update();
			}

			public void update() {
				updateSendButtonState(!messageArea.getRealText().trim().isEmpty());
			}
		});
		sendButton.addActionListener(e -> sendMessage());
	}

	private void addChatMessage(ChatMessage chatMessage) {
		JPanel wrapperPanel = new JPanel(new BorderLayout());
		wrapperPanel.setOpaque(false);
		ChatBubblePanel bubble = new ChatBubblePanel(chatMessage.getMessage(), chatMessage.isMine());
		JLabel timestampLabel = new JLabel(chatMessage.getTimestamp());
		// ▼▼▼ [수정] 시간 표시 폰트 크기 조정 ▼▼▼
		timestampLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 9));
		timestampLabel.setForeground(Color.GRAY);
		timestampLabel.setBorder(new EmptyBorder(0, 5, 5, 5));
		if (chatMessage.isMine()) {
			JPanel myMessagePanel = new JPanel();
			myMessagePanel.setOpaque(false);
			myMessagePanel.setLayout(new BoxLayout(myMessagePanel, BoxLayout.Y_AXIS));
			bubble.setAlignmentX(Component.RIGHT_ALIGNMENT);
			timestampLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
			myMessagePanel.add(bubble);
			myMessagePanel.add(timestampLabel);
			JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); // 간격 제거
			flowPanel.setOpaque(false);
			flowPanel.add(myMessagePanel);
			wrapperPanel.add(flowPanel, BorderLayout.CENTER);
			wrapperPanel.setBorder(new EmptyBorder(5, 50, 5, 10));
		} else {
			JPanel container = new JPanel();
			container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
			container.setOpaque(false);
			container.setAlignmentX(Component.LEFT_ALIGNMENT);
			JLabel senderLabel = new JLabel(chatMessage.getSender());
			// ▼▼▼ [수정] 상대방 이름 폰트 크기 조정 ▼▼▼
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
		SwingUtilities.invokeLater(() -> {
			JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
			vertical.setValue(vertical.getMaximum());
		});
	}

	// --- sendMessage, updateSendButtonState, main 메소드는 이전과 동일 ---
	private void sendMessage() { /* 이전과 동일 */
		String message = messageArea.getRealText();
		if (!message.trim().isEmpty()) {
			addChatMessage(new ChatMessage("나", message, true));
			messageArea.setText("");
			messageArea.requestFocusInWindow();
		}
	}

	private void updateSendButtonState(boolean active) { /* 이전과 동일 */ 
        if (active) {
            sendButton.setBackground(new Color(255, 235, 51));
            sendButton.setEnabled(true);
        } else {
            sendButton.setBackground(new Color(240, 240, 240));
            sendButton.setEnabled(false);
        }
    }

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new MainFrame("테스트"));
	}
}