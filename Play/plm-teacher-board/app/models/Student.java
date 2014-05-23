package models;

import java.util.*;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.*;
import play.data.validation.Constraints.*;

import play.db.ebean.Model;


@Entity
public class Student extends Model {

	@Id
	public String uuid;
	
	public String name;
	
	public String mail;

	public static Finder<String, Student> find = new Finder<String, Student>(String.class, Student.class);

	public Student(String name, String mail, String uuid) {
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
				+ "name : " + student.name +"\n"
				+ "uuid : "+ student.uuid);
	  student.save();
	}
	
	public static void delete(String uuid, String s) {
	  find.ref(uuid).delete();
	}
	
}
