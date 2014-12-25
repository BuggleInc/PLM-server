package git.browse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;

import utils.GitUtils;

/* TODO:
 * 
 * - meilleur feedback
 * - temps de résolution
 * - nb d'essais maximal
 * - taux de complétion des leçons
 * - erreur de compil courante
 * - qté de travail en dehors de heures
 * - travail linéaire vs papillon / exercices papillonés
 * - qté de ragequit
 */

public class Harvester {
	static int totalCommits = 0;	
	static List<Student> students = new ArrayList<Student>();
	static Map<String,Integer> scalaError = new HashMap<String,Integer>();

	public static void parse() throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		GitUtils.fetchRepo(false);

		Git git = new Git(GitUtils.repository);

		List<Ref> branches = git.branchList().setListMode(ListMode.REMOTE).call();

		for(Ref branch:branches) {
			if(branch.getName().contains("PLM")) {				
				String branchName = branch.getName().substring(20);
				Student student = new Student(branchName);
				if (student.exoAttempted.size()>=1) {
					students.add(student);
					if (students.size() % 150 == 0)
						System.out.println(".");
					else
						System.out.print(".");

					totalCommits += student.evtValidCount;
				}
				for (String key:student.scalaError.keySet()) {
					if (!scalaError.containsKey(key))
						scalaError.put(key, student.scalaError.get(key));
					else
						scalaError.put(key, student.scalaError.get(key) + scalaError.get(key));
				}
			}
		}
		System.out.println();
	}
	static void printBranches() {
		System.out.println(Student.getHeader());

		Collections.sort(students);
		for (Student s:students)
			System.out.println(s);		
	}
	static void printErrors() {
		for (String key:scalaError.keySet()) 
			System.out.println(scalaError.get(key)+": "+key);
		System.out.println("Scala errors: "+Student.handled+" handled; "+Student.unhandled+" unhandled.");		
	}
	static void printFeedback() {
		for (String exoName: Student.allClosedFeedback.keySet()) {

			if (Student.allClosedFeedback.get(exoName) != null && Student.allClosedFeedback.get(exoName).get("InterestNb") > 3) {
				System.out.print(exoName+": avg interest="+(Student.allClosedFeedback.get(exoName).get("InterestVal")/Student.allClosedFeedback.get(exoName).get("InterestNb")+" "));
				for (String val:Student.allClosedFeedback.get(exoName).keySet()) 
					System.out.print(val+"="+Student.allClosedFeedback.get(exoName).get(val)+";  ");
				System.out.println();
			}
		}		
	}
	static void printCumulative() {
		int[] passing = new int[201];
		int[] attempting = new int[201];
		int[] lines      = new int[4001];
		for (Student student: students) {
			for (int i=0;i<passing.length;i++) {
				if (student.exoPassed.size()>=i)
					passing[i] ++;
				if (student.exoAttempted.size()>=i)
					attempting[i] ++;
			}
			int passedLines = 0;
			for (String exoName : student.exoPassed.keySet())
				passedLines += student.exoPassed.get(exoName);
			for (int i=0;i<lines.length;i++) {
				if (passedLines >= i)
					lines[i]++;
			}
		}

		System.out.println("# exo count , cumulative count of passing students, cumulative count of attempting students");
		for (int i=passing.length;i>=0;i--) {
			if ((i%25 == 0 && i!=0) || i==190 || i==10 || i==5 || i==1)
				System.out.format(" %4d ,  %4d,  %4d\n",i, passing[i], attempting[i]);
		}

		System.out.println("# lines count , cumulative count of students with more passing lines");
		for (int i=lines.length;i>=0;i--) {
			if ((i%1000==0 && i!=0) || i==500 || i==250 || i==50 || i==10)
				System.out.format("  %4d,  %4d\n",i, lines[i]);
		}
	}
	static void printMonthly() {
		Map<String,Integer> monthlyPassingUsers= new HashMap<String,Integer>(); 
		Map<String,Integer> monthlyJavaPassingUsers= new HashMap<String,Integer>(); 
		Map<String,Integer> monthlyScalaPassingUsers= new HashMap<String,Integer>(); 
		Map<String,Integer> monthlyPythonPassingUsers= new HashMap<String,Integer>(); 

		Map<String,Integer> monthlyPassedExo= new HashMap<String,Integer>(); 
		Map<String,Integer> monthlyJavaPassedExo= new HashMap<String,Integer>(); 
		Map<String,Integer> monthlyScalaPassedExo= new HashMap<String,Integer>(); 
		Map<String,Integer> monthlyPythonPassedExo= new HashMap<String,Integer>(); 

		for (Student student: students) {
			for (String date:student.monthlyPassed.keySet()) {
				Student.incOrInitialize(monthlyPassingUsers, date);
				
				if (student.monthlyPassed.get(date).get("Java") != null) {
					Student.incOrInitialize(monthlyJavaPassingUsers, date);
					Student.addOrInitialize(monthlyJavaPassedExo, date, student.monthlyPassed.get(date).get("Java"));
					Student.addOrInitialize(monthlyPassedExo,     date, student.monthlyPassed.get(date).get("Java"));
				}
				if (student.monthlyPassed.get(date).get("Scala") != null) {
					Student.incOrInitialize(monthlyScalaPassingUsers, date);
					Student.addOrInitialize(monthlyScalaPassedExo, date, student.monthlyPassed.get(date).get("Scala"));
					Student.addOrInitialize(monthlyPassedExo,      date, student.monthlyPassed.get(date).get("Scala"));
				}
				if (student.monthlyPassed.get(date).get("Python") != null) {
					Student.incOrInitialize(monthlyPythonPassingUsers, date);
					Student.addOrInitialize(monthlyPythonPassedExo, date, student.monthlyPassed.get(date).get("Python"));
					Student.addOrInitialize(monthlyPassedExo,       date, student.monthlyPassed.get(date).get("Python"));
				}
			}
		}
		System.out.println("# Monthly statistics. For each column, we report passedExo/passingUsers that match the criteria");
		System.out.println("# Month, Monthly stats, Java only,   Python ,   Scala");

		System.out.println("| Month |exos_month|traces_month|exos_java_month|traces_java_month| exos_python_month|traces_python_month| exos_scala_month|traces_scala_month|");
		System.out.println("|-------+----------+------------+---------------+-----------------+------------------+-------------------+-----------------+------------------|");
		for (int year=2014; year<2016; year++) {
			for (int month=0; month<52; month++) {
				String date = ""+year+"."+month;
				Integer mu = monthlyPassingUsers.get(date);
				Integer me = monthlyPassedExo.get(date);
				if (me == null)
					me = 0;
				if (mu!=null) {
					int ju = monthlyJavaPassingUsers.get(date);
					int pu = monthlyPythonPassingUsers.get(date);
					int su = monthlyScalaPassingUsers.get(date);

					int je = monthlyJavaPassedExo.get(date);
					int pe = monthlyPythonPassedExo.get(date);
					int se = monthlyScalaPassedExo.get(date);
					System.out.format("15/%d/%d|  %5d|%4d  | %4d|%3d | %4d|%3d | %4d|%3d|\n",
							month+1,year, me,mu, je,ju, pe,pu, se,su);
				}
			}
		}

	}

	static void printDaily() {
		Map<String,Integer> dailyJavaPassingUsers= new HashMap<String,Integer>(); 
		Map<String,Integer> dailyScalaPassingUsers= new HashMap<String,Integer>(); 
		Map<String,Integer> dailyPythonPassingUsers= new HashMap<String,Integer>(); 

		Map<String,Integer> dailyJavaPassedExo= new HashMap<String,Integer>(); 
		Map<String,Integer> dailyScalaPassedExo= new HashMap<String,Integer>(); 
		Map<String,Integer> dailyPythonPassedExo= new HashMap<String,Integer>(); 

		for (Student student: students) {
			for (String date:student.dailyPassedLang.keySet()) {

				if (student.dailyPassedLang.get(date).get("Java") != null) {
					Student.incOrInitialize(dailyJavaPassingUsers, date);
					Student.addOrInitialize(dailyJavaPassedExo, date, student.dailyPassedLang.get(date).get("Java"));
				}
				if (student.dailyPassedLang.get(date).get("Scala") != null) { 
					Student.incOrInitialize(dailyScalaPassingUsers, date);
					Student.addOrInitialize(dailyScalaPassedExo, date, student.dailyPassedLang.get(date).get("Scala"));
				}
				if (student.dailyPassedLang.get(date).get("Python") != null) { 
					Student.incOrInitialize(dailyPythonPassingUsers, date);
					Student.addOrInitialize(dailyPythonPassedExo, date, student.dailyPassedLang.get(date).get("Python"));
				}
			}
		}
		System.out.println("|Date|exos_java|traces_java| exos_python|traces_python| exos_scala|traces_scala|");
		System.out.println("|----+---------+-----------+------------+-------------+-----------+------------|");
		for (int year=2014; year<2016; year++) {
			for (int month=1; month<13; month ++) {
				for (int day=1; day<32; day ++) {
					String date = ""+year+"."+month+"."+day;
					Integer ju = intOrZero(dailyJavaPassingUsers.get(date));
					Integer je = intOrZero(dailyJavaPassedExo.get(date));
					Integer su = intOrZero(dailyScalaPassingUsers.get(date));
					Integer se = intOrZero(dailyScalaPassedExo.get(date));
					Integer pu = intOrZero(dailyPythonPassingUsers.get(date));
					Integer pe = intOrZero(dailyPythonPassedExo.get(date));
					if (0+ju+su+pu > 0) {	
						System.out.println("|"+date+"|"+je+"|"+ju+"|"+pe+"|"+pu+"|"+se+"|"+su+"|");
					}
				}
			}
		}
	}
	
	public static void printWeekly() {
		Map<String,Integer> dailyPassingUsers  = new HashMap<String,Integer>();
		Map<String,Integer> dailyPassedExo  = new HashMap<String,Integer>();

		Map<String,Integer> weeklyPassingUsers = new HashMap<String,Integer>();
		Map<String,Integer> weeklyPassedExo = new HashMap<String,Integer>();

		for (Student student: students) {

			for (String date:student.dailyPassed.keySet())
				Student.incOrInitialize(dailyPassingUsers, date);
			for (String date:student.weeklyPassed.keySet())
				Student.incOrInitialize(weeklyPassingUsers, date);

			for (String date:student.dailyPassed.keySet())
				Student.addOrInitialize(dailyPassedExo, date, student.dailyPassed.get(date));
			for (String date:student.weeklyPassed.keySet())
				Student.addOrInitialize(weeklyPassedExo, date, student.weeklyPassed.get(date));
		}
		System.out.println("# Week,Weekly exos/studnts,  Monday ,  Tuesday,Wednesday, Thursday,  Friday , Saturday,   Sunday");

		String[] months = new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
		Calendar cal = Calendar.getInstance();
		for (int year=2014; year<2016; year++) {
			for (int week=0; week<54; week ++) {
				Integer users = weeklyPassingUsers.get(""+year+"."+week);
				Integer exos = weeklyPassedExo.get(""+year+"."+week);
				if (users != null) {
					System.out.format("%d.%2d  ,   %4d/%3d     ",year,week+1,exos,users);
					cal.clear();
					cal.set(Calendar.YEAR, year);
					cal.set(Calendar.WEEK_OF_YEAR,week);

					cal.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
					users = dailyPassingUsers.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					exos  = dailyPassedExo.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					if (users == null) users = 0;
					if (exos == null) exos = 0;
					System.out.format(", %4s/%3d",exos,users);

					cal.set(Calendar.DAY_OF_WEEK,Calendar.TUESDAY);
					users = dailyPassingUsers.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					exos = dailyPassedExo.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					if (users == null) users = 0;
					if (exos == null) exos = 0;
					System.out.format(", %4s/%3d",exos,users);

					cal.set(Calendar.DAY_OF_WEEK,Calendar.WEDNESDAY);
					users = dailyPassingUsers.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					exos = dailyPassedExo.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					if (users == null) users = 0;
					if (exos == null) exos = 0;
					System.out.format(", %4s/%3d",exos,users);

					cal.set(Calendar.DAY_OF_WEEK,Calendar.THURSDAY);
					users = dailyPassingUsers.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					exos = dailyPassedExo.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					if (users == null) users = 0;
					if (exos == null) exos = 0;
					System.out.format(", %4s/%3d",exos,users);

					cal.set(Calendar.DAY_OF_WEEK,Calendar.FRIDAY);
					users = dailyPassingUsers.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					exos = dailyPassedExo.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					if (users == null) users = 0;
					if (exos == null) exos = 0;
					System.out.format(", %4s/%3d",exos,users);

					cal.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
					users = dailyPassingUsers.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					exos = dailyPassedExo.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					if (users == null) users = 0;
					if (exos == null) exos = 0;
					System.out.format(", %4s/%3d",exos,users);

					cal.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
					users = dailyPassingUsers.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					exos = dailyPassedExo.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					if (users == null) users = 0;
					if (exos == null) exos = 0;
					System.out.format(", %4s/%3d",exos,users);

					cal.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
					System.out.println("  # Week of monday "+ cal.get(Calendar.DAY_OF_MONTH)+ "th of "+months[cal.get(Calendar.MONTH)]);
				}
			}
		}
	}
	public static void main(String args[]) throws Exception {
		parse();

		//printBranches();
		//printErrors();
		//printFeedback();

		//printCumulative();
		//printMonthly();
		//printWeekly();
		printDaily();
		System.out.println("\nThere is "+ students.size() + " non-empty students, "+Student.passedExo+" passed exos (of which "+Student.feedback+" have a feedback) and "+ totalCommits + " valid commits!");
		System.out.println("Failed exos: "+Student.failed+"; compil error:"+Student.compil);

	}
	static int intOrZero(Integer i) {
		if (i==null)
			return 0;
		return i;
	}
}
