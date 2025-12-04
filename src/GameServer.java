import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {

    private static final int PORT = 9999;
    private static final Map<String, ClientHandler> clients =
            Collections.synchronizedMap(new HashMap<>());

    private enum GameType { NONE, NUMBER_BASEBALL, WORD_CHAIN, UPDOWN, OMOK }
    private static GameType currentGameType = GameType.NONE;

    // 공통
    private static List<String> gameParticipants = new ArrayList<>();
    private static int currentPlayerIndex = -1;
    private static StringBuilder gameHistory = new StringBuilder();

    // 숫자야구
    private static NumberBaseballGame currentBaseballGame;

    // 끝말잇기
    private static String lastWord = null;
    private static Set<String> usedWords = new HashSet<>();

    // 타이머 (끝말잇기용)
    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> turnTimeoutTask = null;
    private static ScheduledFuture<?> timerBroadcastTask = null;

    // 업다운
    private static int upDownAnswer = -1;
    private static boolean upDownInProgress = false;

    // 오목
    private static final int OMOK_SIZE = 15;
    private static int[][] omokBoard = new int[OMOK_SIZE][OMOK_SIZE];
    private static String omokBlackPlayer = null;
    private static String omokWhitePlayer = null;
    private static boolean omokBlackTurn = true;

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

    // ==========================
    // 게임 생성
    // ==========================
    public static synchronized void createGame(String gameType, String creator) {
        if (currentGameType != GameType.NONE) {
            sendToOne(creator, "GAME_INFO::이미 진행 중인 게임이 있습니다.");
            return;
        }

        if (clients.isEmpty()) {
            sendToOne(creator, "GAME_INFO::접속 중인 사용자가 없습니다.");
            return;
        }

        gameParticipants.clear();
        gameParticipants.addAll(clients.keySet());
        gameHistory.setLength(0);
        currentPlayerIndex = -1;

        cancelTurnTimeout();
        lastWord = null;
        usedWords.clear();
        currentBaseballGame = null;
        upDownInProgress = false;
        omokBlackPlayer = null;
        omokWhitePlayer = null;

        switch (gameType) {
            case "NUMBER_BASEBALL":
                currentGameType = GameType.NUMBER_BASEBALL;
                currentBaseballGame = new NumberBaseballGame();
                Collections.shuffle(gameParticipants);
                gameHistory.append("--- 숫자 야구 게임 시작 ---\n");
                broadcast("GAME_START_SUCCESS::NUMBER_BASEBALL");
                broadcast("GAME_BOARD_UPDATE::" + encodeHistory(gameHistory.toString()));
                nextTurn(); // 턴 게임
                break;

            case "WORD_CHAIN":
                currentGameType = GameType.WORD_CHAIN;
                Collections.shuffle(gameParticipants);
                gameHistory.append("--- 끝말잇기 게임 시작 ---\n");
                broadcast("GAME_START_SUCCESS::WORD_CHAIN");
                broadcast("GAME_BOARD_UPDATE::" + encodeHistory(gameHistory.toString()));
                nextTurn(); // 턴 게임 + 타이머
                break;

            case "UPDOWN":
                currentGameType = GameType.UPDOWN;
                upDownAnswer = new Random().nextInt(100) + 1;
                upDownInProgress = true;
                gameHistory.append("--- 업다운 게임 시작 (1~100) ---\n");
                broadcast("GAME_START_SUCCESS::UPDOWN");
                broadcast("GAME_BOARD_UPDATE::" + encodeHistory(gameHistory.toString()));
                broadcast("GAME_INFO::업다운 게임이 시작되었습니다. 1~100 사이의 정수를 입력하세요.");
                break;

            case "OMOK":
                if (clients.size() < 2) {
                    sendToOne(creator, "GAME_INFO::오목은 최소 2명이 필요합니다.");
                    return;
                }
                currentGameType = GameType.OMOK;

                gameParticipants.clear();
                gameParticipants.addAll(clients.keySet());
                Collections.shuffle(gameParticipants);
                omokBlackPlayer = gameParticipants.get(0);
                omokWhitePlayer = gameParticipants.get(1);
                omokBlackTurn = true;

                for (int r = 0; r < OMOK_SIZE; r++) {
                    Arrays.fill(omokBoard[r], 0);
                }

                gameHistory.append("--- 오목 게임 시작 ---\n")
                        .append("흑: ").append(omokBlackPlayer)
                        .append(", 백: ").append(omokWhitePlayer).append("\n");

                broadcast("GAME_START_SUCCESS::OMOK");
                broadcast("GAME_BOARD_UPDATE::" + encodeHistory(gameHistory.toString()));
                broadcast("GAME_INFO::오목 게임 시작! 흑: " + omokBlackPlayer
                        + ", 백: " + omokWhitePlayer + " / 흑부터 시작합니다.");
                sendToOne(omokBlackPlayer, "OMOK_ROLE::BLACK");
                sendToOne(omokWhitePlayer, "OMOK_ROLE::WHITE");
                break;

            default:
                sendToOne(creator, "GAME_INFO::알 수 없는 게임 타입입니다.");
        }
    }

    // ==========================
    // 공통 액션 처리 (텍스트형 게임)
    // ==========================
    public static synchronized void handleGameAction(String player, String action) {
        if (currentGameType == GameType.NONE) return;

        switch (currentGameType) {
            case UPDOWN:
                handleUpDownAction(player, action);
                break;

            case NUMBER_BASEBALL:
                if (!isPlayersTurn(player)) {
                    sendToOne(player, "GAME_INFO::당신의 턴이 아닙니다.");
                    return;
                }
                handleBaseballAction(player, action);
                break;

            case WORD_CHAIN:
                if (!isPlayersTurn(player)) {
                    sendToOne(player, "GAME_INFO::당신의 턴이 아닙니다.");
                    return;
                }
                handleWordChainAction(player, action);
                break;

            default:
                // OMOK은 별도 프로토콜(OMOK_MOVE) 사용
                break;
        }
    }

    private static boolean isPlayersTurn(String player) {
        if (gameParticipants.isEmpty() ||
                currentPlayerIndex < 0 ||
                currentPlayerIndex >= gameParticipants.size()) {
            return false;
        }
        return gameParticipants.get(currentPlayerIndex).equals(player);
    }

    // ==========================
    // 숫자야구 처리
    // ==========================
    private static void handleBaseballAction(String player, String guess) {
        if (currentBaseballGame == null) return;

        String result = currentBaseballGame.checkGuess(guess);
        gameHistory.append(player).append(" -> ").append(guess)
                .append(" : ").append(result).append("\n");
        broadcast("GAME_BOARD_UPDATE::" + encodeHistory(gameHistory.toString()));

        if (result.contains("4S")) {
            broadcast("GAME_INFO::" + player + "님이 정답("
                    + currentBaseballGame.getAnswerString() + ")을 맞췄습니다!");
            broadcast("GAME_END::숫자야구 승자: " + player);
            currentBaseballGame = null;
            currentGameType = GameType.NONE;
            cancelTurnTimeout();
        } else {
            nextTurn();
        }
    }

    // ==========================
    // 끝말잇기 처리
    // ==========================
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

    // ==========================
    // 업다운 처리
    // ==========================
    private static void handleUpDownAction(String player, String guessText) {
        if (!upDownInProgress) {
            sendToOne(player, "GAME_INFO::현재 진행 중인 업다운 게임이 없습니다.");
            return;
        }

        int guess;
        try {
            guess = Integer.parseInt(guessText.trim());
        } catch (NumberFormatException e) {
            sendToOne(player, "GAME_INFO::정수를 입력하세요.");
            return;
        }

        if (guess < 1 || guess > 100) {
            sendToOne(player, "GAME_INFO::1~100 사이의 숫자만 입력 가능합니다.");
            return;
        }

        String result;
        if (guess < upDownAnswer) result = "UP";
        else if (guess > upDownAnswer) result = "DOWN";
        else result = "정답";

        gameHistory.append(player).append(" -> ").append(guess)
                .append(" : ").append(result).append("\n");
        broadcast("GAME_BOARD_UPDATE::" + encodeHistory(gameHistory.toString()));

        if ("정답".equals(result)) {
            broadcast("GAME_INFO::" + player + "님이 업다운 정답(" + upDownAnswer + ")을 맞췄습니다!");
            broadcast("GAME_END::UPDOWN 승자: " + player);
            upDownInProgress = false;
            currentGameType = GameType.NONE;
        }
    }

    // ==========================
    // 오목 처리
    // ==========================
    public static synchronized void handleOmokMove(String player, String coordText) {
        if (currentGameType != GameType.OMOK) {
            sendToOne(player, "GAME_INFO::현재 오목 게임이 진행 중이 아닙니다.");
            return;
        }

        if (!player.equals(omokBlackPlayer) && !player.equals(omokWhitePlayer)) {
            sendToOne(player, "GAME_INFO::관전자라 돌을 둘 수 없습니다.");
            return;
        }

        boolean isBlackTurnNow = omokBlackTurn;
        String expectedPlayer = isBlackTurnNow ? omokBlackPlayer : omokWhitePlayer;
        if (!player.equals(expectedPlayer)) {
            sendToOne(player, "GAME_INFO::당신의 턴이 아닙니다.");
            return;
        }

        String[] rc = coordText.split(",");
        if (rc.length != 2) {
            sendToOne(player, "GAME_INFO::잘못된 좌표 형식입니다.");
            return;
        }

        int row, col;
        try {
            row = Integer.parseInt(rc[0]);
            col = Integer.parseInt(rc[1]);
        } catch (NumberFormatException e) {
            sendToOne(player, "GAME_INFO::좌표는 숫자여야 합니다.");
            return;
        }

        if (row < 0 || row >= OMOK_SIZE || col < 0 || col >= OMOK_SIZE) {
            sendToOne(player, "GAME_INFO::보드 밖입니다.");
            return;
        }

        if (omokBoard[row][col] != 0) {
            sendToOne(player, "GAME_INFO::이미 돌이 놓인 자리입니다.");
            return;
        }

        int stone = isBlackTurnNow ? 1 : 2;
        omokBoard[row][col] = stone;
        String colorText = isBlackTurnNow ? "BLACK" : "WHITE";

        broadcast("OMOK_MOVE_APPLIED::" + row + "," + col + "::" + colorText + "::" + player);

        gameHistory.append(player)
                .append(" (").append(isBlackTurnNow ? "흑" : "백").append(")")
                .append(" -> (").append(row + 1).append(",").append(col + 1).append(")\n");
        broadcast("GAME_BOARD_UPDATE::" + encodeHistory(gameHistory.toString()));

        if (checkOmokWin(row, col, stone)) {
            broadcast("GAME_INFO::" + player + "님이 오목을 완성했습니다!");
            broadcast("GAME_END::OMOK 승자: " + player);
            currentGameType = GameType.NONE;
            return;
        }

        omokBlackTurn = !omokBlackTurn;
        String nextPlayer = omokBlackTurn ? omokBlackPlayer : omokWhitePlayer;
        broadcast("GAME_INFO::" + nextPlayer + "님의 차례입니다.");
    }

    private static boolean checkOmokWin(int r, int c, int stone) {
        int[][] dirs = { {1,0}, {0,1}, {1,1}, {1,-1} };

        for (int[] d : dirs) {
            int dr = d[0], dc = d[1];
            int count = 1;

            // 한쪽 방향
            int nr = r + dr;
            int nc = c + dc;
            while (nr >= 0 && nr < OMOK_SIZE && nc >= 0 && nc < OMOK_SIZE
                    && omokBoard[nr][nc] == stone) {
                count++;
                nr += dr;
                nc += dc;
            }

            // 반대 방향
            nr = r - dr;
            nc = c - dc;
            while (nr >= 0 && nr < OMOK_SIZE && nc >= 0 && nc < OMOK_SIZE
                    && omokBoard[nr][nc] == stone) {
                count++;
                nr -= dr;
                nc -= dc;
            }

            if (count >= 5) return true;
        }

        return false;
    }

    // ==========================
    // 정답 공개 (숫자야구)
    // ==========================
    public static synchronized void revealAnswer(String requester) {
        if (currentGameType != GameType.NUMBER_BASEBALL || currentBaseballGame == null) {
            sendToOne(requester, "GAME_INFO::숫자 야구 진행 중일 때만 정답 확인이 가능합니다.");
            return;
        }
        String answer = currentBaseballGame.getAnswerString();
        broadcast("GAME_REVEAL::" + answer + "::" + requester);
        broadcast("GAME_END::숫자야구 정답 공개");
        currentBaseballGame = null;
        currentGameType = GameType.NONE;
        cancelTurnTimeout();
    }

    // ==========================
    // 타이머 관련 (끝말잇기용)
    // ==========================
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

    // ==========================
    // 턴 처리 (숫자야구 / 끝말잇기 전용)
    // ==========================
    private static void nextTurn() {
        if (currentGameType != GameType.NUMBER_BASEBALL &&
                currentGameType != GameType.WORD_CHAIN) {
            cancelTurnTimeout();
            return;
        }

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
        }
    }

    // ==========================
    // 플레이어 퇴장
    // ==========================
    public static synchronized void playerLeft(String nickname) {
        clients.remove(nickname);
        broadcast("EXIT_USER::" + nickname);
        System.out.println("[서버] " + nickname + " 님이 나갔습니다.");

        // 오목 플레이어가 나가면 게임 종료
        if (currentGameType == GameType.OMOK &&
                (nickname.equals(omokBlackPlayer) || nickname.equals(omokWhitePlayer))) {
            broadcast("GAME_INFO::" + nickname + "님이 오목 게임을 떠나 게임이 종료됩니다.");
            broadcast("GAME_END::오목 중단");
            currentGameType = GameType.NONE;
        }

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
        } else if (wasCurrentPlayer &&
                (currentGameType == GameType.NUMBER_BASEBALL ||
                        currentGameType == GameType.WORD_CHAIN)) {
            currentPlayerIndex %= gameParticipants.size();
            nextTurn();
        }
    }

    // ==========================
    // 공통 유틸
    // ==========================
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

// ==========================
// 클라이언트 핸들러
// ==========================
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
                        String action = message.split("::", 2)[1];
                        GameServer.handleGameAction(nickname, action);
                    } else if (message.startsWith("GAME_REVEAL_REQUEST::")) {
                        GameServer.revealAnswer(nickname);
                    } else if (message.startsWith("OMOK_MOVE::")) {
                        String coord = message.substring("OMOK_MOVE::".length());
                        GameServer.handleOmokMove(nickname, coord);
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
