package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import controllers.Identity;
import play.db.ebean.Model;

@Entity
public class Teacher extends Model {
	private static final long serialVersionUID = 1L;

	@Id
	public String name;

	public String password;

	public static Finder<String, Teacher> find = new Finder<String, Teacher>(String.class, Teacher.class);

	public Teacher(String name, String password) {
		this.name = name;
		this.password = password;
	}


	public static int count() {
		return find.findRowCount();
	}

	public static List<Teacher> all() {
		return find.all();
	}

	public static void create(Teacher teacher) {
		System.out.println("teacher :\n"
				+ "name : " + teacher.name);
		teacher.save();
	}

	public static void delete(String name, String s) {
		find.byId(name).delete();
	}


	public static Object authenticate(String login, String password) {
		return find.where().eq("name", login).eq("password", Identity.hashed(password)).findUnique();
	}

}
