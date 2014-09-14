package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import controllers.Identity;
import play.db.ebean.Model;

@Entity
public class AssistanceCall extends Model {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public String id;

	public String hostname;

	public String date;

	public String details;

	@ManyToOne
	public Student student;


	public static Finder<String, AssistanceCall> find = new Finder<String, AssistanceCall>(String.class, AssistanceCall.class);

	public AssistanceCall(String hostname, String date, String details, String uuid) {
		String hashedUuid = Identity.hashed(uuid);
		//System.out.println("----!!!!!!!!!!-------- UUID : " + uuid +" ; hashed : " + hashedUuid);
		this.student = Student.find.ref(hashedUuid);
		this.hostname = hostname;
		this.date = date;
		this.details = details;
	}

	public static int count() {
		return find.findRowCount();
	}

	public static List<AssistanceCall> all() {
		return find.all();
	}

	public static void create(AssistanceCall assistanceCall) {
		assistanceCall.student = Student.find.ref(assistanceCall.student.hashedUuid);
//		System.out.println("Assistance call :\n"
//				+ "hashedUuid : " + assistanceCall.student.hashedUuid +"\n"
//				+ "details : "+ assistanceCall.details+"\n"
//				+ "hostname : "+ assistanceCall.hostname);
		assistanceCall.save();
	}

	public static void delete(String id, String s) {
		find.byId(id).delete();
	}

}
