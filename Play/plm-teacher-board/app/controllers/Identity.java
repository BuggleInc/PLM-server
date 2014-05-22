package controllers;

import java.io.File;
import java.io.IOException;

import java.util.Calendar;
import java.util.Iterator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.jcraft.jsch.JSchException;

import play.mvc.Controller;
import play.mvc.Result;

import javax.persistence.PersistenceException;

import models.*;

public class Identity extends Controller {
	
	public static Result index() {
		return ok("You called the index method of the Git controller.");
	}

	public static Result linkIdentity(String username, String hashUUID, String mail) {
		String s;
		
		s = "Username : "+ username + "\nhashUUID : " + hashUUID +"\nMail : " + mail + "\n";
		
		System.out.println(s);
		
		Student stu = new Student(username, hashUUID, mail);
		try {
			stu.save();
		} catch(PersistenceException ex) {
			s += "\n"
				+ "ERROR executing DML bindLog[] error[Unique index or primary key violation: PRIMARY_KEY_B ON PUBLIC.STUDENT(UUID)"
				+ "\n";
		}
		
		return ok(
		views.html.students.render(Student.all())
		);

	}

}
