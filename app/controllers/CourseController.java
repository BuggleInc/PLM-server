package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;

import java.util.*;

import play.mvc.Controller;
import play.mvc.Result;

import play.api.data.*;
import play.api.data.Forms.*;

import models.*;

import java.io.IOException;
import org.eclipse.jgit.api.errors.*
;
public class CourseController extends Controller {

	public static Result course(String name) {
		final String lessonName = name;
		Course course =  Course.find.byId(name);
		List<Student> students = course.students;
		ArrayList<String> studentsName = new ArrayList<>();
		for(Student s : students) {
			studentsName.add(s.uuid);
		}
		ArrayList<ProgressItem> summary = null;
		try {

			summary = JGit.computeStudentForLesson(studentsName, lessonName);
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
	
	public static Result createCourse(String name, String teacherName) {
		Teacher teacher = Teacher.find.byId(teacherName);
		System.out.println("Teacher name :"+teacher.name);
		Course course = new Course(name);
		
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
