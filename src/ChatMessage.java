// ChatMessage.java

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private String sender;
    private String message;
    private String timestamp; // 시간 정보를 담을 필드 추가
    private boolean isMine;

    public ChatMessage(String sender, String message, boolean isMine) {
        this.sender = sender;
        this.message = message;
        this.isMine = isMine;
        // 메시지 생성 시 현재 시간을 "오후 3:30" 형식으로 저장
        this.timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("a h:mm"));
    }

    public String getSender() { return sender; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; } // timestamp getter 추가
    public boolean isMine() { return isMine; }
}
