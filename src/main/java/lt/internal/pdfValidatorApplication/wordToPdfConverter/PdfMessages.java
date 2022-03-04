package lt.internal.pdfValidatorApplication.wordToPdfConverter;

import java.util.ArrayList;
import java.util.List;

public class PdfMessages {
    private String fileName;
    private List<String> messages = new ArrayList<>();
    private int statusCode;

    public PdfMessages(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public List<String>addMessage(String anyMessage){
        messages.add(anyMessage);
        return messages;
    }

    public List<String>addMessages(String anyMessage){
        messages.add(anyMessage);
        return messages;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return "PdfMessages{" +
                "fileName='" + fileName + '\'' +
                ", errors=" + messages +
                ", statusCode=" + statusCode +
                '}';
    }
}
