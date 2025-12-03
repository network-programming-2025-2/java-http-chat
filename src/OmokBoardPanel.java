import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class OmokBoardPanel extends JPanel {

    private static final int SIZE = 15;           // 15x15
    private final int[][] board = new int[SIZE][SIZE]; // 0=없음, 1=흑, 2=백

    private final Main mainApp;
    private int myStone = 0;     // 1=흑, 2=백
    private boolean myTurn = false;

    public OmokBoardPanel(Main mainApp) {
        this.mainApp = mainApp;
        setBackground(new Color(240, 210, 160)); // 나무색
        setOpaque(true);

        // 적당한 기본 크기 (창이 더 작으면 자동으로 줄어듦)
        setPreferredSize(new Dimension(520, 520));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }
    // 예전 코드에서 쓰던 생성자( GamePanel 같이 받는 버전 )도 지원
    public OmokBoardPanel(Main mainApp, GamePanel gamePanel) {
        this(mainApp);   // 실제 로직은 위의 생성자에 다 맡김
    }


    // ─────────────────────  외부에서 호출하는 메서드  ─────────────────────

    /** 내 돌 색(흑/백) 설정 */
    public void setPlayerStone(boolean isBlack) {
        myStone = isBlack ? 1 : 2;
    }

    /** 지금이 내 턴인지 여부 설정 */
    public void setTurn(boolean myTurn) {
        this.myTurn = myTurn;
        repaint();
    }

    /** 서버에서 온 착수 결과 반영 */
    public void placeStone(int row, int col, boolean isBlack) {
        // row, col 은 1~15 들어온다고 가정
        int r = row - 1;
        int c = col - 1;
        if (r < 0 || r >= SIZE || c < 0 || c >= SIZE) return;

        board[r][c] = isBlack ? 1 : 2;
        repaint();
    }

    /** 게임 리셋 */
    public void resetBoard() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                board[r][c] = 0;
            }
        }
        repaint();
    }

    // ─────────────────────  클릭 처리  ─────────────────────

    private void handleClick(int mx, int my) {
        if (!myTurn || myStone == 0) return;

        BoardGeom g = computeGeometry();
        if (g.cellSize <= 0) return;

        // 판 영역 밖이면 무시
        if (mx < g.left || mx > g.left + g.boardSize) return;
        if (my < g.top  || my > g.top  + g.boardSize) return;

        // 격자 좌표로 변환 (가까운 교차점으로 반올림)
        int col = Math.round((mx - g.left) / (float) g.cellSize);
        int row = Math.round((my - g.top)  / (float) g.cellSize);

        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) return;

        // 실제 착수 여부/규칙 체크는 서버가 담당
        // 프로토콜: GAME_ACTION::row,col  (1~15)
        mainApp.getClient().sendMessage("GAME_ACTION::" + (row + 1) + "," + (col + 1));
    }

    // ─────────────────────  그리기  ─────────────────────

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        BoardGeom geom = computeGeometry();

        // 바둑판 배경
        g.setColor(new Color(245, 222, 179));
        g.fillRect(geom.left - 15, geom.top - 15,
                geom.boardSize + 30, geom.boardSize + 30);

        // 격자
        g.setColor(new Color(90, 60, 30));
        for (int i = 0; i < SIZE; i++) {
            int x = geom.left + i * geom.cellSize;
            int y = geom.top  + i * geom.cellSize;
            g.drawLine(geom.left, y, geom.left + geom.boardSize, y);  // 가로
            g.drawLine(x, geom.top, x, geom.top + geom.boardSize);    // 세로
        }

        // 돌 그리기
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                int stone = board[r][c];
                if (stone == 0) continue;

                int cx = geom.left + c * geom.cellSize;
                int cy = geom.top  + r * geom.cellSize;

                int radius = (int) (geom.cellSize * 0.4);
                int x = cx - radius;
                int y = cy - radius;

                if (stone == 1) {
                    g.setColor(Color.BLACK);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillOval(x, y, radius * 2, radius * 2);

                // 테두리
                g.setColor(Color.BLACK);
                g.drawOval(x, y, radius * 2, radius * 2);
            }
        }

        g.dispose();
    }

    // ─────────────────────  내부 계산용 구조체  ─────────────────────

    private static class BoardGeom {
        int left;      // 판 왼쪽 시작 x
        int top;       // 판 위쪽 시작 y
        int boardSize; // 격자 전체 픽셀 길이
        int cellSize;  // 한 칸 간격
    }

    /** 현재 패널 크기를 기준으로 격자 위치/칸 크기 계산 */
    private BoardGeom computeGeometry() {
        int w = getWidth();
        int h = getHeight();

        int padding = 30; // 판 주변 여백
        int usable = Math.min(w, h) - padding * 2;
        if (usable < 50) usable = Math.min(w, h);

        // 15줄 → 간격은 14칸이지만, 교차점 기준이니까 14칸 기준으로 나눔
        int cell = usable / (SIZE - 1);
        if (cell <= 0) cell = 1;

        int boardSize = cell * (SIZE - 1);

        int left = (w - boardSize) / 2;
        int top  = (h - boardSize) / 2;

        BoardGeom g = new BoardGeom();
        g.left = left;
        g.top = top;
        g.boardSize = boardSize;
        g.cellSize = cell;
        return g;
    }
}
