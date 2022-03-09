package lt.internal.pdfValidatorApplication.wordToPdfConverter;

import java.util.ArrayList;
import java.util.List;

public class PdfMessages {
    private String fileName;
    private List<String> messages = new ArrayList<>();
    private boolean isPdfValid;
    private boolean isConvertedToPdf;
    private String url = " ";

    public PdfMessages() {
    }

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

    public boolean isPdfValid() {
        return isPdfValid;
    }

    public void setPdfValid(boolean pdfValid) {
        isPdfValid = pdfValid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isConvertedToPdf() {
        return isConvertedToPdf;
    }

    public void setConvertedToPdf(boolean convertedToPdf) {
        isConvertedToPdf = convertedToPdf;
    }

    @Override
    public String toString() {
        return "PdfMessages{" +
                ", fileName='" + fileName + '\'' +
                ", messages=" + messages +
                ", isPdfValid=" + isPdfValid +
                ", isConvertedToPdf=" + isConvertedToPdf +
                ", url='" + url + '\'' +
                '}';
    }
}
