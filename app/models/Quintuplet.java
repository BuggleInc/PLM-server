package models;

public class Quintuplet {

	public String time, total, failed, codeLink, errorLink;


	public Quintuplet(String time, String total, String failed, String codeLink, String errorLink) {
		this.time = time;
		this.total = total;
		this.failed = failed;
		this.codeLink = codeLink;
		this.errorLink = errorLink;
	}
}
