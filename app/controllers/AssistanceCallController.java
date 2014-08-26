package controllers;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;

import models.AssistanceCall;
import models.Student;
import play.mvc.*;

import com.fasterxml.jackson.databind.JsonNode;

public class AssistanceCallController extends Controller {
	
	public static Result handle() {
		//System.out.println("Handle !!!");
		JsonNode json = request().body().asJson();
		if (json == null) {
			System.out.println("Expecting Json data");
			return badRequest("Expecting Json data");
		} else {
			String uuid = json.findPath("uuid").textValue();
			String hostname = json.findPath("hostname").textValue();
			String date = json.findPath("date").textValue();
			String details = json.findPath("details").textValue();
			
			if (uuid == null) {
				uuid = "";
			}
			if (hostname == null) {
				hostname = "No hostname";
			}
			if (date == null) {
				date = "No date";
			}
			if (details == null) {
				details = "No details";
			}
			System.out.println("uuid : " + uuid + " ; hostname : "+ hostname +" ; date : " + date + " ; details : " + details);
			AssistanceCall assistanceCall = new AssistanceCall(hostname, date, details, uuid);
			assistanceCall.create(assistanceCall);
			return ok("Hello " + uuid);
		}
	}
	
	@Security.Authenticated(Secured.class)
	public static Result getCallHelp() {
	  return ok(
		views.html.assistanceCall.render(AssistanceCall.all())
	  );
	}
	
	@Security.Authenticated(Secured.class)
	public static Result delete(String id) {
		  AssistanceCall.delete(id, "");
		  return redirect(routes.AssistanceCallController.getCallHelp());
		}
}
