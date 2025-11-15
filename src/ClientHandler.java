import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Server server;
    private final Socket clientSocket;

    private String nickname;

    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;

        this.nickname = null;
    }

    @Override
    public void run() {
        System.out.println("ClientHandler 스레드 시작: " + clientSocket.getInetAddress());

        try {
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);

            String requestedNickname = in.readLine();

            if (requestedNickname == null) {
                System.out.println("클라이언트가 유효한 닉네임을 보내지 않았습니다.");
                return;
            }

            if(server.tryRegisterUser(requestedNickname, this)) {
                this.nickname = requestedNickname;
                System.out.println(nickname + "님 로그인 성공. 현재 접속자 수: " + server.getUsersCount());

                out.println("LOGIN_SUCCESS");
            } else {
                System.out.println("중복된 닉네임, 스레드 종료");
                out.println("이미 접속 중인 닉네임입니다.");
            }

        } catch (IOException e) {
            System.err.println(nickname + "와의 통신 오류: " + e.getMessage());
        }
    }
}
