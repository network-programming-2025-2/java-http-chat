import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 15x15 오목 게임 엔진
 * 돌 색: 0=빈칸, 1=흑, 2=백
 */
public class OmokGame {
    public static final int SIZE = 15;

    private final int[][] board = new int[SIZE][SIZE];
    private final Map<String, Integer> playerColors = new HashMap<>(); // 닉네임 -> 색(1/2)
    private final List<String> players = new ArrayList<>();

    public OmokGame(List<String> participants) {
        // 참가자 복사
        for (String p : participants) {
            if (!players.contains(p)) {
                players.add(p);
            }
        }
        // 2명 기준으로 색 배정: 첫번째=흑, 두번째=백
        if (players.size() >= 1) {
            playerColors.put(players.get(0), 1); // 흑
        }
        if (players.size() >= 2) {
            playerColors.put(players.get(1), 2); // 백
        }
    }

    /** 플레이어 색상(1: 흑, 2: 백, 0: 없음) */
    public int getColorOf(String player) {
        return playerColors.getOrDefault(player, 0);
    }

    /** 현재 판 상태 반환 (디버깅용) */
    public int[][] getBoard() {
        return board;
    }

    /**
     * 돌 두기
     * @param player 닉네임
     * @param x 0~14 (가로)
     * @param y 0~14 (세로)
     * @return "NO_COLOR", "OUT_OF_RANGE", "OCCUPIED", "OK", "WIN"
     */
    public String placeStone(String player, int x, int y) {
        int color = getColorOf(player);
        if (color == 0) {
            return "NO_COLOR"; // 관전자 등
        }

        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) {
            return "OUT_OF_RANGE";
        }

        if (board[y][x] != 0) {
            return "OCCUPIED";
        }

        board[y][x] = color;

        if (isFiveInRow(x, y, color)) {
            return "WIN";
        }
        return "OK";
    }

    /** 마지막 둔 자리 기준으로 5목 체크 */
    private boolean isFiveInRow(int x, int y, int color) {
        // 4방향: 가로, 세로, 대각(↘), 대각(↗)
        int[][] dirs = {
                {1, 0}, // 가로
                {0, 1}, // 세로
                {1, 1}, // ↘
                {1, -1} // ↗
        };

        for (int[] d : dirs) {
            int count = 1;

            // 한쪽 방향
            count += countDirection(x, y, d[0], d[1], color);
            // 반대 방향
            count += countDirection(x, y, -d[0], -d[1], color);

            if (count >= 5) return true;
        }
        return false;
    }

    private int countDirection(int x, int y, int dx, int dy, int color) {
        int cnt = 0;
        int nx = x + dx;
        int ny = y + dy;

        while (nx >= 0 && nx < SIZE && ny >= 0 && ny < SIZE
                && board[ny][nx] == color) {
            cnt++;
            nx += dx;
            ny += dy;
        }
        return cnt;
    }
}
