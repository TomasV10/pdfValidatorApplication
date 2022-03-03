package lt.internal.pdfValidatorApplication.pdfValidator;

import java.awt.image.BufferedImage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

public class MarginsOfPDFValidator {

	/*
	 * This is the main method of PDF page's top and bottom margins' validation.
	 */

	public void validate(PDFRenderer pdfRenderer, PDDocument document, int pageNr, ValidationErrors validationErrors) {
		try {
		    BufferedImage bim = pdfRenderer.renderImageWithDPI(pageNr, 300, ImageType.RGB);
		    int headerHeightInPixels = 201; // 17mm = 201pixels
		    int footerHeightInPixels = 118;	// 10mm = 118pixels
		    isPageMarginsValid(headerHeightInPixels, footerHeightInPixels, bim, pageNr, validationErrors);
//		    System.out.println(pageMarginsValid);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Validating page margins
	 */

	private void isPageMarginsValid(int headerHeightInPixels, int footerHeightInPixels,
							BufferedImage bufferedImage, int pageNum, ValidationErrors validationErrors) {

		boolean headerBlank = isHeaderBlank(headerHeightInPixels, bufferedImage);
		boolean footerBlank = isFooterBlank(footerHeightInPixels, bufferedImage);
		if(!headerBlank) validationErrors.addHeaderErrors(pageNum+1);
		if(!footerBlank) validationErrors.addFooterErrors(pageNum+1);
//		System.out.println(pageNum+1 + " header " + headerBlank);
//		System.out.println(pageNum+1 + " footer " + footerBlank);
	}

	/*
	 * Checking is header is blank.
	 */

	private boolean isHeaderBlank(int headerHeightInPixels, BufferedImage bufferedImage) {
		for(int y = 0; y < headerHeightInPixels; y++) {
			for(int x = 0; x < bufferedImage.getWidth(); x++) {
				int RGBA = bufferedImage.getRGB(x, y);
				int red = (RGBA >> 16) & 255;
				int green = (RGBA >> 8) & 255;
				int blue = RGBA & 255;
				if(red != 255 || green != 255 || blue != 255) {
					return false;
				}
			}
		}
		return true;
	}

	/*
	 * Checking is footer is blank.
	 */

	private boolean isFooterBlank(int footerHeightInPixels, BufferedImage bufferedImage) {
		for(int y = bufferedImage.getHeight() - footerHeightInPixels; y < bufferedImage.getHeight(); y++) {
			for(int x = 0; x < bufferedImage.getWidth(); x++) {
				int RGBA = bufferedImage.getRGB(x, y);
				int red = (RGBA >> 16) & 255;
				int green = (RGBA >> 8) & 255;
				int blue = RGBA & 255;
				if(red != 255 || green != 255 || blue != 255) {
					return false;
				}
			}
		}
		return true;
	}

}
