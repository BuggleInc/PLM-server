package controllers;

import java.util.List;

import models.Course;
import models.Student;
import models.Teacher;
import play.libs.Yaml;
import play.mvc.Controller;
import play.mvc.Result;

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
	
	public static Result allStudents() {
	  return ok(
		views.html.studentsAll.render(StudentController.getAllStudents())
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
