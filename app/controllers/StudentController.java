package controllers;

import java.util.ArrayList;

import models.Course;
import models.Student;
import play.mvc.Controller;
import play.mvc.Result;

public class StudentController extends Controller {
	
	public static Result joinCourse(String courseName, String uuid) {
		Course course = Course.find.byId(courseName);
		Student student = Student.find.byId(uuid);
		
		
		if(course.students == null) {
			course.students=new ArrayList<>();
		}
		//System.out.println("Before add in "+course.name);
		//System.out.println("size "+course.students.size());
		//for(Student s : course.students) {
		//	System.out.println("Student name : "+s.name);
		//}
		
		//System.out.println("student count : " +course.students.size());
		//System.out.println("Student name : "+student.name);
		
		student.courses.add(course);
		course.students.add(student);
		
		student.save();
		student.saveManyToManyAssociations("courses");
		course.save();	
		course.saveManyToManyAssociations("students");
		course.saveManyToManyAssociations("teachers");		
		
		//System.out.println("After add");
		//for(Student s : course.students) {
		//	System.out.println("Student name : "+s.name);
		//}
		
		return redirect(routes.StudentController.student(uuid));
	}
	
	public static Result student(String uuid) {
		return ok(
			views.html.student.render(Student.find.byId(uuid), Course.all())
		);
	}

	public static Result deleteStudent(String uuid) {
	  Student.delete(uuid, "");
	  return redirect(routes.Application.students());
	}
	
		
	public static Result leaveCourse(String name, String uuid) {
		Course course = Course.find.byId(name);
		Student student = Student.find.byId(uuid);
		
		course.students.remove(student);
		student.courses.remove(course);
		
		course.save();
		course.saveManyToManyAssociations("students");
		course.saveManyToManyAssociations("teachers");
		student.save();
		student.saveManyToManyAssociations("courses");
		
		return redirect(routes.StudentController.student(uuid));
	}

}
