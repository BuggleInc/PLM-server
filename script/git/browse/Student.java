package git.browse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.jgit.api.errors.GitAPIException;

public class Student implements Comparable<Student>{
	static Set<String> validVersions = new HashSet<String>();
	static Set<String> betaVersions = new HashSet<String>();
	static {
		validVersions.add("2.4 (20140901)");
		validVersions.add("2.4.1 (20140905)");
		validVersions.add("2.4.2 (20140909)");
		validVersions.add("2.4.3 (20140911)");
		validVersions.add("2.4.4 (20140912)");
		validVersions.add("2.4.5 (20140916)");
		validVersions.add("2.4.6 (20140917)");
		validVersions.add("2.4.7 (20130920)");
		validVersions.add("2.4.8 (20140928)");
		validVersions.add("2.4.9 (20140929)");
		validVersions.add("2.4.10 (20140930)");
		validVersions.add("2.4.11 (20141009)");
		validVersions.add("2.5 (20141031)");
		validVersions.add("2.6-pre (20150202)");		
		validVersions.add("2.6 (20151010)");	
		
		betaVersions.add("2.3beta (20140515)");
		betaVersions.add("2.4alpha (20140821)");
		betaVersions.add("2.4beta1 (20140821)");
		betaVersions.add("2.4beta1 (20140901)");
		betaVersions.add("2.4beta2 (20140901)");
		betaVersions.add("2.4alpha (20140724)");
		betaVersions.add("2.4.8-git (20131001)");
		betaVersions.add("2.5-pre (20141015)");
		betaVersions.add("2.6-pre (20141130)");
		betaVersions.add("internal (internal)");
	}
	Boolean usesBeta = false;
	static int betaUsers = 0;
	

	Map<String,Integer> exoPassed = new HashMap<String,Integer>();
	Map<String,Integer> exoAttempted = new HashMap<String,Integer>();

	int passEvt, failEvt, compilEvt, helpEvt, startEvt,stopEvt,idleEvt, switchEvt, revertEvt, tipEvt;
	int evtCount,evtValidCount;

	String name;
	
	Map<String,Integer> dailyEvt  = new HashMap<String,Integer>();
	Map<String,Integer> weeklyEvt = new HashMap<String,Integer>();
	Map<String,Map<String,Integer>> monthlyEvt= new HashMap<String,Map<String,Integer>>(); 
	
	Map<String,Map<String,Integer>> dailyPassedLang  = new HashMap<String,Map<String,Integer>>();
	Map<String,Integer> dailyPassed  = new HashMap<String,Integer>();
	Map<String,Integer> weeklyPassed = new HashMap<String,Integer>();
	Map<String,Map<String,Integer>> monthlyPassed= new HashMap<String,Map<String,Integer>>(); 
	
	Map<String,Integer> scalaError = new HashMap<String,Integer>();
	
	int scala= 0;
	int python = 0;
	int C = 0;
	int java = 0;
	int lightbot = 0;
	int blockly = 0;
	
	static int unhandled = 0;
	static int handled = 0;

	static int passedExo = 0;
	static int feedback = 0;
	static int compil = 0;
	static int failed = 0;
	
	static Map<String,Map<String,Integer>>allClosedFeedback = new HashMap<String,Map<String,Integer>>();
	static Map<String,Vector<String>>allOpenFeedback = new HashMap<String,Vector<String>>();
	Vector<String> languages = new Vector<>();
	
	Student(String branchName) throws IOException, GitAPIException {
		Calendar cal = Calendar.getInstance();
		
		name=branchName;
		ArrayList<GitEvent> commits = GitUtils.computeCommits(branchName.substring(3));
		Collections.sort(commits);
		evtCount = commits.size();
		
		
		for (GitEvent commit: commits) {
			if (! commit.isValid())
				continue;

					
			cal.setTime(commit.rev.getAuthorIdent().getWhen());
			incOrInitialize(dailyEvt,   ""+cal.get(Calendar.YEAR)+"."+cal.get(Calendar.MONTH+1)+"."+cal.get(Calendar.DAY_OF_MONTH));
			incOrInitialize(weeklyEvt,  ""+cal.get(Calendar.YEAR)+"."+cal.get(Calendar.WEEK_OF_YEAR));
			incOrInitialize2(monthlyEvt, ""+cal.get(Calendar.YEAR)+"."+cal.get(Calendar.MONTH+1) , commit.exolang);

			if (commit.exolang != null) {
				if (commit.evt_type.equals("Help") ||
						commit.evt_type.equals("Start") ||
						commit.evt_type.equals("Stop") ||
						commit.evt_type.equals("idle") ||
						commit.evt_type.equals("Switched") ||
						commit.evt_type.equals("Reverted") ||
						commit.evt_type.equals("commonErrorFeedback") ||						
						commit.evt_type.equals("Read")) {
					/* Ignore other events than code submission */
				} else {
					switch(commit.exolang) {
					case "Scala":  scala++;  break;  
					case "Python": python++; break;
					case "C":      C++;      break;
					case "Java":   java++;   break;
					case "lightbot": lightbot++; break;
					case "Blockly": blockly++; break;
					default: System.err.println("Unhandled language: '"+commit.exolang+"' for event type '"+commit.evt_type+"' ("+commit.commitLog+")");
					}
				}
			}

			evtValidCount++;
			switch (commit.evt_type) {
			case "Success":
				passEvt++;
				String source = GitUtils.getSource(commit);
				
				cal.setTime(commit.rev.getAuthorIdent().getWhen());
				String dayDate = ""+cal.get(Calendar.YEAR)+"."+(cal.get(Calendar.MONTH)+1)+"."+cal.get(Calendar.DAY_OF_MONTH);
				incOrInitialize(dailyPassed,   dayDate);
				incOrInitialize2(dailyPassedLang,   dayDate, commit.exolang);
				incOrInitialize(weeklyPassed,  ""+cal.get(Calendar.YEAR)+"."+cal.get(Calendar.WEEK_OF_YEAR));
				incOrInitialize2(monthlyPassed, ""+cal.get(Calendar.YEAR)+"."+(cal.get(Calendar.MONTH)+1) , commit.exolang);
				
				if (source != null)
					setMin(exoPassed, commit.exoname, source.split("\n").length);
				
				if (commit.jo.get("exoDifficulty") != null) {
					feedback++;
					String Difficulty = commit.jo.get("exoDifficulty").toString();
					incOrInitialize2(allClosedFeedback, commit.exoname, Difficulty);
					incOrInitialize2(allClosedFeedback, commit.exoname, "DifficultyNb");
					addOrInitialize2(allClosedFeedback, commit.exoname, "InterestNb",0); // Just ensure that it's not null
				}
				if (commit.jo.get("exoInterest") != null && (!commit.jo.get("exoInterest").toString().equals("\"(please choose)\""))) {
					feedback++;
					String Interest = commit.jo.get("exoInterest").toString();
					incOrInitialize2(allClosedFeedback, commit.exoname, Interest);
					incOrInitialize2(allClosedFeedback, commit.exoname, "InterestNb");
					addOrInitialize2(allClosedFeedback, commit.exoname, "DifficultyNb",0); // Just ensure that it's not null
					switch (Interest) {
					case "\"Really good\"": addOrInitialize2(allClosedFeedback, commit.exoname, "InterestVal", 4); break;
					case "\"Amusing\"": addOrInitialize2(allClosedFeedback, commit.exoname, "InterestVal", 3); break;
					case "\"Just okay\"": addOrInitialize2(allClosedFeedback, commit.exoname, "InterestVal", 2); break;
					case "\"Boring\"": addOrInitialize2(allClosedFeedback, commit.exoname, "InterestVal", 1); break;
					case "\"Really bad\"": addOrInitialize2(allClosedFeedback, commit.exoname, "InterestVal", 0); break;
					default: System.out.println("Unhandled interest: "+Interest); System.exit(1);
					}
				}
				if (commit.jo.get("exoComment") != null) {
					feedback++;
					Vector<String> whereTo = allOpenFeedback.get(commit.exoname);
					if (whereTo == null) {
						whereTo = new Vector<String>();
						allOpenFeedback.put(commit.exoname, whereTo);
					}
					whereTo.add(commit.jo.get("exoComment").toString());
				}
				passedExo++;
				incOrInitialize(exoAttempted, commit.exoname);
				break;
			case "Failed":
				failEvt++;
				failed++;
				incOrInitialize(exoAttempted, commit.exoname);
				break;
			case "Compilation error":
				compil++;
				incOrInitialize(exoAttempted, commit.exoname);
				if (commit.exolang.equals("Scala")) {
					String error = GitUtils.getError(commit);
					if (error != null) {
						String[] lines = error.split("\n");
						int rank = 0;
						while (lines[rank].equals("") && rank < lines.length-1) 
							rank++;
																
						if (!lines[rank].equals("")) {
							if (ScalaHandler.addCommit(scalaError, lines[rank],commit))
								handled++;
							else
								unhandled++;
						}
							
					}
				}
				compilEvt++;
				break;
			case "Help":
				helpEvt++;
				break;
			case "commonErrorFeedback":
				/* TODO */
				break;
			case "Start":
				if (! validVersions.contains( commit.jo.get("plm").getAsString() )) {
					if (! betaVersions.contains(commit.jo.get("plm").getAsString())) 
						System.err.println("\nUnhandled version name: '"+ commit.jo.get("plm").getAsString()+"'") ;
					usesBeta = true;
					betaUsers++;
					return; // Don't parse this student any further!
				}
				startEvt++;
				break;
			case "Stop":
				stopEvt++;
				break;
			case "idle":
				idleEvt++;
				break;
			case "Switched":
				switchEvt++;
				break;
			case "Reverted":
				revertEvt++;
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
	static void incOrInitialize(Map<String,Integer> map, String key) {
		Integer value = map.get(key); 
		if (value == null) {
			map.put(key,1);
		} else {
			map.put(key, value + 1);
		}
	}
	static void addOrInitialize(Map<String,Integer> map, String key, Integer val) {
		Integer value = map.get(key); 
		if (value == null) {
			map.put(key,val);
		} else {
			map.put(key, value + val);
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
	void addOrInitialize2(Map<String,Map<String,Integer>> map, String key,String key2, Integer val) {
		Map<String,Integer> map2 = map.get(key);
		if (map2 == null) {
			map2 = new HashMap<String,Integer>();
			map.put(key,map2);
		}
		Integer value = map2.get(key2);
		if (value == null) {
			map2.put(key2,val);
		} else {
			map2.put(key2, value + val);
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
