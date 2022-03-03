package lt.internal.pdfValidatorApplication.wordToPdfConverter;

import java.util.ArrayList;
import java.util.List;

public class PdfsResults {
    List<PdfMessages> conversionMessages = new ArrayList<>();
    List<PdfMessages>validationMessage = new ArrayList<>();

    public List<PdfMessages> getConversionMessages() {
        return conversionMessages;
    }

    public void setConversionMessages(List<PdfMessages> conversionMessages) {
        this.conversionMessages = conversionMessages;
    }

    public List<PdfMessages> getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(List<PdfMessages> validationMessage) {
        this.validationMessage = validationMessage;
    }
}
