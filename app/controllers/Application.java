package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

import models.*;

import java.util.List;
import play.libs.Yaml;
import com.avaje.ebean.Ebean;

public class Application extends Controller {

    public static Result index() {
        return ok(views.html.home.render());
    }
	
	public static Result init() {
		Ebean.save((List) Yaml.load("test-data.yml"));
	  return ok(
		views.html.students.render(Student.all())
	  );
	}
	
	public static Result students() {
	  return ok(
		views.html.students.render(Student.all())
	  );
	}
	
	public static Result courses() {
	  return ok(
		views.html.courses.render(Course.all())
	  );
	}
	
	public static Result teachers() {
	  return ok(
		views.html.teachers.render(Teacher.all())
	  );
	}
	
	public static Result createCourse() {
	  return ok(
		views.html.createCourse.render(Teacher.all())
	  );
	}
	public static Result createTeacher() {
	  return ok(
		views.html.createTeacher.render()
	  );
	}
}
