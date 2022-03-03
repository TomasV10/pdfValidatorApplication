package lt.internal.pdfValidatorApplication.wordToPdfConverter;

public interface FilesStorageService {
    void createDirectoryForUploadedFiles();
    void deleteUploadsFolder();
}
