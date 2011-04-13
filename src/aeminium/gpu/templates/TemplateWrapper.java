package aeminium.gpu.templates;

import java.io.InputStream;

public class TemplateWrapper {
	InputStream is;
	String fname;

	public TemplateWrapper(String file) {
		fname = "templates/" + file;
		
		// Inside the jar, the path is absolute
		is = TemplateWrapper.class.getResourceAsStream("/" + fname);
	}

	public InputStream getInputStream() {
		return is;
	}

	public String getFileName() {
		return fname;
	}

	public String toString() {
		return fname;
	}

}