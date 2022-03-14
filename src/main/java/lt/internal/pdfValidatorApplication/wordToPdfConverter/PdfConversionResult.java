package lt.internal.pdfValidatorApplication.wordToPdfConverter;

import java.util.ArrayList;
import java.util.List;

public class PdfConversionResult {
    private String fileName;
    private boolean isConvertedToPdf;
    private List<String> messages =new ArrayList<>();

    public PdfConversionResult() {
    }

    public PdfConversionResult(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isConvertedToPdf() {
        return isConvertedToPdf;
    }

    public void setConvertedToPdf(boolean convertedToPdf) {
        isConvertedToPdf = convertedToPdf;
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

    public List<String>addMessages(List<String>validationMessages){
        messages.addAll(validationMessages);
        return messages;
    }


    @Override
    public String toString() {
        return "PdfConversionResult{" +
                "fileName='" + fileName + '\'' +
                ", isConvertedToPdf=" + isConvertedToPdf +
                ", messages=" + messages +
                '}';
    }
    public PdfMessages toPdfMessage(){
        return new PdfMessages(fileName, messages, isConvertedToPdf);
    }
}
