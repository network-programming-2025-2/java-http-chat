import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private Main mainApp;

    public Client(Main mainApp) {
        this.mainApp = mainApp;
    }

    public boolean connect(String nickname) {
        try {
            socket = new Socket("localhost", 9999);
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("LOGIN::" + nickname);
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        mainApp.handleServerMessage(serverMessage);
                    }
                } catch (IOException e) {
                    mainApp.handleServerMessage("SERVER_DOWN::");
                }
            }).start();
            return true;
        } catch (IOException e) {
            mainApp.loginFailed("서버에 연결할 수 없습니다.");
            return false;
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
