package badtzmarupekkle.littlethings.entity;
public class Response {
    private boolean success;
    private int errorCode;

    public boolean getSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public int getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
