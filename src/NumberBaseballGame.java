import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NumberBaseballGame {
    private List<Integer> answer;

    public NumberBaseballGame() {
        generateAnswer();
    }

    private void generateAnswer() {
        answer = new ArrayList<>();
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i <= 9; i++) numbers.add(i);
        Collections.shuffle(numbers);
        for (int i = 0; i < 4; i++) answer.add(numbers.get(i));
        System.out.println("[게임 엔진] 정답 생성: " + answer);
    }

    public String checkGuess(String guess) {
        if (guess.length() != 4) return "4자리 숫자만 입력하세요.";
        if (!guess.matches("\\d+")) return "숫자만 입력하세요.";

        List<Integer> guessNumbers = new ArrayList<>();
        try {
            for (char c : guess.toCharArray()) {
                guessNumbers.add(Integer.parseInt(String.valueOf(c)));
            }
        } catch (NumberFormatException e) {
            return "숫자 변환 오류.";
        }

        if (guessNumbers.stream().distinct().count() < 4)
            return "서로 다른 4자리 숫자를 입력하세요.";

        int strikes = 0, balls = 0;
        for (int i = 0; i < 4; i++) {
            if (guessNumbers.get(i).equals(answer.get(i))) strikes++;
            else if (answer.contains(guessNumbers.get(i))) balls++;
        }

        if (strikes == 4) return "4S - 정답입니다!";
        return strikes + "S " + balls + "B";
    }

    public String getAnswerString() {
        return "" + answer.get(0) + answer.get(1)
                + answer.get(2) + answer.get(3);
    }
}
