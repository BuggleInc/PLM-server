package controllers;

import java.io.IOException;
import java.util.ArrayList;

import models.Commit;
import models.Student;
import models.Quintuplet;

import org.eclipse.jgit.api.errors.GitAPIException;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

@Security.Authenticated(Secured.class)
public class ExerciseController extends Controller {

	public static Result display(String hashedUuid, String exerciseName, int limit) {
		//System.out.println("Display exercise "+exerciseName+ " for " + hashedUuid);
		Student student = Student.find.byId(hashedUuid);
		if (student == null) {
			student = new Student(hashedUuid.substring(0, 10), "mail@mail.mail", hashedUuid, "");
		}
		//System.out.println("Student name : " + student.name);
		ArrayList<Quintuplet> quintuplet = new ArrayList<>();
		ArrayList<Commit> commits = new ArrayList<>();
		try {
			commits = JGit.computeCommits(hashedUuid);
		} catch (IOException | GitAPIException e) { // TODO
		}
		for (Commit c : commits) {
			if (quintuplet.size() < limit && (c.evt_type.equals("Success") || c.evt_type.equals("Failed")) && c.exoname.equals(exerciseName)) {
				if (c.totaltests.equals("-1")) { // compilation error

				}
				quintuplet.add(new Quintuplet(c.commitTime, c.passedtests, c.totaltests, c.codeLink, c.errorLink));
			}
		}
		return ok(
				views.html.exerciseGraph.render(
						student,
						quintuplet,
						exerciseName
				)
		);
	}

}
