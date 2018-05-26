package badtzmarupekkle.littlethings.entity;


public class Song extends EntityModel {
    private boolean writer;
    private long timestamp;
    private String key;
    private String message;

    public void setWriter(boolean writer) {
        this.writer = writer;
    }
    public boolean getWriter() {
        return writer;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public long getTimestamp() {
        return timestamp;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public String getKey() {
        return key;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
}
