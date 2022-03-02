package lt.internal.pdfValidatorApplication;

import lt.internal.pdfValidatorApplication.wordToPdfConverter.WordToPdfConverterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PdfValidatorApplication {

	private static final WordToPdfConverterService wordToPdfConverterService = new WordToPdfConverterService();


	public static void main(String[] args) {
		SpringApplication.run(PdfValidatorApplication.class, args);

		wordToPdfConverterService.deleteAll();
		wordToPdfConverterService.init();
	}
}
