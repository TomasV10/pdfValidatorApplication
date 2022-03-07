package lt.internal.pdfValidatorApplication;

import lt.internal.pdfValidatorApplication.wordToPdfConverter.FilesStorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;

@SpringBootApplication
public class PdfValidatorApplication implements CommandLineRunner {

	@Resource
	FilesStorageService filesStorageService;


	public static void main(String[] args) {
		SpringApplication.run(PdfValidatorApplication.class, args);
	}

	@Override
	public void run(String... args) {
		filesStorageService.deleteUploadsFolder();
		filesStorageService.createDirectoryForUploadedFiles();
	}
}
