package lt.internal.pdfValidatorApplication.wordToPdfConverter;

public class DocFileInfo {
    private String fileName;
    private boolean isConvertedToPdf;
    private boolean isPdfValid;
    private String pdfFileName;
    private String url;

    public DocFileInfo(String fileName) {
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

    public boolean isPdfValid() {
        return isPdfValid;
    }

    public void setPdfValid(boolean pdfValid) {
        isPdfValid = pdfValid;
    }

    public String getPdfFileName() {
        return pdfFileName;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName = pdfFileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
