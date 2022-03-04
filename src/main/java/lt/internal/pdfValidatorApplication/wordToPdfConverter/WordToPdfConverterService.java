package lt.internal.pdfValidatorApplication.wordToPdfConverter;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import lt.internal.pdfValidatorApplication.pdfValidator.PdfValidatorService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class WordToPdfConverterService implements FilesStorageService{

    private static final int WD_DO_NOT_SAVE_CHANGES = 0;//Don't save pending changes.
    private static final int WD_FORMAT_PDF = 17;//WORD to PDF format
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".doc", ".docx"); //List of allowed extensions
    private static final Path root = Paths.get("uploads").toAbsolutePath();

    private final PdfValidatorService pdfValidatorService;

    public WordToPdfConverterService(PdfValidatorService pdfValidatorService) {
        this.pdfValidatorService = pdfValidatorService;
    }

    @Override
    public void createDirectoryForUploadedFiles() {
        try {
            Files.createDirectory(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }
    @Override
    public void deleteUploadsFolder() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    public void saveFiles(MultipartFile file){
        try {
            Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename()));
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    public Resource retrieveFileAccordingGivenFileName(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            return checkIfFileExist(resource);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }



    public Stream<Path> retrieveAllFiles() {
        try {
            return Files.walk(this.root, 1)
                    .filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }

    public boolean hasDocOrDocxFormat(MultipartFile file){
       return ALLOWED_EXTENSIONS.stream()
                .map(String::toLowerCase)
                .anyMatch(file.getOriginalFilename()::endsWith);
    }

    public static List<PdfMessages> convertAllDocFilesInDirectoryToPdf(Optional<String>fileName) {
//        if(!isBaseDirectoryValid(root)) return;
        ConversionsCounter counter = new ConversionsCounter();
        try  {
            List<PdfMessages> messagesList =
                Files.walk(root, 1)
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> p.toString().toLowerCase())
                        .filter(path -> isOneOfAllowedExtensions(path))
                        .filter(filePath -> doesFileNameMatch(filePath, fileName))
                        .map(file -> convertWordToPdf(file, counter))
                        .collect(Collectors.toList());

            printConversionsResults(counter);

            List<PdfMessages> validatorMsgList =  PdfValidatorService.validatePdfDocuments(root, fileName);
            return mergeBothListToOne(messagesList, validatorMsgList);
        }catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static List<PdfMessages> mergeBothListToOne(List<PdfMessages> messagesList, List<PdfMessages> validatorMsgList) {
        for(PdfMessages msg : messagesList){
            for(PdfMessages message : validatorMsgList){
                if(msg.getFileName().equals(message.getFileName())){
                    message.getMessages()
                            .forEach(msg::addMessage);
                }
            }
        }return messagesList;
    }

    private Resource checkIfFileExist(Resource resource) {
        System.out.println(resource);
        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("Could not read the file!");
        }
    }

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

    private static PdfMessages convertWordToPdf(String source, ConversionsCounter counter) {
        PdfMessages msg = new PdfMessages(getFileNameWithoutPath(changeExtensionToPdf(source)));
        System.out.println("Word to PDF started...");
        long start = System.currentTimeMillis();
        try {
            ActiveXComponent app = createActiveXComponent();

            Dispatch doc = openingDocFile(source, app);

            String pathOfFileWithPdfExtension = changeExtensionToPdf(source);

            saveConvertedFile(pathOfFileWithPdfExtension, doc);
            counter.increaseSuccessfulConversionsCount();
            closeActiveXComponent(app);
            msg.addMessage("Converted file successfully");
            msg.setStatusCode(HttpStatus.OK.value());
            return msg;
        } catch (Exception e) {
            msg.addMessage("Error converting Word to PDF:" + e.getMessage());
            msg.setStatusCode(HttpStatus.BAD_REQUEST.value());
            return msg;
        } finally {
            long end = System.currentTimeMillis();
            System.out.println("Time required:" + (end-start) + "ms");
        }
    }

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

    private static void saveConvertedFile(String pathOfFileWithPdfExtension, Dispatch doc) {
        removeFileIfAlreadyExist(pathOfFileWithPdfExtension);
        System.out.println("Convert document to PDF:" + pathOfFileWithPdfExtension);
        Dispatch.call(doc, "SaveAs", pathOfFileWithPdfExtension, WD_FORMAT_PDF);
        Dispatch.call(doc, "Close", false);
    }


    /*
     * Deletes file if exist
     */

    private static void removeFileIfAlreadyExist(String pathOfFileWithPdfExtension) {
        File tofile = new File(pathOfFileWithPdfExtension);
        if (tofile.exists()) {
            tofile.delete();
        }
    }


    /*
     * Replacing .doc or .docx extension to .pdf
     * @param source is .doc or .docx file name
     */

    public static String changeExtensionToPdf(String source) {
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

    public static boolean isBaseDirectoryValid(Path baseDir) {
        if (!Files.exists(baseDir)) {
            System.err.println("Base path does not exist " + baseDir);
            return false;
        }else if(!Files.isDirectory(baseDir)) {
            System.err.println("Base path must be a directory! " + baseDir);
            return false;
        }return true;
    }
}
