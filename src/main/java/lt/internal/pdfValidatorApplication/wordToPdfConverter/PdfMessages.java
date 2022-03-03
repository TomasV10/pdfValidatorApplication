package lt.internal.pdfValidatorApplication.wordToPdfConverter;

import java.util.ArrayList;
import java.util.List;

public class PdfMessages {
    private String fileName;
    private List<String> errors = new ArrayList<>();
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

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String>addMessage(String anyMessage){
        errors.add(anyMessage);
        return errors;
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
                ", errors=" + errors +
                ", statusCode=" + statusCode +
                '}';
    }
}
