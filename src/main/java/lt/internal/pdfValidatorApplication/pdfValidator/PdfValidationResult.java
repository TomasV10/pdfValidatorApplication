package lt.internal.pdfValidatorApplication.pdfValidator;

import java.util.ArrayList;
import java.util.List;

public class PdfValidationResult {
    private boolean isPdfValid;
    private List<String>messages = new ArrayList<>();
    private String url;

    public boolean isPdfValid() {
        return isPdfValid;
    }

    public void setPdfValid(boolean pdfValid) {
        isPdfValid = pdfValid;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String>addMessage(String anyMessage){
        messages.add(anyMessage);
        return messages;
    }

    @Override
    public String toString() {
        return "PdfValidationResult{" +
                "isPdfValid=" + isPdfValid +
                ", messages=" + messages +
                ", url='" + url + '\'' +
                '}';
    }
}
