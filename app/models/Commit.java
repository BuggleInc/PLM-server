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
 *
 * @author Ced
 */
public class Commit {

	public String course, exolang, exoswitchto, evt_type, evt_class, totaltests,
		passedtests, exoname, commitTime, comment, os, plm_version, java_version;

	public Commit(String json, int commitTime) {
		this.commitTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                          .format(new Date(commitTime * 1000L));
						  
		evt_type = "";
		evt_class = "";
		comment = "";
		exolang = "";
		
		try {
			JsonParser jsonParser = new JsonParser();
			JsonObject jo = (JsonObject)jsonParser.parse(json);
			//System.out.println(jo.get("evt_type").getAsString());
			try {
				switch(jo.get("kind").getAsString()) {
					case "switched":
						evt_class = "warning";
						evt_type = "Switched";
						break;
					case "executed": // TODO: check if total = passed tests
						try {
							totaltests = jo.get("totaltests").getAsString();
							passedtests = jo.get("passedtests").getAsString();
							exolang = jo.get("lang").getAsString();
							if(totaltests.equals(passedtests)) {
								evt_class = "success";
								evt_type = "Success";
							} else {
								evt_class = "danger";
								evt_type = "Failed";
							}
						} catch(Exception ex) {
						}
						break;
					case "start":
						evt_class = "active";
						evt_type = "Start";
						os = jo.get("os").getAsString();
						plm_version = jo.get("plm").getAsString();
						java_version = jo.get("java").getAsString();
						break;
				}
			} catch(Exception ex) {

			}
			try {
				if(evt_type != null && evt_type.equals("Switched")) {
					exoswitchto = jo.get("switchto").getAsString();
				} else {
					exoswitchto = "";
				}
			} catch(Exception ex) {

			}
			try{
				exoname = jo.get("exo").getAsString();
			} catch(Exception ex) {
				exoname = "";
			}
		} catch(JsonSyntaxException ex) {
			
		}
		
		if(evt_type.equals("Switched")) {
			comment = "Switched to " + exoswitchto;
		} else if(evt_type.equals("Failed") || evt_type.equals("Success")) {
			comment = "Language : " + exolang + ", total tests : " + totaltests + ", passed : " + passedtests;
		} else if (evt_type.equals("Start")) {
			comment = "OS : " + os + ", PLM_VERSION : " + plm_version + ", JAVA_VERSION : " + java_version;
		}
	}
}