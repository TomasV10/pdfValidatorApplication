package lt.internal.pdfValidatorApplication.message;

import java.util.List;

public class UploadMessage {
    private String fileName;
    private List<String> message;
    private boolean isFileUploaded;


    public UploadMessage(String fileName, List<String> message, boolean isFileUploaded) {
        this.fileName = fileName;
        this.message = message;
        this.isFileUploaded = isFileUploaded;
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

    public boolean isFileUploaded() {
        return isFileUploaded;
    }

    public void setFileUploaded(boolean fileUploaded) {
        isFileUploaded = fileUploaded;
    }
}
