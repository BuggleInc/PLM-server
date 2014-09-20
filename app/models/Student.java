package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import com.google.gson.JsonObject;

import play.db.ebean.Model;

@Entity
public class Student extends Model {
	private static final long serialVersionUID = 1L;

	@Id
	public String hashedUuid;

	public String uuid;

	public String name;

	public String mail;

	public static Finder<String, Student> find = new Finder<String, Student>(String.class, Student.class);

	public Student(String name, String mail, String hashedUuid, String uuid) {
		this.hashedUuid = hashedUuid;
		this.uuid = uuid;
		this.name = name;
		this.mail = mail;
	}


	public static Student authenticate(String uuid) {
		return find.where().eq("uuid", uuid).findUnique();
	}


	public static int count() {
		return find.findRowCount();
	}

	public static List<Student> all() {
		return find.all();
	}

	public static void create(Student student) {
		System.out.println("Student :\n"
				+ "name : " + student.name + "\n"
				+ "hashedUuid : " + student.hashedUuid
				+ "uuid : " + student.uuid);
		student.save();
	}

	public static void delete(String hashedUuid, String s) {
		find.byId(hashedUuid).delete();
	}

	public static void toJSON(JsonObject jsonObject, Student s) {
		jsonObject.addProperty("hashedUuid", s.hashedUuid);
		jsonObject.addProperty("uuid", s.uuid);
		jsonObject.addProperty("name", s.name);
		jsonObject.addProperty("mail", s.mail);
	}

    public String getName() {
        if(play.mvc.Http.Context.current().session().get("login") == null) {
            return "Anonymous ";
        }
        else {
            return name;
        }
    }

    public String getEmail() {
        if(play.mvc.Http.Context.current().session().get("login") == null) {
            return "email";
        }
        else {
            return mail;
        }
    }

}
