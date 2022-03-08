package lt.internal.pdfValidatorApplication.wordToPdfConverter;

import lt.internal.pdfValidatorApplication.messages.DeleteMessage;
import lt.internal.pdfValidatorApplication.messages.ResponseMessage;
import lt.internal.pdfValidatorApplication.messages.UploadMessage;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api")
public class WordToPdfConverterController {

    private final WordToPdfConverterService wordToPdfConverterService;
    private String message = "";

    public WordToPdfConverterController(WordToPdfConverterService wordToPdfConverterService) {
        this.wordToPdfConverterService = wordToPdfConverterService;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadMessage>uploadFile(@RequestParam("file") MultipartFile file){
        if(wordToPdfConverterService.hasDocOrDocxFormat(file)) {
            try {
                wordToPdfConverterService.saveFiles(file);
                message = "Uploaded the file successfully";
                return ResponseEntity.status(OK).body(new UploadMessage(file.getOriginalFilename(),
                        Collections.singletonList(message), true));
            } catch (Exception e) {
                message = "Could not upload the file";
                return ResponseEntity.status(EXPECTATION_FAILED)
                            .body(new UploadMessage(file.getOriginalFilename(),
                                    Collections.singletonList(message), false));
            }
        }
        message = "Please upload a .doc or .docx file!";
        return ResponseEntity.status(BAD_REQUEST).body(createUploadMessage(file.getOriginalFilename(), message,
                                                                                                    false));
    }

    @PostMapping(path = {"/files/convert","/files/convert/{fileName}"})
    public ResponseEntity<List<ResponseMessage>> convertAllFiles(@RequestParam(value = "fileName",required = false )
                                                                             String fileName){
        List<ResponseMessage> responseMessages = createResponseMessages(fileName);
        return ResponseEntity.status(OK)
                    .body(responseMessages);
    }

    @DeleteMapping("/files/delete/{fileName}")
    public ResponseEntity<DeleteMessage>deleteFile(@PathVariable("fileName") String fileName) throws IOException {
            if(wordToPdfConverterService.deleteFileIfExist(fileName)) {
                message = "Deleted file successfully";
                return ResponseEntity.status(OK).body(new DeleteMessage(fileName,
                        Collections.singletonList(message), true));
            }else {
                message = "File not found";
                return ResponseEntity.status(BAD_REQUEST).body(new DeleteMessage(fileName,
                    Collections.singletonList(message), false));
            }
    }


    @GetMapping("/files")
    public ResponseEntity<List<FileInfo>> getListOfAllPdfFiles() {
        List<FileInfo> fileInfos = wordToPdfConverterService.retrieveAllPdfFiles()
                .map(path -> {
            String filename = path.getFileName().toString();
            String url = MvcUriComponentsBuilder
                    .fromMethodName(WordToPdfConverterController.class, "getFile",
                            path.getFileName().toString()).build().toString().replaceAll(" ","%20");
            return new FileInfo(filename, url);
        }).collect(Collectors.toList());
        return ResponseEntity.status(OK).body(fileInfos);
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        Resource file = wordToPdfConverterService.retrieveFileAccordingGivenFileName(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename()
                        + "\"").body(file);
    }

    private UploadMessage createUploadMessage(String fileName, String message, boolean isFileUploaded) {
        return new UploadMessage(fileName, Collections.singletonList(message), isFileUploaded);
    }

    /*
    Creates response message with validation results
     */

    private List<ResponseMessage> createResponseMessages(String fileName) {
        List<PdfMessages>pdfMessages = WordToPdfConverterService.convertAllDocFilesInDirectoryToPdf(ofNullable(fileName));
        List<ResponseMessage>responseMessages = pdfMessages.stream()
                .map(er -> {
                    String nameOfFile = er.getFileName();
                    List<String>msg = er.getMessages();
                    boolean isValid = er.isPdfValid();
                    boolean isConvertedToPdf = er.isConvertedToPdf();
                    String url = er.getUrl();
                    return new ResponseMessage(nameOfFile, msg, isValid, isConvertedToPdf, url);
                })
                .collect(Collectors.toList());
        return responseMessages;
    }
}
