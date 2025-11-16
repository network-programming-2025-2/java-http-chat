import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final int PORT = 9999;
    private ServerSocket serverSocket;

    private final ConcurrentHashMap<String, ClientHandler> users = new ConcurrentHashMap<>();

    // 포트 열기 및 서버 run
    public Server() {
        try {
            this.serverSocket = new ServerSocket(PORT);
            System.out.println("Server is Running: " + PORT);

            serverRun();
        } catch(IOException e) {
            System.err.println("서버 생성 실패");
            e.printStackTrace();

            System.exit(1);
        }
    }

    // 서버 run
    public void serverRun() {
        System.out.println("유저 연결 대기");

        while(true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("새로운 유저 접속: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, this);

                new Thread(handler).start();

            } catch(IOException e) {
                // 단순히 유저 한 명의 접속 오류이기에 그냥 에러 메시지만 출력(다른 액션 금지)
                System.err.println("유저 연결 중 오류 발생: " + e.getMessage());
            }
        }
    }

    // 고유한 식별자(닉네임)의 중복을 검사하고 유저를 리스트에 등록하는 메서드
    public boolean tryRegisterUser(String nickname, ClientHandler handler) {
        if(users.containsKey(nickname)) {
            return false;
        } else {
            users.put(nickname, handler);

            System.out.println(users);
            return true;
        }
    }

    // 접속중인 유저 수 반환 메서드
    public int getUsersCount() {
        return users.size();
    }

    public static void main(String[] args) {
        new Server();
    }
}
