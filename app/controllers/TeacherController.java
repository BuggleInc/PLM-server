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

import java.util.Calendar;
import java.util.Iterator;
import java.util.Arrays;
import java.util.ArrayList;

import play.mvc.Controller;
import play.mvc.Result;

import models.*;

public class TeacherController extends Controller {
	
	public static Result createTeacher(String name) {
		Teacher teacher = new Teacher(name);
		
		Teacher.create(teacher);
		
		return ok(
		views.html.createTeacherOk.render()
	  );
	}
	
	public static Result deleteTeacher(String name) {
	  Teacher.delete(name, "");
	  return redirect(routes.Application.teachers());
	}

}
