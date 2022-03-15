package lt.internal.pdfValidatorApplication.wordToPdfConverter;


import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import lt.internal.pdfValidatorApplication.pdfValidator.PdfValidationResult;
import lt.internal.pdfValidatorApplication.pdfValidator.PdfValidatorService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WordToPdfConverterService implements FilesStorageService{

    private static final int WD_DO_NOT_SAVE_CHANGES = 0;//Don't save pending changes.
    private static final int WD_FORMAT_PDF = 17;//WORD to PDF format
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".doc", ".docx"); //List of allowed extensions
    private static final Path root = Paths.get("uploads").toAbsolutePath();

    /*
    Creates "uploads" directory when application is started
     */

    @Override
    public void createDirectoryForUploadedFiles() {
        try {
            Files.createDirectory(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    /*
    Deletes "uploads" directory when application is started
     */

    @Override
    public void deleteUploadsFolder() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    /*
    Saves file to "uploads" directory
     */

    public void saveFiles(MultipartFile file){
        try {
            Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename()));
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    /*
    Retrieves file according given name
     */

    public Resource retrieveFileAccordingGivenFileName(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            return checkIfFileExist(resource);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    /*
    Validating format of upload file
     */

    public boolean hasDocOrDocxFormat(MultipartFile file){
        return ALLOWED_EXTENSIONS.stream()
                .map(String::toLowerCase)
                .anyMatch(file.getOriginalFilename()::endsWith);
    }

    /*
    Deletes file if exist
     */

    public boolean deleteFileIfExist(String fileName) throws IOException {
        return Files.deleteIfExists(Paths.get("uploads" + "/" + fileName));
    }

    /*
    This is the main conversion method
     */

    public static List<PdfMessages> convertAllDocFilesInDirectoryToPdf(Optional<String>fileName) {
        isBaseDirectoryValid(root);
        ConversionsCounter counter = new ConversionsCounter();
        try  {
            List<PdfMessages> messagesList =
                    Files.walk(root, 1)
                            .filter(p -> !Files.isDirectory(p))
                            .map(p -> p.toString().toLowerCase())
                            .filter(WordToPdfConverterService::isOneOfAllowedExtensions)
                            .filter(filePath -> doesFileNameMatch(filePath, fileName))
                            .map(file -> convertAndValidatePdf(file, counter))
                            .collect(Collectors.toList());

            printConversionsResults(counter);

            return doesMessageListEmpty(messagesList,fileName);
        }catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /*
    Retrieves all files which are in directory
     */

    public List<DocFileInfo> getAllFiles() throws IOException {
        return
                Files.walk(root, 1)
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> p.toString().toLowerCase())
                        .filter(WordToPdfConverterService::isOneOfAllowedExtensions)
                        .map(this::collectDocFileInfo)
                        .collect(Collectors.toList());
    }

    /*
    collects information about doc or docx file.
     */

    private DocFileInfo collectDocFileInfo(String fileName)  {
        DocFileInfo docFileInfo = new DocFileInfo(getFileNameWithoutPath(fileName));
        String pdfFileName = changeExtensionToPdf(fileName);
        PdfValidationResult validationResult = PdfValidatorService.validatePdfDocument(new PdfConversionResult(pdfFileName));
        boolean isConvertedToPdf = isDocConvertedToPdf(pdfFileName);
        docFileInfo.setConvertedToPdf(isConvertedToPdf);
        if(isConvertedToPdf){
            docFileInfo.setPdfFileName(getFileNameWithoutPath(pdfFileName));
            boolean isPdfValid = validationResult.isPdfValid();
            docFileInfo.setPdfValid(isPdfValid);
            if(isPdfValid) docFileInfo.setUrl(validationResult.getUrl());
            else docFileInfo.setUrl(" ");
        }
        return docFileInfo;
    }

    /*
    checks if file is converted to PDF or not.
     */

    private boolean isDocConvertedToPdf(String fileName){
        return Files.exists(Paths.get(fileName));
    }


    /*
    this method has all conversion doc to pdf logic
     */

    private static PdfConversionResult convertWordToPdf(String source, ConversionsCounter counter) {
        PdfConversionResult result = new PdfConversionResult(getFileNameWithoutPath(source));
        System.out.println("Word to PDF started...");
        long start = System.currentTimeMillis();
        try {
            ActiveXComponent app = createActiveXComponent();

            Dispatch doc = openingDocFile(source, app);

            String pathOfFileWithPdfExtension = changeExtensionToPdf(source);

            saveConvertedFile(pathOfFileWithPdfExtension, doc);
            counter.increaseSuccessfulConversionsCount();
            closeActiveXComponent(app);
            result.setFileName(changeExtensionToPdf(source));
            result.addMessage("Converted file successfully");
            result.setConvertedToPdf(true);
            return result;
        } catch (Exception e) {
            result.addMessage("Error converting Word to PDF:" + e.getMessage());
            counter.increaseFailedConversionsCount();
            return result;
        } finally {
            long end = System.currentTimeMillis();
            System.out.println("Time required:" + (end-start) + "ms");
        }
    }

    /*
    Converts doc & docx files to PDF and validates PDF files
     */

    private static PdfMessages convertAndValidatePdf(String file, ConversionsCounter counter){
        PdfConversionResult conversion = convertWordToPdf(file, counter);
        if(conversion.isConvertedToPdf()){
            PdfValidationResult pdfValidationResult = PdfValidatorService.validatePdfDocument(conversion);
            conversion.addMessages(pdfValidationResult.getMessages());
            return new PdfMessages(conversion, pdfValidationResult).mapToPdfMessage();
        }else return conversion.toPdfMessage();
    }

    /*
    This method checks the message list. If it's empty, that means, file with given name does not exist
     */

    private static List<PdfMessages> doesMessageListEmpty(List<PdfMessages>messagesList, Optional<String>fileName){
        PdfMessages msg = new PdfMessages(fileName.toString());
        if(messagesList.isEmpty() && fileName != null){
            msg.setFileName(fileName.get());
            msg.addMessage("File not found");
            messagesList.add(msg);
            return messagesList;
        }else return messagesList;
    }

    /*
    Check if file exists
     */

    private Resource checkIfFileExist(Resource resource) {
        System.out.println(resource);
        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("Could not read the file!");
        }
    }

    /*
     * If file name is given, then checks if it match to with one of files in the uploads directory.
     */

    private static boolean doesFileNameMatch(String filePath, Optional<String>fileName){
        return fileName
                .map(String::toLowerCase)
                .map(filePath::endsWith)
                .orElse(true);
    }

    /*
     * Printing results of conversions.
     */

    private static void printConversionsResults(ConversionsCounter counter) {
        System.out.println("Successful conversions: " + counter.getSuccessfulConversion());
        System.out.println("Failed conversions: " + counter.getFailedConversion());
    }


    /*
     * Converts word document to pdf format file using ActiveXComponent
     * @param source - word file path
     * @param counter - counts successful and failed conversions
     */



    private static String getFileNameWithoutPath(String path) {
        return path.replaceAll("^.*[\\/\\\\]", "");
    }

    /*
     * Closing activeXComponent
     */

    private static void closeActiveXComponent(ActiveXComponent app) {
        app.invoke("Quit", WD_DO_NOT_SAVE_CHANGES);
    }



    /*
     * Saving converted doc file to pdf
     * @param pathOfFileWithPdfExtension - pdf file path
     * @param doc - doc file
     */

    private static void saveConvertedFile(String pathOfFileWithPdfExtension, Dispatch doc) throws IOException {
        removeFileIfAlreadyExist(pathOfFileWithPdfExtension);
        System.out.println("Convert document to PDF:" + pathOfFileWithPdfExtension);
        Dispatch.call(doc, "SaveAs", pathOfFileWithPdfExtension, WD_FORMAT_PDF);
        Dispatch.call(doc, "Close", false);
    }


    /*
     * Deletes file if exist
     */

    private static void removeFileIfAlreadyExist(String pathOfFileWithPdfExtension) throws IOException {
        Files.deleteIfExists(Paths.get(pathOfFileWithPdfExtension));
    }


    /*
     * Replacing .doc or .docx extension to .pdf
     * @param source is .doc or .docx file name
     */

    private static String changeExtensionToPdf(String source) {
        return source.replaceFirst("[.][^.]+$", "")+ ".pdf";
    }

    /*
     * Opening doc file
     * @param source - doc file path
     * @param app -
     */

    private static Dispatch openingDocFile(String source, ActiveXComponent app) {
        Dispatch docs = app.getProperty("Documents").toDispatch();
        System.out.println("Open document:" + source);
        Dispatch doc = Dispatch.call(docs, "Open", source, false, true).toDispatch();
        return doc;
    }


    /*
     * Creating activeXComponent and assigning property
     */

    private static ActiveXComponent createActiveXComponent() {
        ActiveXComponent app = new ActiveXComponent("Word.Application");
        app.setProperty("Visible", false);
        return app;
    }


    /*
     * Checking if file extension matches allowed extensions list
     * @param allowedExtentions is a list of allowed extensions (.doc and .docx)
     * @param path - file path
     */
    private static boolean isOneOfAllowedExtensions(String path) {
        return WordToPdfConverterService.ALLOWED_EXTENSIONS.stream()
                .map(String::toLowerCase)
                .anyMatch(path::endsWith);

    }

    /*
     * Checks base path if it exists and if its a directory
     */

    public static void isBaseDirectoryValid(Path baseDir) {
        if (!Files.exists(baseDir)) {
            System.err.println("Base path does not exist " + baseDir);
            throw new IllegalArgumentException("Base path does not exist " + baseDir);
        }else if(!Files.isDirectory(baseDir)) {
            System.err.println("Base path must be a directory! " + baseDir);
            throw new IllegalArgumentException("Base path must be a directory! " + baseDir);
        }
    }
}
