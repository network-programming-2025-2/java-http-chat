// Main.java (최종 수정본)

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.HashSet;
import java.util.Set;

public class Main {

    private LoginFrame loginFrame;
    private MainFrame mainFrame;
    
    // [핵심] 모든 사용자 목록을 관리할 데이터 저장소 (Set으로 중복 자동 방지)
    private Set<String> userList = new HashSet<>();

    public Main() {
        // 로그인 창을 먼저 보여줌
        loginFrame = new LoginFrame(this);
        loginFrame.setVisible(true);
    }

    // 로그인 시도 시 호출되는 메소드
    public void attemptLogin(String nickname) {
        // [핵심] 중복 닉네임 검사
        if (userList.contains(nickname)) {
            JOptionPane.showMessageDialog(loginFrame, "이미 접속 중인 닉네임입니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
        } else {
            // 로그인 성공
            userList.add(nickname); // 사용자 목록에 추가
            
            // 처음 로그인하는 경우, 채팅창을 새로 염
            if (mainFrame == null) {
                mainFrame = new MainFrame();
                mainFrame.setTitle("겜톡");
                // 모든 유저 목록을 채팅창에 반영
                updateUserListUI(); 
                mainFrame.setVisible(true);
            } else {
                // 이미 채팅창이 열려있는 경우, 새 유저만 추가
                mainFrame.addUser(nickname);
            }
            
            // 로그인 창 숨기기 (나중에 다른 아이디로 또 로그인할 수 있도록)
            loginFrame.setVisible(false);
            // 새 로그인 창을 다시 띄워서 다음 사람 로그인을 준비
            loginFrame = new LoginFrame(this);
            loginFrame.setVisible(true);
        }
    }
    
    // 채팅창의 UI를 최신 사용자 목록으로 업데이트하는 메소드
    private void updateUserListUI() {
        if (mainFrame != null) {
            mainFrame.clearUserList(); // 기존 목록을 모두 지우고
            for (String user : userList) { // 최신 목록으로 다시 채움
                mainFrame.addUser(user);
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new Main());
    }
}
