package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

import models.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("PLM Teacher Board will be here."));
    }

	public static Result deleteStudent(String uuid) {
	  Student.delete(uuid, "");
	  return redirect(routes.Application.students());
	}
	
	public static Result students() {
	  return ok(
		views.html.students.render(Student.all())
	  );
	}
}
