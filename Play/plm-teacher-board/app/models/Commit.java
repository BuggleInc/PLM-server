/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package models;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Ced
 */
public class Commit {

	public String course, exolang, exoswitchto, evt_type, evt_class, totaltests,
		passedtests, exoname, commitTime, comment, os, plm_version, java_version;

	private String json;

	public Commit(String json, int commitTime) {
		this.json = json;
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
				switch(jo.get("evt_type").getAsString()) {
					case "switched":
						evt_class = "warning";
						evt_type = "Switched";
						break;
					case "executed": // TODO: check if total = passed tests
						try {
							totaltests = jo.get("totaltests").getAsString();
							passedtests = jo.get("passedtests").getAsString();
							exolang = jo.get("exolang").getAsString();
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
					case "started":
						evt_class = "active";
						evt_type = "Start";
						os = jo.get("os").getAsString();
						plm_version = jo.get("plm_version").getAsString();
						java_version = jo.get("java_version").getAsString();
						break;
				}
			} catch(Exception ex) {

			}
			try {
				if(evt_type != null && evt_type.equals("Switched")) {
					exoswitchto = jo.get("exoswitchto").getAsString();
				} else {
					exoswitchto = "";
				}
			} catch(Exception ex) {

			}
			try{
				exoname = jo.get("exoname").getAsString();
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
			comment = "os" + os + ", plm_version : " + plm_version + ", java_version : " + java_version;
		}
	}
}