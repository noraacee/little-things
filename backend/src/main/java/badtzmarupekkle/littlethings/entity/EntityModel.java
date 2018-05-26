package badtzmarupekkle.littlethings.entity;

public abstract class EntityModel {
    private boolean writer;
    private long id;
    private long timestamp;
    private String secret;

    public boolean getWriter() {
        return writer;
    }
    public void setWriter(boolean writer) {
        this.writer=writer;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String getSecret() {
        return secret;
    }
    public void setSecret(String secret) {
        this.secret = secret;
    }
}
