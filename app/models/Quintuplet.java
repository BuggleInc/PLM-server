package models;

public class Quintuplet {
	
	public String time, passed, failed, codeLink, errorLink;
	
	
	public Quintuplet(String time, String passed, String failed, String codeLink, String errorLink) {
		this.time = time;
		this.passed = passed;
		this.failed = failed;
		this.codeLink = codeLink;
		this.errorLink = errorLink;
	}
}
