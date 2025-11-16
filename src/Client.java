import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private final String SERVER_IP = "127.0.0.1";
    private final int PORT = 9999;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private String nickname;
    private MainFrame mainFrame;


    private final LoginFrame loginFrame;

    public Client() {
        this.loginFrame = new LoginFrame(this);

        loginFrame.setVisible(true);
    }

    private void connectToServer(String serverIp, int port) throws IOException {
        this.socket = new Socket(serverIp, port);

        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void attemptLogin(String nickname) {
        new Thread(() -> {
            try {
                connectToServer(this.SERVER_IP, this.PORT);

                this.out.println(nickname);

                String response = in.readLine();

                if("LOGIN_SUCCESS".equalsIgnoreCase(response)) {
                    SwingUtilities.invokeLater(() -> {
                        this.nickname = nickname;
                        //loginFrame.setVisible(false);
                        this.mainFrame = new MainFrame(nickname);

                    });
                } else{
                    JOptionPane.showMessageDialog(loginFrame, response, "로그인 실패", JOptionPane.ERROR_MESSAGE);
                }
            } catch(IOException e) {
                System.err.println(e.getMessage());

                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(loginFrame, "서버에 연결할 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    public static void main(String[] args) {
        new Client();
    }
}
