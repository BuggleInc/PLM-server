package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.GregorianCalendar;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import com.jcraft.jsch.JSchException;

import com.google.gson.*;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;

import play.mvc.Controller;
import play.mvc.Result;

import models.*;

public class JGit extends Controller {
	private static final String REMOTE_URL = "https://github.com/mquinson/PLM-data.git";
	
	public static String filePath;
	
	public static int passed;

	public static Result index() {
		return ok("You called the index method of the Git controller.");
	}

	public static Result cloneRepo() throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		// prepare a new folder for the cloned repository
		File localPath = File.createTempFile("TestGitRepository", "");
		localPath.delete();
		filePath = localPath.getAbsolutePath();
		
		// then clone
		System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
		Git.cloneRepository().setURI(REMOTE_URL).setDirectory(localPath).call();

		// now open the created repository
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repository = builder.setGitDir(localPath).readEnvironment() // scan environment GIT_* variables
				.findGitDir() // scan up the file system tree
				.build();

		System.out.println("Having repository: " + repository.getDirectory());

		// Repository localRepo = new FileRepository(localPath + "/.git");
		// Git git = new Git(localRepo);
		// git.pull().call();

		
		
		repository = FileRepositoryBuilder.create(new File(localPath+"/.git"));
		Git git = new Git(repository);
		RevWalk walk = new RevWalk(repository);
		RevCommit commit = null;
		String s = "";
		
		Iterable<RevCommit> logs = git.log().call();
		Iterator<RevCommit> i = logs.iterator();

		while (i.hasNext()) {
			commit = walk.parseCommit(i.next());
			s = s + commit.getFullMessage() + "\n";

		}
		repository.close();
		
		System.out.println(s);
		
		return ok(s);
	}
	
	
	private static ArrayList<Commit> computeCommits(String uuid)  throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		uuid = "PLM"+uuid;
		File localPath = new File("repo/");
		if (!localPath.exists()) {
			localPath.mkdir();
			filePath = localPath.getAbsolutePath();

			// clone
			System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
			Git.cloneRepository().setURI(REMOTE_URL).setDirectory(localPath).call();
		}
		
		Repository repository = FileRepositoryBuilder.create(new File(localPath+"/.git"));
		Git git = new Git(repository);

		git.checkout().setName("master").call();

		try {
			CreateBranchCommand create = git.branchCreate();
			create.setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM);
			create.setName(uuid);
			create.setStartPoint("origin/" + uuid);
			create.call();
		} catch (RefAlreadyExistsException ex) {

		}

		PullCommand pullCmd = git.pull();

		// checkout the branch of the current user
		git.checkout().setName(uuid).call();
		
		git.pull().call();
		
		RevWalk walk = new RevWalk(repository);
		RevCommit commit = null;
		
		Iterable<RevCommit> logs = git.log().call();
		Iterator<RevCommit> i = logs.iterator();
			
		Gson gson = new Gson();
		ArrayList<Commit> commits = new ArrayList<>();
		while (i.hasNext()) {
			commit = walk.parseCommit(i.next());
			String commitJson = commit.getFullMessage();
			commits.add(new Commit(commitJson, commit.getCommitTime()));
		}
		repository.close();
		return commits;
	}
	
	public static Result displayBranch(String uuid, String studentname) throws IOException, InvalidRemoteException, TransportException, GitAPIException, ParseException {
		ArrayList<Commit> commits = computeCommits(uuid);
		ArrayList<Double> eventSummary = new ArrayList<>();
		int chartDay = 15;
		Integer[] eventCount = new Integer[chartDay];
		int cptEvt = 0;
		for(int i = 0; i<chartDay; i++) { // init eventCount for the chart
			eventCount[i] = 0;
		}
		
		eventSummary.add(0.0);eventSummary.add(0.0);eventSummary.add(0.0);eventSummary.add(0.0);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
		Calendar cal = Calendar.getInstance(), endRange = Calendar.getInstance(), beginRange = Calendar.getInstance();
		beginRange.add(Calendar.DAY_OF_YEAR, - (chartDay-1));
		Date dateParsed;
		for(Commit c : commits) {
			cptEvt++;
			
			dateParsed = df.parse(c.commitTime); // get a Date object with the String
			cal.setTime(dateParsed); // use a Calendar
			if(cal.compareTo(endRange) <= 0 && cal.compareTo(beginRange) >= 0) { // if commit date is in the range of the chart
				eventCount[cal.get(Calendar.DAY_OF_YEAR)-beginRange.get(Calendar.DAY_OF_YEAR)]++;
			}
			
			switch(c.evt_type) {
				case "Switched":
					eventSummary.set(0, eventSummary.get(0)+1);
				break;
				case "Success":
					eventSummary.set(1, eventSummary.get(1)+1);
				break;
				case "Failed":
					eventSummary.set(2, eventSummary.get(2)+1);
				break;
				case "Start":
					eventSummary.set(3, eventSummary.get(3)+1);
				break;
			}
		}
		cptEvt-=2; // 2 useless commits for statistics: "Empty initial commit" and "Create README.md"
		for(int j =0; j<eventSummary.size(); j++) {
			eventSummary.set(j, eventSummary.get(j)*100/cptEvt);
		}
		final File path = new File("repo");
		
		final ArrayList<ProgressItem> summary = new ArrayList<>();
		
		passed = 0;
		
		computeProgress(summary, path);
		
		return ok(
			views.html.commits.render(commits, studentname, summary, eventSummary, eventCount)
			);
	}
	
	/**
	 * Compute the progression of students for a given course
	 *
	 * @param uuidList the list of students uuid
	 * @param course the course
	 */
	public static ArrayList<ProgressItem> computeStudentForLesson(ArrayList<String> uuidList, Course course) throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		final ArrayList<ProgressItem> summary = new ArrayList<>();
		int cpt = 0;
		for(String uuid : uuidList) { // for each student
		cpt++;
			ArrayList<Commit> commits = computeCommits(uuid);
			int possible = 0, passed = 0 ;
			String p = course.programmingLanguage; // for the programming language
			possible = 0;
			passed = 0;
			final File path = new File("repo");
			
			passed = 0;
			Path sourcePath = Paths.get("repo/"+course.name+".summary");
			String summaryLine = "";
			try (BufferedReader reader = Files.newBufferedReader(sourcePath, StandardCharsets.UTF_8)) {
				summaryLine = reader.readLine();
				//System.out.println("Summary : "+ summaryLine);
			} catch (IOException ex) { // file does not exists maybe
				summary.add(new ProgressItem(course.name, p, 1, -1));
				return summary;
			}
			// Retrieve informations on per language progression
			JsonParser jsonParser = new JsonParser();
			JsonObject jo = (JsonObject)jsonParser.parse(summaryLine);
			
			possible = jo.get("possible"+p).getAsInt();
			try {
				passed = jo.get("passed"+p).getAsInt();
			} catch (Exception ex) { // passed information for the current language not available
			}
			//System.out.println(course.name + "   " + p + "   " + possible + ", " + passed +" done");
			if(passed > 0) {
				summary.add(new ProgressItem(course.name, p, possible, passed));
			} else { // not attemp
				summary.add(new ProgressItem(course.name, p, 1, -1));
			}
		}
		return summary;
	}
	
	/**
	 * Compute progression for the current repo state
	 */
	private static void computeProgress (final ArrayList<ProgressItem> summary, final File path) {
		String pattern = "*.summary";
		FileSystem fs = FileSystems.getDefault();
		final PathMatcher matcher = fs.getPathMatcher("glob:" + pattern); // to match file names ending with .summary

		FileVisitor<Path> matcherVisitor = new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attribs) {
				Path name = file.getFileName();
				String[] languages = {"Java", "Python", "Scala", "C"};
				if (matcher.matches(name)) { // if file matches
					String s = name + "";
					String[] tab = s.split("\\.", 0);
					String lessonNameTmp = "";
					for (int i = 0; i <= tab.length - 2; i++) { // get the lesson id
						lessonNameTmp += tab[i];
					}
					final String lessonName = lessonNameTmp;
					
					// Read lessonID.summary
					Path sourcePath = Paths.get("repo/"+lessonName+".summary");
					String summaryLine = "";
					try (BufferedReader reader = Files.newBufferedReader(sourcePath, StandardCharsets.UTF_8)) {
						summaryLine = reader.readLine();
						//System.out.println("Summary : "+ summaryLine);
					} catch (IOException ex) {
						//System.out.println("Can't open "+sourcePath);
					}
					// Retrieve informations on per language progression
					JsonParser jsonParser = new JsonParser();
					JsonObject jo = (JsonObject)jsonParser.parse(summaryLine);
					int possible = 0, passed = 0 ;
					for (final String p : languages) { // for each programming language, how many exercises are done/possible 
						possible = 0;
						passed = 0;
						possible = jo.get("possible"+p).getAsInt();
						try {
							passed = jo.get("passed"+p).getAsInt();
						} catch (Exception ex) { // passed information for the current language not available
						}
						//System.out.println(lessonName + "   " + p + "   " + possible + ", " + passed +" done");
						if(passed > 0) {
							summary.add(new ProgressItem(lessonName, p, possible, passed));
						}
					}
				}
				return FileVisitResult.CONTINUE;
			}
		};
		try {
			Files.walkFileTree(Paths.get(path.getPath()), matcherVisitor);
		} catch (IOException ex) {
		}
	}

}
