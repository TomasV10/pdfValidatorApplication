package lt.internal.pdfValidatorApplication.wordToPdfConverter;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class WordToPdfConverterService {

    private static final int WD_DO_NOT_SAVE_CHANGES = 0;//Don't save pending changes.
    private static final int WD_FORMAT_PDF = 17;//WORD to PDF format
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".doc", ".docx"); //List of allowed extensions
    private static final Path root = Paths.get("uploads").toAbsolutePath();


    public void init() {
        try {
            Files.createDirectory(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    public void saveFiles(MultipartFile file){
        try {
            Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename()));
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    public Stream<Path> loadAll() {
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

    public static void convertAllDocFilesInDirectoryToPdf(Optional<String>fileName) {
        if(!isBaseDirectoryValid(root)) return;
        ConversionsCounter counter = new ConversionsCounter();
        try  {

            Files.walk(root, 1)
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.toString().toLowerCase())
                    .filter(path -> isOneOfAllowedExtensions(ALLOWED_EXTENSIONS, path))
                    .filter(filePath -> doesFileNameMatch(filePath, fileName))
                    .forEach(file -> convertWordToPdf(file, counter));

            printConversionsResults(counter);

        }catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }


    private static boolean doesFileNameMatch(String filePath, Optional<String>fileName){
        return fileName
                .map(filePath::endsWith)
                .orElse(true);
    }

    private static String getFileNameWithoutPath(String path) {
        return path.replaceAll("^.*[\\/\\\\]", "");
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

    private static void convertWordToPdf(String source, ConversionsCounter counter) {

        System.out.println("Word to PDF started...");
        long start = System.currentTimeMillis();
        try {
            ActiveXComponent app = createActiveXComponent();

            Dispatch doc = openingDocFile(source, app);

            String pathOfFileWithPdfExtension = changeExtensionToPdf(source);

            saveConvertedFile(pathOfFileWithPdfExtension, doc);
            counter.increaseSuccessfulConversionsCount();
            closeActiveXComponent(app);
        } catch (Exception e) {
            System.out.println("Error converting Word to PDF:" + e.getMessage());
            counter.increaseFailedConversionsCount();
            throw new IllegalArgumentException(e.getMessage());
        } finally {
            long end = System.currentTimeMillis();
            System.out.println("Time required:" + (end-start) + "ms");
        }
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

    private static String changeExtensionToPdf(String source) {
        System.out.println("change pdf extension");
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
    private static boolean isOneOfAllowedExtensions(List<String>allowedExtentions, String path) {
        return allowedExtentions.stream()
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
