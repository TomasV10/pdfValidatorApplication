package lt.internal.pdfValidatorApplication.message;

import java.util.List;

public class ResponseMessage {
    private String fileName;
    private List<String> message;
    private int statusCode;

    public ResponseMessage() {
    }

    public ResponseMessage(List<String> message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public ResponseMessage(String fileName, List<String> message, int statusCode) {
        this.fileName = fileName;
        this.message = message;
        this.statusCode = statusCode;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getMessage() {
        return message;
    }

    public void setMessage(List<String> message) {
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
