package lt.internal.pdfValidatorApplication.pdfValidator;

import java.util.HashSet;
import java.util.Set;

public class ValidationErrors {

	private Set<Integer> header = new HashSet<>();
	private Set<Integer> footer = new HashSet<>();


	public Set<Integer> addHeaderErrors(int pageNr){
		header.add(pageNr);
		return header;
	}

	public Set<Integer> addFooterErrors(int pageNr){
		footer.add(pageNr);
		return footer;
	}

	public Set<Integer> getHeader() {
		return header;
	}
	public void setHeader(Set<Integer> header) {
		this.header = header;
	}
	public Set<Integer> getFooter() {
		return footer;
	}
	public void setFooter(Set<Integer> footer) {
		this.footer = footer;
	}



}
