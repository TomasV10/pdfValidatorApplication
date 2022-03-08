package lt.internal.pdfValidatorApplication.messages;

import java.util.List;

public class ResponseMessage {
    private String fileName;
    private List<String> message;
    private boolean isPdfValid;
    private boolean isConvertedToPdf;
    private String url = " ";

    public ResponseMessage(String fileName, List<String> message, boolean isPdfValid,
                                                                boolean isConvertedToPdf, String url) {
        this.fileName = fileName;
        this.message = message;
        this.isPdfValid = isPdfValid;
        this.isConvertedToPdf = isConvertedToPdf;
        this.url = url;
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
}
