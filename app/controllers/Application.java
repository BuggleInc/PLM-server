package controllers;

import java.util.List;

import models.Course;
import models.Student;
import models.Teacher;
import play.libs.Yaml;
import play.mvc.Controller;
import play.mvc.Result;

import play.*;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;

import models.*;
import views.html.*;
import com.avaje.ebean.Ebean;

public class Application extends Controller {

	@Security.Authenticated(Secured.class)
    public static Result index() {
        return ok(views.html.home.render(request().username()));
    }
	
	public static Result init() {
		Ebean.save((List) Yaml.load("test-data.yml"));
		CourseController.addTeacher("maze", "Teacher1");
		CourseController.addTeacher("maze", "Teacher2");
		CourseController.addTeacher("PPP", "Teacher1");
		session().clear();
		flash("success", "Database fill with test data");
	  	return login();
	}
	
	public static Result login() {
	    return ok(
	     views.html.login.render(form(Login.class))
	    );
	}
	
	public static Result logout() {
	    session().clear();
	    flash("success", "You've been logged out");
	    return redirect(
	        routes.Application.login()
	    );
	}
	
	public static Result authenticate() {
	    Form<Login> loginForm = form(Login.class).bindFromRequest();
	    if (loginForm.hasErrors()) {
	        return badRequest(views.html.login.render(loginForm));
	    } else {
	        session().clear();
	        session("login", loginForm.get().login);
	        return redirect(
	            routes.Application.index()
	        );
	    }

	}
	
	@Security.Authenticated(Secured.class)
	public static Result courses() {
	  return ok(
		views.html.courses.render(Course.all())
	  );
	}
	
	@Security.Authenticated(Secured.class)
	public static Result teachers() {
	  return ok(
		views.html.teachers.render(Teacher.all())
	  );
	}
	
	@Security.Authenticated(Secured.class)
	public static Result createCourse() {
	  return ok(
		views.html.createCourse.render(Teacher.all())
	  );
	}
	
	@Security.Authenticated(Secured.class)
	public static Result createTeacher() {
	  return ok(
		views.html.createTeacher.render()
	  );
	}
	
	public static class Login {

	    public String login;
	    public String password;
	    
	    public String validate() {
	        if (Teacher.authenticate(login, password) == null) {
	          return "Invalid user or password";
	        }
	        return null;
	    }

	}
}
