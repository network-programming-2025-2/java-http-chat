import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {

    private static final int PORT = 9999;
    private static final Map<String, ClientHandler> clients =
            Collections.synchronizedMap(new HashMap<>());

    private enum GameType { NONE, NUMBER_BASEBALL, WORD_CHAIN }

    private static GameType currentGameType = GameType.NONE;

    private static NumberBaseballGame currentBaseballGame;

    private static List<String> gameParticipants = new ArrayList<>();
    private static int currentPlayerIndex = -1;
    private static StringBuilder gameHistory = new StringBuilder();

    // 끝말잇기용
    private static String lastWord = null;
    private static Set<String> usedWords = new HashSet<>();

    // 타이머
    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> turnTimeoutTask = null;
    private static ScheduledFuture<?> timerBroadcastTask = null;

    private static String encodeHistory(String s) {
        return s.replace("\n", "\\n");
    }

    public static void main(String[] args) {
        System.out.println("[서버] 시작. 포트: " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new Thread(new ClientHandler(serverSocket.accept())).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scheduler.shutdownNow();
        }
    }

    // 게임 생성
    public static synchronized void createGame(String gameType, String creator) {
        if (currentGameType != GameType.NONE) {
            sendToOne(creator, "GAME_INFO::이미 진행 중인 게임이 있습니다.");
            return;
        }

        gameParticipants.clear();
        gameHistory.setLength(0);
        gameParticipants.addAll(clients.keySet());

        if (gameParticipants.isEmpty()) {
            sendToOne(creator, "GAME_INFO::참여할 유저가 없습니다.");
            return;
        }

        Collections.shuffle(gameParticipants);
        currentPlayerIndex = -1;

        if ("NUMBER_BASEBALL".equals(gameType)) {
            currentGameType = GameType.NUMBER_BASEBALL;
            currentBaseballGame = new NumberBaseballGame();
            lastWord = null;
            usedWords.clear();

            gameHistory.append("--- 숫자 야구 게임 시작 ---\n");
            broadcast("GAME_START_SUCCESS::NUMBER_BASEBALL");
            broadcast("GAME_BOARD_UPDATE::" + encodeHistory(gameHistory.toString()));
            nextTurn();

        } else if ("WORD_CHAIN".equals(gameType)) {
            currentGameType = GameType.WORD_CHAIN;
            currentBaseballGame = null;
            lastWord = null;
            usedWords.clear();

            gameHistory.append("--- 끝말잇기 게임 시작 ---\n");
            broadcast("GAME_START_SUCCESS::WORD_CHAIN");
            broadcast("GAME_BOARD_UPDATE::" + encodeHistory(gameHistory.toString()));
            nextTurn();

        } else {
            sendToOne(creator, "GAME_INFO::알 수 없는 게임 타입입니다.");
        }
    }

    // 공통 액션 처리
    public static synchronized void handleGameAction(String player, String action) {
        if (currentGameType == GameType.NONE || gameParticipants.isEmpty()) return;

        if (currentPlayerIndex < 0 ||
                !gameParticipants.get(currentPlayerIndex).equals(player)) {
            sendToOne(player, "GAME_INFO::당신의 턴이 아닙니다.");
            return;
        }

        if (currentGameType == GameType.NUMBER_BASEBALL) {
            handleBaseballAction(player, action);
        } else if (currentGameType == GameType.WORD_CHAIN) {
            handleWordChainAction(player, action);
        }
    }

    // 숫자야구 처리
    private static void handleBaseballAction(String player, String guess) {
        if (currentBaseballGame == null) return;

        String result = currentBaseballGame.checkGuess(guess);
        gameHistory.append(player).append(" -> ").append(guess)
                .append(" : ").append(result).append("\n");
        broadcast("GAME_BOARD_UPDATE::" + encodeHistory(gameHistory.toString()));

        if (result.contains("4S")) {
            broadcast("GAME_INFO::" + player + "님이 정답(" +
                    currentBaseballGame.getAnswerString() + ")을 맞췄습니다!");
            broadcast("GAME_END::" + player);
            currentBaseballGame = null;
            currentGameType = GameType.NONE;
            cancelTurnTimeout();
        } else {
            nextTurn();
        }
    }

    // 끝말잇기 처리 (10초 제한)
    private static void handleWordChainAction(String player, String word) {
        word = word.trim();

        if (word.isEmpty()) {
            sendToOne(player, "GAME_INFO::단어를 입력하세요.");
            return;
        }

        if (!word.matches("^[가-힣]+$")) {
            sendToOne(player, "GAME_INFO::한글로만 된 단어를 입력하세요.");
            return;
        }

        if (usedWords.contains(word)) {
            sendToOne(player, "GAME_INFO::이미 사용된 단어입니다.");
            return;
        }

        if (lastWord != null) {
            char lastCh = lastWord.charAt(lastWord.length() - 1);
            char firstCh = word.charAt(0);
            if (lastCh != firstCh) {
                sendToOne(player, "GAME_INFO::'" + lastCh + "' 로 시작하는 단어를 입력해야 합니다.");
                return;
            }
        }

        usedWords.add(word);
        lastWord = word;

        gameHistory.append(player).append(" -> ").append(word).append("\n");
        broadcast("GAME_BOARD_UPDATE::" + encodeHistory(gameHistory.toString()));

        nextTurn();
    }

    // 정답 공개 (숫자야구)
    public static synchronized void revealAnswer(String requester) {
        if (currentGameType != GameType.NUMBER_BASEBALL || currentBaseballGame == null) {
            sendToOne(requester, "GAME_INFO::숫자 야구 진행 중일 때만 정답 확인이 가능합니다.");
            return;
        }
        String answer = currentBaseballGame.getAnswerString();
        broadcast("GAME_REVEAL::" + answer + "::" + requester);
        broadcast("GAME_END::정답 공개");
        currentBaseballGame = null;
        currentGameType = GameType.NONE;
        cancelTurnTimeout();
    }

    // 타이머 관련
    private static void scheduleTurnTimeout(String player) {
        cancelTurnTimeout();

        final int[] timeLeft = {10};

        timerBroadcastTask = scheduler.scheduleAtFixedRate(() -> {
            if (timeLeft[0] <= 0) return;
            broadcast("GAME_TIMER::" + timeLeft[0]);
            timeLeft[0]--;
        }, 0, 1, TimeUnit.SECONDS);

        turnTimeoutTask = scheduler.schedule(() -> {
            handleTurnTimeout(player);
            if (timerBroadcastTask != null) timerBroadcastTask.cancel(true);
        }, 10, TimeUnit.SECONDS);
    }

    private static void cancelTurnTimeout() {
        if (turnTimeoutTask != null && !turnTimeoutTask.isDone())
            turnTimeoutTask.cancel(true);
        if (timerBroadcastTask != null && !timerBroadcastTask.isDone())
            timerBroadcastTask.cancel(true);
    }

    private static synchronized void handleTurnTimeout(String player) {
        if (currentGameType != GameType.WORD_CHAIN) return;
        if (gameParticipants.isEmpty()) return;
        if (currentPlayerIndex < 0 ||
                !gameParticipants.get(currentPlayerIndex).equals(player)) return;

        gameHistory.append(player).append(" -> (시간 초과)\n");
        broadcast("GAME_BOARD_UPDATE::" + encodeHistory(gameHistory.toString()));

        broadcast("GAME_INFO::" + player + "님이 10초 안에 단어를 입력하지 않아 GAME OVER!");
        broadcast("GAME_END::시간초과(" + player + ")");

        currentGameType = GameType.NONE;
        currentBaseballGame = null;
        lastWord = null;
        usedWords.clear();
        cancelTurnTimeout();
    }

    // 턴 처리
    private static void nextTurn() {
        if (gameParticipants.isEmpty()) {
            broadcast("GAME_END::모두 나감");
            currentGameType = GameType.NONE;
            currentBaseballGame = null;
            lastWord = null;
            usedWords.clear();
            cancelTurnTimeout();
            return;
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % gameParticipants.size();
        String nextPlayer = gameParticipants.get(currentPlayerIndex);

        String infoMsg;
        if (currentGameType == GameType.NUMBER_BASEBALL) {
            infoMsg = nextPlayer + "님의 차례입니다. (서로 다른 4자리 숫자)";
            broadcast("GAME_INFO::" + infoMsg);
            cancelTurnTimeout();
        } else if (currentGameType == GameType.WORD_CHAIN) {
            if (lastWord == null) {
                infoMsg = nextPlayer + "님의 차례입니다. (아무 단어나 입력하세요)";
            } else {
                char lastCh = lastWord.charAt(lastWord.length() - 1);
                infoMsg = nextPlayer + "님의 차례입니다. ('" + lastCh + "' 로 시작하는 단어)";
            }
            broadcast("GAME_INFO::" + infoMsg);
            scheduleTurnTimeout(nextPlayer);
        } else {
            infoMsg = "현재 진행 중인 게임이 없습니다.";
            broadcast("GAME_INFO::" + infoMsg);
            cancelTurnTimeout();
        }
    }

    public static synchronized void playerLeft(String nickname) {
        clients.remove(nickname);
        broadcast("EXIT_USER::" + nickname);
        System.out.println("[서버] " + nickname + " 님이 나갔습니다.");

        if (currentGameType == GameType.NONE) return;
        if (!gameParticipants.contains(nickname)) return;

        boolean wasCurrentPlayer =
                (currentPlayerIndex >= 0 &&
                        currentPlayerIndex < gameParticipants.size() &&
                        gameParticipants.get(currentPlayerIndex).equals(nickname));

        gameParticipants.remove(nickname);
        broadcast("GAME_INFO::" + nickname + "님이 게임을 떠났습니다.");

        if (gameParticipants.isEmpty()) {
            broadcast("GAME_END::모두 나감");
            currentGameType = GameType.NONE;
            currentBaseballGame = null;
            lastWord = null;
            usedWords.clear();
            cancelTurnTimeout();
        } else if (wasCurrentPlayer) {
            currentPlayerIndex %= gameParticipants.size();
            nextTurn();
        }
    }

    public static void broadcast(String message) {
        System.out.println("[서버 방송] " + message);
        clients.values().forEach(c -> c.sendMessage(message));
    }

    public static void sendToOne(String nickname, String message) {
        ClientHandler target = clients.get(nickname);
        if (target != null) target.sendMessage(message);
    }

    public static void sendPrivateMessage(String sender, String target, String msg) {
        String fullMessage = "PRIVATE_MSG::" + sender + "::" + target + "::" + msg;
        sendToOne(target, fullMessage);
        sendToOne(sender, fullMessage);
    }

    public static boolean isNicknameTaken(String nickname) {
        return clients.containsKey(nickname);
    }

    public static void addClient(String nickname, ClientHandler handler) {
        clients.put(nickname, handler);
    }

    public static String getUserList() {
        return String.join(",", clients.keySet());
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;

    public ClientHandler(Socket socket) { this.socket = socket; }

    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            String request;
            if ((request = in.readLine()) != null &&
                    request.startsWith("LOGIN::")) {

                String potentialNickname = request.substring(7);
                if (GameServer.isNicknameTaken(potentialNickname)) {
                    sendMessage("LOGIN_FAIL::이미 사용 중인 닉네임입니다.");
                    return;
                }

                this.nickname = potentialNickname;
                GameServer.addClient(nickname, this);
                sendMessage("LOGIN_SUCCESS::");
                GameServer.sendToOne(nickname,
                        "USER_LIST::" + GameServer.getUserList());
                GameServer.broadcast("NEW_USER::" + nickname);
                System.out.println("[서버] " + nickname + " 님이 접속했습니다.");

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("[서버 수신] " + nickname + ": " + message);

                    if (message.startsWith("PUBLIC_MSG::")) {
                        GameServer.broadcast("PUBLIC_MSG::" + nickname + "::" +
                                message.substring(12));
                    } else if (message.startsWith("PRIVATE_MSG::")) {
                        String[] p = message.split("::", 3);
                        GameServer.sendPrivateMessage(nickname, p[1], p[2]);
                    } else if (message.startsWith("GAME_CREATE_REQUEST::")) {
                        GameServer.createGame(message.split("::")[1], nickname);
                    } else if (message.startsWith("GAME_ACTION::")) {
                        GameServer.handleGameAction(
                                nickname, message.split("::")[1]);
                    } else if (message.startsWith("GAME_REVEAL_REQUEST::")) {
                        GameServer.revealAnswer(nickname);
                    }
                }
            }
        } catch (IOException e) {
            // 무시
        } finally {
            if (nickname != null) GameServer.playerLeft(nickname);
            try { socket.close(); } catch (IOException e) {}
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
