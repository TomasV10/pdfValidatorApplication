package lt.internal.pdfValidatorApplication.pdfValidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lt.internal.pdfValidatorApplication.wordToPdfConverter.PdfMessages;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PdfValidatorService {

	private static final String ALLOWED_EXTENSION = ".pdf";
	private static final int ALLOWED_NUMBER_OF_PAGES = 349;
	private static final int A4_FORMAT_WIDTH = 595;
	private static final int A4_FORMAT_HEIGHT = 842;

	public static List<PdfMessages> validatePdfDocuments(Path baseDir) {
//		if(!WordToPdfConverterService.isBaseDirectoryValid(baseDir)) return;
		long start = System.currentTimeMillis();
		try  {
			List<PdfMessages>messagesList =
				Files.walk(baseDir, 1)
					.filter(p -> !Files.isDirectory(p))
					.map(p -> p.toString().toLowerCase())
					.filter(f -> f.endsWith(ALLOWED_EXTENSION))
					.map(file -> validatePdfDocument(file))
						.collect(Collectors.toList());
		return messagesList;
       }catch (IOException e) {
        	throw new IllegalArgumentException(e);
		}finally {
			long end = System.currentTimeMillis();
			System.out.println("Time required:" + (end-start) / 1000 + "s");
		}
	}

	/*
	 * Validates PDF document according to specified requirements
	 */

	public static PdfMessages validatePdfDocument(String path) {
		PdfMessages msg = new PdfMessages(getFileNameWithoutPath(path));
		Set<String>notEmbeddedFontList = new HashSet<>();
		List<Integer>pageNumbers = new ArrayList<>();
		ValidationErrors validationErrors = new ValidationErrors();
		MarginsOfPDFValidator marginValidator = new MarginsOfPDFValidator();
		System.out.println("PDF document validation started...");
		try {
			System.out.println("Loading PDF document " + path);
			PDDocument document = PDDocument.load(new File(path));
			PDFRenderer pdfRenderer = new PDFRenderer(document);

			 for(int pageNr = 0; pageNr < document.getNumberOfPages(); pageNr++) {

				 PDPage page = retrievePage(document, pageNr);
				 PDResources resources = getEachPageResources(page);

				 collectNotEmbeddedFonts(resources, notEmbeddedFontList);

				 if(validatePageFormat(pageNumbers, page, pageNr)) {
				 marginValidator.validate(pdfRenderer, document, pageNr, validationErrors);
				 }
			 }
			 writeResultsToTxtFile(document, msg, notEmbeddedFontList, pageNumbers, validationErrors);

			 document.close();
			 return msg;
		} catch (IOException e) {
			msg.addMessage("Error validating PDF:" + e.getMessage());
			return msg;
		}
	}

	/*
	 * This method is responsible for result printing.
	 */

	private static void writeResultsToTxtFile(PDDocument document, PdfMessages msg, Set<String> notEmbeddedFontList,
															List<Integer> pageNumbers, ValidationErrors validationErrors) {

		if(notEmbeddedFontList.isEmpty() && pageNumbers.isEmpty() && validationErrors.getHeader().isEmpty()
				&& validationErrors.getFooter().isEmpty()) {
			 msg.addMessage("Dokumentas atitiko reikalavimus");
			 msg.setStatusCode(HttpStatus.OK.value());
		 }else {
			 msg.addMessage("Dokumentas reikalavimų neatitiko");
			 msg.setStatusCode(HttpStatus.BAD_REQUEST.value());
			 validatePdfDocumentNumberOfPages(document, msg);
			 printNotEmbeddedFontList(notEmbeddedFontList, msg);
			 printPageNumbersWhichHasBadFormat(validationErrors, pageNumbers, msg);

		 }
	}

	/*
	 * Deletes path of file, leaving file name
	 */

	private static String getFileNameWithoutPath(String path) {
		return path.replaceAll("^.*[\\/\\\\]", "");
	}


	/*
	 * Prints pages numbers of bad format pages to .txt file
	 * If page format is good then checks top and bottom margins.
	 */

	private static void printPageNumbersWhichHasBadFormat(ValidationErrors validationErrors,
															List<Integer> pageNumbers, PdfMessages msg) {
		if(!pageNumbers.isEmpty()) {
			msg.addMessage("Klaida! Puslapiai " +  pageNumbers.toString()
		 		.replace("[", "").replace("]", "")
		 		+ " neatitinka formato. Kadangi puslapis neatitinka formato, negaliu patikrinti puslapio paraščių");
		}else printPagesWhichHasBadTopOrBottomMargins(validationErrors, msg);
	}

	/*
	 * Method prints error message to .txt file
	 */

	private static void printPagesWhichHasBadTopOrBottomMargins(ValidationErrors validationErrors, PdfMessages msg) {

		if(!validationErrors.getHeader().isEmpty()) {
			msg.addMessage("Klaida! Puslapių " + validationErrors.getHeader().toString() + " viršutinė paraštė neatitinka formato");
		}if(!validationErrors.getFooter().isEmpty()) {
			msg.addMessage("Klaida! Puslapių " + validationErrors.getFooter().toString() + " apatinė paraštė neatitinka formato");
		}
	}

	/*
	 * Returns PDPage object
	 */

	private static PDPage retrievePage(PDDocument document, int pageNr) {
		return document.getPage(pageNr);
	}

	/*
	 * Returns each page resources
	 */

	private static PDResources getEachPageResources(PDPage page) {
		return page.getResources();
	}

	/*
	 * 	Page format validation.
	 *  If format is not valid, page number is collected to the Integer list.
	 */


	private static boolean validatePageFormat(List<Integer>pageNumbers, PDPage page, int pageNr) throws IOException {
		int width = Math.round(page.getMediaBox().getWidth());
		int height = Math.round(page.getMediaBox().getHeight());

		if(width != A4_FORMAT_WIDTH && height != A4_FORMAT_HEIGHT
									&& width != A4_FORMAT_HEIGHT
									&& height != A4_FORMAT_WIDTH) {

			pageNumbers.add(pageNr + 1);
			return false;
		}
		return true;
	}

	/*
	 * Method collects NOT embedded fonts to the list (SET)
	 */

	private static Set<String> collectNotEmbeddedFonts(PDResources resources, Set<String>notEmbeddedFontList)
																							throws IOException {
		for (COSName key : resources.getFontNames())
		    {
		        PDFont font = resources.getFont(key);
		        if(!font.isEmbedded()) {
		        	notEmbeddedFontList.add(font.getName());
		        }
		    }
		return notEmbeddedFontList;
	}

	/*
	 * Printing results of pages which fonts hasn't been embedded to file .txt
	 */

	private static void printNotEmbeddedFontList(Set<String> notEmbeddedFontList, PdfMessages msg) {
		if(!notEmbeddedFontList.isEmpty()) msg.addMessage("Klaida! Ne visi naudojami šriftai yra įkelti į PDF dokumentą. Neįkelti šriftai: ");
		notEmbeddedFontList.forEach(font -> msg.addMessage(font));

	}

	/*
	 * Checking PDF document how many pages does it has.
	 * If more than the number of pages specified in the requirements throws an error.
	 */

	private static void validatePdfDocumentNumberOfPages(PDDocument document, PdfMessages msg) {
		if(document.getNumberOfPages() > ALLOWED_NUMBER_OF_PAGES) {

			msg.addMessage("Klaida! PDF dokumentas turi per daug puslapių! Puslapių skaičius: "
	 						+ document.getNumberOfPages()
	 						+ "Puslapių skaičius negali būti didesnis nei " + ALLOWED_NUMBER_OF_PAGES);
		 }
	}

}


