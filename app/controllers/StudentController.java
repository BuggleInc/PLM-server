package controllers;

import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.IOException;

import models.Course;
import models.Student;

import play.mvc.*;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

@Security.Authenticated(Secured.class)
public class StudentController extends Controller {
	
	public static Result students() {
		JGit.fetchRepo();
		ArrayList<String> lastActivity = new ArrayList<>();
		List<Student> students = Student.all();
        try {
            JGit.getLastActivity(students, lastActivity);
        } catch (IOException|GitAPIException e) {
            //lastActivity.add("0");
        }
		return ok(
			views.html.students.render(students, lastActivity)
		);
	}
	
	public static Result allStudents() {
		ArrayList<String> lastActivity = new ArrayList<>();
		List<Student> students = StudentController.getAllStudents();
        try {
            JGit.getLastActivity(students, lastActivity);
        } catch (IOException|GitAPIException e) {
            //lastActivity.add("0");
        }
		  return ok(
			views.html.studentsAll.render(students, lastActivity)
		  );
		}
	
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

		course.students.add(student);
		
		student.save();
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

	public static Result deleteStudent(String hashedUuid) {
        Student.delete(hashedUuid, "");
        return redirect(routes.StudentController.students());
	}
	
		
	public static Result leaveCourse(String name, String uuid) {
		Course course = Course.find.byId(name);
		Student student = Student.find.byId(uuid);
		
		course.students.remove(student);
		
		course.save();
		course.saveManyToManyAssociations("students");
		course.saveManyToManyAssociations("teachers");
		student.save();
		
		return redirect(routes.StudentController.student(uuid));
	}

	public static List<Student> getAllStudents() {
		ArrayList<Student> students = new ArrayList<>();
		JGit.fetchRepo();
		try {
			File localPath = new File("repo/");
			
			Repository repository = FileRepositoryBuilder.create(new File(localPath+"/.git"));
			Git git = new Git(repository);
			
			List<Ref> call = git.branchList().setListMode(ListMode.ALL).call();
			String branchName = "", refName = "";
			for (Ref ref : call) { // for each remote branch
				refName = ref.getName();
				//System.out.println(refName);
				if(refName.contains("refs/remotes/") && !refName.contains("master")) {
					branchName = refName.substring(23); // remove "refs/remotes/origin/PLM"
					//System.out.println(branchName);
					if(!branchName.equals("master")) {
						students.add(new Student(branchName.substring(0,10), "unknown", branchName, ""));
					}
				}
			}
		} catch (IOException|GitAPIException ex) {
		}
		return students;
	}
}
