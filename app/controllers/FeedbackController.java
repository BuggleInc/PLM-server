package controllers;

import com.avaje.ebean.Ebean;

import models.Student;
import models.Feedback;

import org.eclipse.jgit.api.errors.GitAPIException;

import play.*;
import play.data.*;
import play.mvc.*;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import static play.data.Form.*;

public class FeedbackController extends Controller {

	public static Result feedbacks() {
		List<Feedback> feedbacks = new ArrayList<>();
		try {
			feedbacks = JGit.getFeedBack(Student.all());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		return ok(views.html.feedbacks.render(feedbacks));
	}
}
