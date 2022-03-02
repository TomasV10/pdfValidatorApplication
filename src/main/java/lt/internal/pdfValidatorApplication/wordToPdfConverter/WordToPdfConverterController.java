package lt.internal.pdfValidatorApplication.wordToPdfConverter;

import lt.internal.pdfValidatorApplication.message.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

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
                message = "Uploaded the file successfully: " + file.getOriginalFilename();
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
            } catch (Exception e) {
                message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
            }
        }
        message = "Please upload a .doc or .docx file!";
        return ResponseEntity.status(BAD_REQUEST).body(createResponseMessage(message));
    }

    @PostMapping(path = {"/files/convert","/files/convert/{fileName}"})
    public ResponseEntity<ResponseMessage> convertAllFiles(@RequestParam(value = "fileName",required = false ) String fileName){
        try {
            WordToPdfConverterService.convertAllDocFilesInDirectoryToPdf(Optional.ofNullable(fileName));
            message = "Converted file successfully";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        }catch (Exception e) {
            message = "Could not convert the file!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

//    @GetMapping("/files")
//    public ResponseEntity<List<FileInfo>> getListFiles() {
//        List<FileInfo> fileInfos = storageService.loadAll().map(path -> {
//            String filename = path.getFileName().toString();
//            String url = MvcUriComponentsBuilder
//                    .fromMethodName(FilesController.class, "getFile", path.getFileName().toString()).build().toString();
//            return new FileInfo(filename, url);
//        }).collect(Collectors.toList());
//        return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
//    }



    private ResponseMessage createResponseMessage(String message) {
        return new ResponseMessage(message);
    }

}
