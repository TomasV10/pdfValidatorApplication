package lt.internal.pdfValidatorApplication.messages;

import java.util.List;

public class DeleteMessage {
    private String fileName;
    private List<String> message;
    private boolean isFileDeleted;

    public DeleteMessage(String fileName, List<String> message, boolean isFileDeleted) {
        this.fileName = fileName;
        this.message = message;
        this.isFileDeleted = isFileDeleted;
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

    public boolean isFileDeleted() {
        return isFileDeleted;
    }

    public void setFileDeleted(boolean fileDeleted) {
        isFileDeleted = fileDeleted;
    }
}
