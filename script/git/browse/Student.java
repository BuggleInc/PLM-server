package git.browse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.api.errors.GitAPIException;

import models.Commit;
import controllers.JGit;

public class Student {
	Map<String,Integer> exoPassed = new HashMap<String,Integer>();
	Map<String,Integer> exoAttempted = new HashMap<String,Integer>();

	int passEvt, failEvt, compilEvt, helpEvt, startEvt, switchEvt, revertEvt, tipEvt;
	int evtCount,evtValidCount;

	String name;
	
	Student(String branchName) throws IOException, GitAPIException {
		name=branchName;
		ArrayList<Commit> commits = JGit.computeCommits(branchName.substring(3));
		evtCount = commits.size();
		for(Commit commit: commits) {
			if (! commit.isValid())
				continue;

			evtValidCount++;
			switch (commit.evt_type) {
			case "Success":
				passEvt++;
				incrementOrInitialize(exoPassed, commit.exoname);
				incrementOrInitialize(exoAttempted, commit.exoname);
				break;
			case "Failed":
				failEvt++;
				incrementOrInitialize(exoAttempted, commit.exoname);
				break;
			case "Compilation error":
				incrementOrInitialize(exoAttempted, commit.exoname);
				compilEvt++;
				break;
			case "Help":
				helpEvt++;
				break;
			case "Start":
				startEvt++;
				break;
			case "Switched":
				switchEvt++;
				break;
			case "Reverted":
				revertEvt++;
				break;
			case "Stop":
				break;
			case "Read": // Tip
				tipEvt++;
				break;
			default: 
				System.err.println("Unhandled evt_type: '"+commit.evt_type+"' ("+commit.commitLog+")");
			}
		}
	}
	
	public String toString() {
		return name+", "+
			   exoPassed.size()+", "+
			   exoAttempted.size()+", "+
			   
			   passEvt+", "+
			   failEvt+", "+
			   compilEvt+", "+
			   helpEvt+", "+
			   tipEvt+", "+
			   startEvt+", "+
			   switchEvt+", "+
			   revertEvt+", "+

			   " ";

	}
	
	void incrementOrInitialize(Map<String,Integer> map, String key) {
		Integer value = map.get(key); 
		if (value == null) {
			map.put(key,1);
		} else {
			map.put(key, value + 1);
		}
	}
}
