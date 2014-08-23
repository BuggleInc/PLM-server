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

@Security.Authenticated(Secured.class)
public class CourseController extends Controller {

	public static Result course(String name) {
		Course course =  Course.find.byId(name); // get the course from the db
		List<Student> students = course.students; // get the students
		ArrayList<String> studentsNames = new ArrayList<>(); // store their name to display on the view
		for(Student s : students) {
			studentsNames.add(s.uuid);
		}
		ArrayList<ProgressItem> summary = null;
		try {
			summary = JGit.computeStudentForLesson(studentsNames, course); // compute progression of each student for the current lesson
		} catch(IOException|GitAPIException ex) {
			System.out.println(ex);
		}

		return ok(
			views.html.course.render(
			Course.find.byId(name),
			summary
			)
		);
	}
	
	public static Result addTeacher(String courseName, String teacherName) {
		Teacher teacher = Teacher.find.byId(teacherName);
		Course course = Course.find.byId(courseName);
		Course.addTeacher(course, teacher);
		return ok(
				views.html.courses.render(Course.all())
			  );
	}
	
	public static Result createCourse(String name, String teacherName, String displayName, String programmingLanguage) {
		Teacher teacher = Teacher.find.byId(teacherName);
		// System.out.println("Teacher name :"+teacher.name);
		
		Course course = new Course(name);
		course.displayName = displayName;
		course.programmingLanguage = programmingLanguage;
		
		course.teachers.add(teacher);
		teacher.courses.add(course);
		course.save();
		course.saveManyToManyAssociations("students");
		course.saveManyToManyAssociations("teachers");
		teacher.save();	
		teacher.saveManyToManyAssociations("courses");

		return redirect(routes.Application.courses()); // redirect page

	}
	
	public static Result deleteCourse(String name) {
		Course course = Course.find.byId(name);
		for(Teacher t : course.teachers) {
			Teacher teacher = Teacher.find.byId(t.name);
			teacher.courses.remove(course);
			teacher.saveManyToManyAssociations("courses");
		}
		for(Student s : course.students) {
			Student student = Student.find.byId(s.uuid);
			student.courses.remove(course);
			student.saveManyToManyAssociations("courses");
		}
		Course.delete(name, "");
		return redirect(routes.Application.courses());
	}

}
