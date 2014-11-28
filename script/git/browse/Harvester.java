package git.browse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.lib.Ref;

import utils.GitUtils;

public class Harvester {

	public static void main(String args[]) throws Exception {
		GitUtils.fetchRepo(false);
		
		Git git = new Git(GitUtils.repository);

		List<Ref> branches = git.branchList().setListMode(ListMode.REMOTE).call();

		int totalCommits = 0;

		List<Student> students = new ArrayList<Student>();
		
		System.out.println(Student.getHeader());
		
		for(Ref branch:branches) {
			if(branch.getName().contains("PLM")) {				
				String branchName = branch.getName().substring(20);
				Student student = new Student(branchName);
				if (student.exoAttempted.size()>=1) {
					students.add(student);

					totalCommits += student.evtValidCount;
				}
			}
		}
		Collections.sort(students);
		for (Student s:students)
				System.out.println(s);
			

		System.out.println("There is "+ students.size() + " non-empty students and "+ totalCommits + " valid commits!");
		
		
		// Compute some cumulative stats about the DB
		int[] passing = new int[201];
		int[] attempting = new int[201];
		int[] lines      = new int[4001];
		Map<String,Integer> daily  = new HashMap<String,Integer>();
		Map<String,Integer> weekly = new HashMap<String,Integer>();
		Map<String,Integer> monthly= new HashMap<String,Integer>(); 
		Map<String,Integer> monthlyJava= new HashMap<String,Integer>(); 
		Map<String,Integer> monthlyScala= new HashMap<String,Integer>(); 
		Map<String,Integer> monthlyPython= new HashMap<String,Integer>(); 

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
			
			for (String date:student.daily.keySet())
				student.incOrInitialize(daily, date);
			for (String date:student.weekly.keySet())
				student.incOrInitialize(weekly, date);
			for (String date:student.monthly.keySet()) {
				student.incOrInitialize(monthly, date);
				if (student.monthly.get(date).get("Java") != null)
					student.incOrInitialize(monthlyJava, date);
				if (student.monthly.get(date).get("Scala") != null)
					student.incOrInitialize(monthlyScala, date);
				if (student.monthly.get(date).get("Python") != null)
					student.incOrInitialize(monthlyPython, date);
			}
		}
		System.out.println("# exo count , cumulative count of passing students, cumulative count of attempting students");
		for (int i=passing.length;i>=0;i--) {
			if ((i%25 == 0 && i!=0) || i==10 || i==5 || i==1)
				System.out.format(" %4d ,  %4d,  %4d\n",i, passing[i], attempting[i]);
		}
		System.out.println("# lines count , cumulative count of students with more passing lines");
		for (int i=lines.length;i>=0;i--) {
			if ((i%1000==0 && i!=0) || i==500 || i==250 || i==50 || i==10)
				System.out.format("  %4d,  %4d\n",i, lines[i]);
		}
		System.out.println("# Month, Active users per month, Active Java users, Active Python users, Active Scala users");
		for (int year=2014; year<2016; year++) {
			for (int month=0; month<52; month++) {
				String date = ""+year+"."+month;
				Integer m = monthly.get(date);
				if (m!=null) {
					int j = monthlyJava.get(date);
					int p = monthlyPython.get(date);
					int s = monthlyScala.get(date);
					System.out.format("%d.%2d, %4d, %3d, %3d, %3d\n",
							year,month+1,m, j, p, s);
				}
			}
		}
	
		System.out.println("# Week, Active users per week, monday users, tuesday, wednesday, thursday, friday, saturday, sunday");
		Calendar cal = Calendar.getInstance();
		for (int year=2014; year<2016; year++) {
			for (int week=0; week<52; week ++) {
				Integer w = weekly.get(""+year+"."+week);
				if (w != null) {
					System.out.format("%d.%2d  , %3d     ",year,week+1,w);
					cal.clear();
					cal.set(Calendar.YEAR, year);
					cal.set(Calendar.WEEK_OF_YEAR,week);
					
					cal.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
					Integer val = daily.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					if (val == null) val = 0;
					System.out.format(", %3s",val);

					cal.set(Calendar.DAY_OF_WEEK,Calendar.TUESDAY);
					val = daily.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					if (val == null) val = 0;
					System.out.format(", %3s",val);

					cal.set(Calendar.DAY_OF_WEEK,Calendar.WEDNESDAY);
					val = daily.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					if (val == null) val = 0;
					System.out.format(", %3s",val);

					cal.set(Calendar.DAY_OF_WEEK,Calendar.THURSDAY);
					val = daily.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					if (val == null) val = 0;
					System.out.format(", %3s",val);

					cal.set(Calendar.DAY_OF_WEEK,Calendar.FRIDAY);
					val = daily.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					if (val == null) val = 0;
					System.out.format(", %3s",val);

					cal.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
					val = daily.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					if (val == null) val = 0;
					System.out.format(", %3s",val);

					cal.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
					val = daily.get(""+year+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.DAY_OF_MONTH));
					if (val == null) val = 0;
					System.out.format(", %3s",val);
					
					System.out.println();
				}
			}
		}
	}
	
}
