package lt.internal.pdfValidatorApplication.wordToPdfConverter;

public class ConversionsCounter {
	private int successfulConversion = 0;
	private	int failedConversion = 0;
	
	
	public int getSuccessfulConversion() {
		return successfulConversion;
	}
	public void setSuccessfulConversion(int successfulConversion) {
		this.successfulConversion = successfulConversion;
	}
	public int getFailedConversion() {
		return failedConversion;
	}
	public void setFailedConversion(int failedConversion) {
		this.failedConversion = failedConversion;
	}
	
	
	public int increaseSuccessfulConversionsCount() {
		return successfulConversion++;
	}
	public int increaseFailedConversionsCount() {
		return failedConversion++;
	}
}
