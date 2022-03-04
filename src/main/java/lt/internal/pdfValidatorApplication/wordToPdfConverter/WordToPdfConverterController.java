package lt.internal.pdfValidatorApplication.wordToPdfConverter;

import lt.internal.pdfValidatorApplication.message.ResponseMessage;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public ResponseEntity<ResponseMessage>uploadFile(@RequestParam("file") MultipartFile file){
        if(wordToPdfConverterService.hasDocOrDocxFormat(file)) {
            try {
                wordToPdfConverterService.saveFiles(file);
                message = "Uploaded the file successfully";
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(file.getOriginalFilename(),
                        Collections.singletonList(message), HttpStatus.OK.value()));
            } catch (Exception e) {
                message = "Could not upload the file";
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(file.getOriginalFilename(),
                        Collections.singletonList(message), HttpStatus.OK.value()));
            }
        }
        message = "Please upload a .doc or .docx file!";
        return ResponseEntity.status(BAD_REQUEST).body(createResponseMessage(message, BAD_REQUEST.value()));
    }

    @PostMapping(path = {"/files/convert","/files/convert/{fileName}"})
    public ResponseEntity<List<ResponseMessage>> convertAllFiles(@RequestParam(value = "fileName",required = false ) String fileName){
           List<PdfMessages>pdfMessages = WordToPdfConverterService.convertAllDocFilesInDirectoryToPdf(Optional.ofNullable(fileName));
           List<ResponseMessage>responseMessages = pdfMessages.stream()
                   .map(er -> {
                       String nameOfFile = er.getFileName();
                       List<String>msg = er.getMessages();
                       int statusCode = er.getStatusCode();
                       return new ResponseMessage(nameOfFile, msg, statusCode);
                   })
                   .collect(Collectors.toList());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(responseMessages);
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileInfo>> getListOfFiles() {
        List<FileInfo> fileInfos = wordToPdfConverterService.retrieveAllFiles()
                .map(path -> {
            String filename = path.getFileName().toString();
            String url = MvcUriComponentsBuilder
                    .fromMethodName(WordToPdfConverterController.class, "getFile",
                            path.getFileName().toString()).build().toString().replaceAll(" ","%20");
            return new FileInfo(filename, url);
        }).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        Resource file = wordToPdfConverterService.retrieveFileAccordingGivenFileName(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    private ResponseMessage createResponseMessage(String message, int statusCode) {
        return new ResponseMessage(Collections.singletonList(message), statusCode);
    }

}
