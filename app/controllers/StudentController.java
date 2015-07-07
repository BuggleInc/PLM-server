package controllers;

import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import models.Course;
import models.Student;

import play.mvc.*;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class StudentController extends Controller {

	public static Result students() {
		JGit.fetchRepoOnDemand(); // fetch to retrieve last info from remote repo
		ArrayList<String> lastActivity = new ArrayList<>();
		List<Student> students = Student.all();
		try {
			lastActivity = JGit.getLastActivity(students);
		} catch (IOException | GitAPIException e) {
			//System.out.println(e);
		}
		return ok(
				views.html.students.render(students, lastActivity)
		);
	}

	public static Result allStudents() {
		ArrayList<String> lastActivity = new ArrayList<>();
		List<Student> students = StudentController.getAllStudents();
		try {
			lastActivity = JGit.getLastActivity(students);
		} catch (IOException | GitAPIException e) {
			//lastActivity.add("0");
		}
		return ok(
				views.html.studentsAll.render(students, lastActivity)
		);
	}

    @Security.Authenticated(Secured.class)
	public static Result joinCourse(String courseName, String uuid) {
		Course course = Course.find.byId(courseName);
		Student student = Student.find.byId(uuid);


		if (course.students == null) {
			course.students = new ArrayList<>();
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

    @Security.Authenticated(Secured.class)
	public static Result student(String uuid) {
		return ok(
				views.html.student.render(Student.find.byId(uuid), Course.all())
		);
	}

    @Security.Authenticated(Secured.class)
	public static Result deleteStudent(String hashedUuid) {
		Student.delete(hashedUuid, "");
		return redirect(routes.StudentController.students());
	}


    @Security.Authenticated(Secured.class)
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
		JGit.fetchRepoOnDemand();
		try {
			File localPath = new File("repo/");

			Repository repository = FileRepositoryBuilder.create(new File(localPath + "/.git"));
			Git git = new Git(repository);

			List<Ref> call = git.branchList().setListMode(ListMode.ALL).call();
			String branchName = "", refName = "";
			for (Ref ref : call) { // for each remote branch
				refName = ref.getName();
				//System.out.println(refName);
				if (refName.contains("refs/remotes/") && !refName.contains("master")) {
					branchName = refName.substring(23); // remove "refs/remotes/origin/PLM"
					//System.out.println(branchName);
					if (!branchName.equals("master")) {
						students.add(new Student(branchName.substring(0, 10), "unknown", branchName, ""));
					}
				}
			}
		} catch (IOException | GitAPIException ex) {
		}
		return students;
	}

    @Security.Authenticated(Secured.class)
	public static Result export() {
		// build json file and then send it to client
		JsonObject jsonObject, jsonRoot = new JsonObject();
		JsonArray jsonArray = new JsonArray();
		for(Student s : Student.all()) {
			jsonObject = new JsonObject();
			Student.toJSON(jsonObject, s);
			jsonArray.add(jsonObject);
		}
		jsonRoot.add("Students", jsonArray);
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		try {
			Files.write(Paths.get("students.json"), gson.toJson(jsonRoot).getBytes());
			return ok(new java.io.File("students.json"));
		}catch(IOException ex) {
			return Application.index();
		}
	}
}
