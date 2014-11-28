package git.browse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import models.GitEvent;

import org.eclipse.jgit.api.errors.GitAPIException;

import utils.GitUtils;

public class Student implements Comparable<Student>{

	Map<String,Integer> exoPassed = new HashMap<String,Integer>();
	Map<String,Integer> exoAttempted = new HashMap<String,Integer>();

	int passEvt, failEvt, compilEvt, helpEvt, startEvt, switchEvt, revertEvt, tipEvt;
	int evtCount,evtValidCount;

	String name;
	
	Map<String,Integer> daily  = new HashMap<String,Integer>();
	Map<String,Integer> weekly = new HashMap<String,Integer>();
	Map<String,Map<String,Integer>> monthly= new HashMap<String,Map<String,Integer>>(); 

	Student(String branchName) throws IOException, GitAPIException {
		Calendar cal = Calendar.getInstance();
		
		name=branchName;
		ArrayList<GitEvent> commits = GitUtils.computeCommits(branchName.substring(3));
		evtCount = commits.size();
		for (GitEvent commit: commits) {
			if (! commit.isValid())
				continue;

			cal.setTime(commit.rev.getAuthorIdent().getWhen());
			incOrInitialize(daily,   ""+cal.get(Calendar.YEAR)+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
			incOrInitialize(weekly,  ""+cal.get(Calendar.YEAR)+"."+cal.get(Calendar.WEEK_OF_YEAR));
			incOrInitialize2(monthly, ""+cal.get(Calendar.YEAR)+"."+cal.get(Calendar.MONTH) , commit.exolang);
					

			evtValidCount++;
			switch (commit.evt_type) {
			case "Success":
				passEvt++;
				String source = GitUtils.getSource(commit);
				
				if (source != null)
					setMin(exoPassed, commit.exoname, source.split("\n").length);
				incOrInitialize(exoAttempted, commit.exoname);
				break;
			case "Failed":
				failEvt++;
				incOrInitialize(exoAttempted, commit.exoname);
				break;
			case "Compilation error":
				incOrInitialize(exoAttempted, commit.exoname);
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

	static public String getHeader() {
		return "# branch,  exoPassed,  exoAttempted, passedLines, passed,  failed,  compile,  help,  tip,  start,  switch, revert";
	}

	public String toString() {
		int passedLines = 0;
		for (String exoName : exoPassed.keySet())
			passedLines += exoPassed.get(exoName);
			
		return name+", "+
				exoPassed.size()+", "+
				exoAttempted.size()+", "+
				passedLines+", "+

			   passEvt+", "+
			   failEvt+", "+
			   compilEvt+", "+
			   helpEvt+", "+
			   tipEvt+", "+
			   startEvt+", "+
			   switchEvt+", "+
			   revertEvt+

			   " ";

	}

	void setMin(Map<String,Integer> map, String key, Integer value2) {
		Integer value = map.get(key); 
		if (value == null) {
			map.put(key,value2);
		} else {
			map.put(key, Math.min(value,value2));
		}
	}
	void incOrInitialize(Map<String,Integer> map, String key) {
		Integer value = map.get(key); 
		if (value == null) {
			map.put(key,1);
		} else {
			map.put(key, value + 1);
		}
	}
	void incOrInitialize2(Map<String,Map<String,Integer>> map, String key,String key2) {
		Map<String,Integer> map2 = map.get(key);
		if (map2 == null) {
			map2 = new HashMap<String,Integer>();
			map.put(key,map2);
		}
		Integer value = map2.get(key2);
		if (value == null) {
			map2.put(key2,1);
		} else {
			map2.put(key2, value + 1);
		}
	}

	@Override
	public int compareTo(Student other) {
		if (exoPassed.size() == other.exoPassed.size()) {
			if (exoAttempted.size() == other.exoAttempted.size())
				return 0;
			if (exoAttempted.size() < other.exoAttempted.size())
				return 1;
			return -1;
		}
		if (exoPassed.size() < other.exoPassed.size())
			return 1;
		return -1;
	}
}
