import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class OmokBoardPanel extends JPanel {
    private static final int SIZE = 15; // 15x15
    private int[][] board = new int[SIZE][SIZE]; // 0: empty, 1: black, 2: white
    private Main mainApp;

    public OmokBoardPanel(Main mainApp) {
        this.mainApp = mainApp;
        setBackground(new Color(210, 180, 140)); // 나무 느낌

        // 기본 사이즈(프레임이 커지면 비례해서 커짐)
        setPreferredSize(new Dimension(500, 500));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    public void resetBoard() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                board[r][c] = 0;
            }
        }
        repaint();
    }

    public void applyMove(int row, int col, boolean isBlack) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) return;
        board[row][col] = isBlack ? 1 : 2;
        repaint();
    }

    private void handleClick(int x, int y) {
        // 현재 패널 크기에 맞게 셀 크기 계산
        int w = getWidth();
        int h = getHeight();

        int margin = Math.min(w, h) / 20; // 바깥 여백
        int boardSize = Math.min(w, h) - margin * 2;
        double cell = boardSize / (double) (SIZE - 1);

        int boardLeft = (w - boardSize) / 2;
        int boardTop = (h - boardSize) / 2;

        // 근처 격자 좌표 찾기
        int col = (int) Math.round((x - boardLeft) / cell);
        int row = (int) Math.round((y - boardTop) / cell);

        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) return;

        // 서버에 좌표 전송
        mainApp.getClient().sendMessage("OMOK_MOVE::" + row + "," + col);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();

        int margin = Math.min(w, h) / 20;
        int boardSize = Math.min(w, h) - margin * 2;
        int boardLeft = (w - boardSize) / 2;
        int boardTop = (h - boardSize) / 2;

        double cell = boardSize / (double) (SIZE - 1);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 격자 그리기
        g2.setColor(Color.BLACK);
        for (int i = 0; i < SIZE; i++) {
            int x1 = (int) Math.round(boardLeft + i * cell);
            int y1 = (int) Math.round(boardTop);
            int x2 = x1;
            int y2 = (int) Math.round(boardTop + (SIZE - 1) * cell);
            g2.drawLine(x1, y1, x2, y2);

            int xx1 = (int) Math.round(boardLeft);
            int yy1 = (int) Math.round(boardTop + i * cell);
            int xx2 = (int) Math.round(boardLeft + (SIZE - 1) * cell);
            int yy2 = yy1;
            g2.drawLine(xx1, yy1, xx2, yy2);
        }

        // 돌 그리기
        int stoneSize = (int) Math.round(cell * 0.8);
        int stoneOffset = stoneSize / 2;

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] == 0) continue;

                int cx = (int) Math.round(boardLeft + c * cell);
                int cy = (int) Math.round(boardTop + r * cell);

                if (board[r][c] == 1) g2.setColor(Color.BLACK);
                else g2.setColor(Color.WHITE);

                g2.fillOval(cx - stoneOffset, cy - stoneOffset, stoneSize, stoneSize);
                g2.setColor(Color.BLACK);
                g2.drawOval(cx - stoneOffset, cy - stoneOffset, stoneSize, stoneSize);
            }
        }
    }
}
