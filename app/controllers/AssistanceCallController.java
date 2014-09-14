package controllers;

import javax.persistence.EntityNotFoundException;

import models.AssistanceCall;
import models.Student;
import play.libs.Json;
import play.mvc.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AssistanceCallController extends Controller {

	@BodyParser.Of(BodyParser.Json.class)
	public static Result handle() {
		JsonNode json = request().body().asJson();
		ObjectNode result = Json.newObject();
		String uuid = json.findPath("uuid").textValue(),
				hostname = json.findPath("hostname").textValue(),
				date = json.findPath("date").textValue(),
				details = json.findPath("details").textValue(),
				action = json.findPath("action").textValue();
		if (uuid == null) {
			result.put("status", "KO");
			result.put("message", "Missing parameter [uuid]");
			return ok(result);
		} else {
			if (action.equals("add")) { // student ask for help
				try {
					Student.find.ref(Identity.hashed(uuid)).name.length(); // to throw an EntityNotFoundException if Student not in database
					if (hostname == null) {
						hostname = "No hostname";
					}
					if (date == null) {
						date = "No date";
					}
					if (details == null) {
						details = "No details";
					}
					//System.out.println("uuid : " + uuid + " ; hostname : "+ hostname +" ; date : " + date + " ; details : " + details);
					AssistanceCall assistanceCall = new AssistanceCall(hostname, date, details, uuid);
					AssistanceCall.create(assistanceCall);
					//System.out.println("------------Controller---------------------------"+assistanceCall.id);
					result.put("status", "OK");
					result.put("callID", assistanceCall.id);
					return ok(result); // send the CallID back to the PLM (allow student to cancel it
				} catch (EntityNotFoundException ex) { // student not in database
					//System.out.println("EntityNotFoundException -- sending bad request");
					result.put("status", "KO");
					result.put("message", "You haven't linked your identity");
					return ok(result);
				}
			} else if (action.equals("remove")) { // student cancel call for help
				String callID = json.findPath("callID").textValue();
				//System.out.println("Call ID to remove ------------ : " + callID);
				AssistanceCallController.delete(callID);
				result.put("status", "OK");
				result.put("message", "Call canceled");
				return ok(result);
			}

		}
		return TODO;
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
