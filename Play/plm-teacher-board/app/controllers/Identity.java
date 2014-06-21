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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.persistence.PersistenceException;

import models.*;

public class Identity extends Controller {
	
	public static Result index() {
		return ok("You called the index method of the Git controller.");
	}

	public static Result linkIdentity(String username, String UUID, String mail) {
		String s;
		
		s = "Username : "+ username + "\nUUID : " + UUID +"\nMail : " + mail + "\n";
		
		System.out.println(s);
		
		String hashUUID = "";
		
		try {
			hashUUID = sha1(UUID);
		} catch (NoSuchAlgorithmException ex) {
			//System.err.println(ex.getMessage());
		}
		
		Student stu = new Student(username, mail, hashUUID);
		try {
			stu.save();
			stu.saveManyToManyAssociations("courses");
		} catch(PersistenceException ex) {
			s += "\n"
				+ "ERROR executing DML bindLog[] error[Unique index or primary key violation: PRIMARY_KEY_B ON PUBLIC.STUDENT(UUID)"
				+ "\n";
		}
		
		return ok(
			//views.html.students.render(Student.all())
			views.html.linkOk.render()
			);
	}
	
	public static Result linkForm(String UUID) {
		return ok(
			views.html.identity.render(UUID)
			);
	}
	
		// Helper methods
	private static String sha1(String input) throws NoSuchAlgorithmException {
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		byte[] result = mDigest.digest(input.getBytes());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < result.length; i++) {
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
		}

		return sb.toString();
	}

}
