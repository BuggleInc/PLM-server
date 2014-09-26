package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.Course;
import models.ProgressItem;
import models.Student;
import models.Teacher;

import org.eclipse.jgit.api.errors.GitAPIException;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

public class CourseController extends Controller {

	public static Result course(String name) {
		Course course = Course.find.byId(name); // get the course from the db
		List<Student> students = course.students; // get the students
		ArrayList<String> hasheds = new ArrayList<>(); // store their name to display on the view
		for (Student s : students) {
			hasheds.add(s.hashedUuid);
		}
		ArrayList<ProgressItem> summary = null;
		try {
			summary = JGit.computeStudentForLesson(hasheds, course); // compute progression of each student for the current lesson
		} catch (IOException | GitAPIException ex) {
			System.out.println(ex);
		}

		return ok(
				views.html.course.render(
						Course.find.byId(name),
						summary
				)
		);
	}

	public static Result courses() {
		return ok(
				views.html.courses.render(Course.all())
		);
	}

    @Security.Authenticated(Secured.class)
	public static Result createCourseForm() {
		return ok(
				views.html.createCourse.render(Teacher.all())
		);
	}

    @Security.Authenticated(Secured.class)
	public static Result addTeacher(String courseName, String teacherName) {
		Teacher teacher = Teacher.find.byId(teacherName);
		Course course = Course.find.byId(courseName);
		Course.addTeacher(course, teacher);
		return ok(
				views.html.courses.render(Course.all())
		);
	}

    @Security.Authenticated(Secured.class)
	public static Result createCourse(String name, String teacherName, String displayName, String programmingLanguage) {
		Teacher teacher = Teacher.find.byId(teacherName);
		// System.out.println("Teacher name :"+teacher.name);

		Course course = new Course();
		course.name = name;
		course.displayName = displayName;
		course.programmingLanguage = programmingLanguage;

		course.teachers.add(teacher);
		course.save();
		course.saveManyToManyAssociations("students");
		course.saveManyToManyAssociations("teachers");
		teacher.save();

		return redirect(routes.CourseController.courses()); // redirect page

	}

    @Security.Authenticated(Secured.class)
	public static Result deleteCourse(String name) {
		Course course = Course.find.byId(name);
		Course.delete(name, "");
		return redirect(routes.CourseController.courses());
	}

    @Security.Authenticated(Secured.class)
	public static Result addAllStudentToCourse(String courseID) {
		Course course = Course.find.byId(courseID);
		for(Student s : Student.all()) {
		    course.students.add(s);
		}
		course.saveManyToManyAssociations("students");
		flash("success", "Students added to " + courseID + " course");
		return controllers.CourseController.course(courseID);
	}

}
