/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package models;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 *
 * @author Ced
 */
public class Commit {

	public String course, exolang, exoswitchto, evt_type, totaltests, passedtests, exoname;

	private String json;

	public Commit(String json) {
		this.json = json;
		try {
			JsonParser jsonParser = new JsonParser();
			JsonObject jo = (JsonObject)jsonParser.parse(json);
			System.out.println(jo.get("evt_type").getAsString());
			try {
				switch(jo.get("evt_type").getAsString()) {
					case "switched":
						evt_type = "switch.png";
						break;
					case "executed": // TODO: check if total = passed tests
						evt_type = "correct.png";
						break;
					case "started":
						evt_type = "wrong.png";
						break;
				}
			} catch(Exception ex) {

			}
		} catch(JsonSyntaxException ex) {
			
		}
		
	}
	
	@Override
	public String toString() {
		String str = "";
		str= "<img src=\"/assets/images/"+evt_type+"\" width=\"1%\" height=\"1%\"></img> bnej";
		
		return str;
	}
}