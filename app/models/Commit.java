/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package models;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * @author Ced
 */
public class Commit {

	public String exolang, exoswitchto, evt_type, evt_class, totaltests="", outcome,
			passedtests="", exoname, commitTime, comment, os, plm_version, java_version, codeLink, errorLink;

	public Commit(String json, int commitTime, String commitID) {
		this.commitTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date(commitTime * 1000L));

		evt_type = "";
		evt_class = ""; // to color the row in table (view) (bootstrap class)
		comment = "";
		exolang = "";
		outcome = "";

		try {
			JsonParser jsonParser = new JsonParser();
			JsonObject jo = (JsonObject) jsonParser.parse(json);
			//System.out.println(jo.get("evt_type").getAsString());
			try {
				switch (jo.get("kind").getAsString()) {
					case "switched":
						evt_class = "warning";
						evt_type = "Switched";
						break;
					case "reverted":
						evt_class = "warning";
						evt_type = "Reverted";
						break;
					case "executed":
						try {
							try { // old commit log format does not contain outcome entry
								outcome = jo.get("outcome").getAsString();
							} catch (Exception ex) {
							}
							exolang = jo.get("lang").getAsString();
							if (outcome.equals("pass")) {
								evt_class = "success";
								evt_type = "Success";
							} else if (outcome.equals("fail")) {
								evt_class = "danger";
								evt_type = "Failed";
							} else if (outcome.equals("compile")) {
								evt_class = "danger";
								evt_type = "Compilation error";
							}
							if (jo.get("totaltests") != null)
								totaltests = jo.get("totaltests").getAsString(); // not present if outcome is "compile"
							if (jo.get("passedtests") != null)
								passedtests = jo.get("passedtests").getAsString(); // not present if outcome is "compile"

							if (evt_type.isEmpty()) { // old commit log format support
								if (totaltests.equals(passedtests)) {
									evt_class = "success";
									evt_type = "Success";
								} else {
									evt_class = "danger";
									evt_type = "Failed";
								}
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						break;
					case "start":
					case "started":
						evt_class = "active";
						evt_type = "Start";
						os = jo.get("os").getAsString();
						plm_version = jo.get("plm").getAsString();
						java_version = jo.get("java").getAsString();
						break;
					case "leaved":
						evt_class = "active";
						evt_type = "Stop";
						os = jo.get("os").getAsString();
						plm_version = jo.get("plm").getAsString();
						java_version = jo.get("java").getAsString();
						break;
						
					case "readTip":
						evt_class = "tip";
						evt_type = "Read";
						// ({"kind":"readTip","exo":"sort.basic.lessons.sort.basic.bubble.AlgBubbleSort1","course":"","id":"#tip-1","outcome":"pass","lang":"Scala"})

						break;
						
					case "callForHelp":
					case "cancelCallForHelp": // no break : same operations
						comment = jo.get("kind").getAsString();
						evt_class = "";
						evt_type = "Help";
						exolang = jo.get("lang").getAsString();
						break;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				if (evt_type != null && evt_type.equals("Switched")) {
					exoswitchto = jo.get("switchto").getAsString();
				} else {
					exoswitchto = "";
				}
			} catch (Exception ex) {

			}
			try {
				exoname = jo.get("exo").getAsString();
			} catch (Exception ex) {
				exoname = "";
			}
		} catch (JsonSyntaxException ex) {

		}

		String extURL = "";
		switch (exolang) {
			case "Java":
				extURL = "java";
				break;
			case "C":
				extURL = "c";
				break;
			case "Scala":
				extURL = "scala";
				break;
			case "Python":
				extURL = "py";
				break;
			case "lightbot":
				extURL = "ignored";
				break;
		}

		if (evt_type.equals("Switched")) {
			comment = "Switched to " + exoswitchto;
		} else if (evt_type.equals("Failed") || evt_type.equals("Success") || evt_type.equals("Compile err")) {
			comment = "Language : " + exolang + ", total tests : " + totaltests + ", passed : " + passedtests;
			codeLink = "https://github.com/mquinson/PLM-data/blob/" + commitID + "/" + exoname + "." + extURL + ".code";
			errorLink = "https://github.com/mquinson/PLM-data/blob/" + commitID + "/" + exoname + "." + extURL + ".error";
		} else if (evt_type.equals("Start")) {
			comment = "OS : " + os + ", PLM_VERSION : " + plm_version + ", JAVA_VERSION : " + java_version;
		} else if (evt_type.equals("Help")) {
			comment = comment + " ; Language : " + exolang;
			codeLink = "https://github.com/mquinson/PLM-data/blob/" + commitID + "/" + exoname + "." + extURL + ".code";
		}
	}
}