import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private String sender;
    private String message;
    private boolean isMine;
    private String timestamp;

    public ChatMessage(String sender, String message, boolean isMine) {
        this.sender = sender;
        this.message = message;
        this.isMine = isMine;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getSender() { return sender; }
    public String getMessage() { return message; }
    public boolean isMine() { return isMine; }
    public String getTimestamp() { return timestamp; }
}
