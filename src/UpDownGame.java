import java.util.Random;

public class UpDownGame {
    private int target;
    private int min;
    private int max;
    private int attempts;

    public UpDownGame() {
        this.min = 1;
        this.max = 100;
        this.attempts = 0;
        this.target = new Random().nextInt(100) + 1; // 1~100
        System.out.println("[업다운] 정답 생성: " + target);
    }

    /**
     * @return "CORRECT", "UP", "DOWN", "OUT_OF_RANGE", "NOT_NUMBER"
     */
    public String checkGuess(String guessStr) {
        guessStr = guessStr.trim();
        int guess;

        try {
            guess = Integer.parseInt(guessStr);
        } catch (NumberFormatException e) {
            return "NOT_NUMBER";
        }

        attempts++;

        if (guess < min || guess > max) {
            return "OUT_OF_RANGE";
        }

        if (guess == target) {
            return "CORRECT";
        } else if (guess < target) {
            // 현재 최소 범위 업데이트
            if (guess + 1 > min) min = guess + 1;
            return "UP";
        } else {
            // 현재 최대 범위 업데이트
            if (guess - 1 < max) max = guess - 1;
            return "DOWN";
        }
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getAttempts() {
        return attempts;
    }

    public int getTarget() {
        return target;
    }
}
