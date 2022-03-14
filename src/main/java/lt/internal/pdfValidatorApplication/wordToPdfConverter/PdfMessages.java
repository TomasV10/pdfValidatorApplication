package lt.internal.pdfValidatorApplication.wordToPdfConverter;

import lt.internal.pdfValidatorApplication.pdfValidator.PdfValidationResult;

import java.util.ArrayList;
import java.util.List;

public class PdfMessages {
    private String fileName;
    private List<String> messages = new ArrayList<>();
    private boolean isPdfValid;
    private boolean isConvertedToPdf;
    private String url = " ";
    private PdfConversionResult resultsConversion;
    private PdfValidationResult resultsValidation;

    public PdfMessages(List<String> messages, boolean isPdfValid, String url) {
        this.messages = messages;
        this.isPdfValid = isPdfValid;
        this.url = url;
    }

    public PdfMessages(String fileName, List<String> messages, boolean isConvertedToPdf) {
        this.fileName = fileName;
        this.messages = messages;
        this.isConvertedToPdf = isConvertedToPdf;
    }

    public PdfMessages(String fileName, List<String> messages, boolean isPdfValid, boolean isConvertedToPdf, String url) {
        this.fileName = fileName;
        this.messages = messages;
        this.isPdfValid = isPdfValid;
        this.isConvertedToPdf = isConvertedToPdf;
        this.url = url;
    }

    public PdfMessages(PdfConversionResult resultsConversion, PdfValidationResult resultsValidation) {
        this.resultsConversion = resultsConversion;
        this.resultsValidation = resultsValidation;
    }

    public PdfMessages(String fileName) {
        this.fileName = fileName;
    }

    public PdfConversionResult getResultsConversion() {
        return resultsConversion;
    }

    public void setResultsConversion(PdfConversionResult resultsConversion) {
        this.resultsConversion = resultsConversion;
    }

    public PdfValidationResult getResultsValidation() {
        return resultsValidation;
    }

    public void setResultsValidation(PdfValidationResult resultsValidation) {
        this.resultsValidation = resultsValidation;
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


    public PdfMessages mapToPdfMessage(){
        return new PdfMessages(getResultsConversion().getFileName(),
                getResultsConversion().getMessages(), getResultsValidation().isPdfValid(),
                getResultsConversion().isConvertedToPdf(), getResultsValidation().getUrl());
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
