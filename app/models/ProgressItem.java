package models;

public class ProgressItem {
	
	public String lessonName, language;
	public int total, passed, percent;

	public ProgressItem(String lessonName, String language, int total, int passed) {
		this.lessonName = lessonName;
		this.language = language;
		this.total = total;
		this.passed = passed;
		this.percent =  passed*100/total;
	}
	
}