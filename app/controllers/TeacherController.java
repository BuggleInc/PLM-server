package controllers;

import models.Course;
import models.Teacher;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

@Security.Authenticated(Secured.class)
public class TeacherController extends Controller {

	public static Result createTeacher(String name, String password) {
		Teacher teacher = new Teacher(name, Identity.hashed(password));

		Teacher.create(teacher);
		flash("success", "Teacher created");
		return ok(
				views.html.teachers.render(Teacher.all(), Course.all())
		);
	}

	public static Result deleteTeacher(String name) {
		Teacher.delete(name, "");
		return redirect(routes.TeacherController.teachers());
	}

	public static Result teachers() {
		return ok(
				views.html.teachers.render(Teacher.all(), Course.all())
		);
	}

	public static Result createTeacherForm() {
		return ok(
				views.html.createTeacher.render()
		);
	}

}
