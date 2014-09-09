package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import models.Commit;
import models.Course;
import models.ProgressItem;

import models.Student;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import play.mvc.Controller;
import play.mvc.Result;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JGit extends Controller {
	public static final String REMOTE_URL = "https://github.com/mquinson/PLM-data.git";
	
	public static int passed;

	public static Result index() {
		return ok("You called the index method of the Git controller.");
	}
	
	public static void fetchRepo() {
		File localPath = new File("repo/");
		if (!localPath.exists()) {
			localPath.mkdir();

			// clone
			//System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
			try {
				Git.cloneRepository().setURI(REMOTE_URL).setDirectory(localPath).call();
			} catch (GitAPIException e) {
			}
		}
		
		Repository repository;
		try {
			repository = FileRepositoryBuilder.create(new File(localPath+"/.git"));
			Git git = new Git(repository);

			git.checkout().setName("master").call();
			
			git.fetch().call();
		} catch (IOException|GitAPIException e) {
		}
		
	}

	private static void checkoutUserBranch(String hashedUuid) throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		hashedUuid = "PLM"+hashedUuid;
		File localPath = new File("repo/");
		if (!localPath.exists()) {
			localPath.mkdir();

			// clone
			//System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
			Git.cloneRepository().setURI(REMOTE_URL).setDirectory(localPath).call();
		}
		
		Repository repository = FileRepositoryBuilder.create(new File(localPath+"/.git"));
		Git git = new Git(repository);

		git.checkout().setName("master").call();
		try {
			git.fetch().call();
		} catch (TransportException ex) {
			System.out.println("Not connected to Internet to fetch the repo.");
		}
		try {
			CreateBranchCommand create = git.branchCreate();
			create.setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM);
			create.setName(hashedUuid);
			create.setStartPoint("origin/" + hashedUuid); 
			create.call();
		} catch (RefAlreadyExistsException ex) {

		}

		// checkout the branch of the current user
		git.checkout().setName(hashedUuid).call();
		
		try {
			git.pull().call();
		} catch (TransportException ex) {
			System.out.println("Not connected to Internet to fetch the repo.");
		}
	}
	
	public static ArrayList<String> getLastActivity(List<Student> students) throws IOException, InvalidRemoteException, TransportException, GitAPIException {
        ArrayList<String> lastActivity = new ArrayList<>();
		File localPath = new File("repo/");
		if (!localPath.exists()) {
			localPath.mkdir();
			Git.cloneRepository().setURI(REMOTE_URL).setDirectory(localPath).call();
		}
		
		Repository repository = FileRepositoryBuilder.create(new File(localPath+"/.git"));
		Git git = new Git(repository);
        Ref head;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ObjectLoader loader;
        ByteArrayOutputStream byteArrayOutputStream;
        PrintStream ps;
        String content;
        long ts;
        for(Student s : students) {
            head = repository.getRef("refs/heads/PLM" + s.hashedUuid);

            if (head == null) { // create local branch
                try {
                    CreateBranchCommand create = git.branchCreate();
                    create.setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM);
                    create.setName("PLM" + s.hashedUuid);
                    create.setStartPoint("origin/PLM" + s.hashedUuid);
                    create.call();
                } catch (GitAPIException ex) {
                    //System.out.println(ex);
                }
                head = repository.getRef("refs/heads/PLM" + s.hashedUuid);
            }

            if (head == null) { // if it's still null
                lastActivity.add("0");
            } else {
                loader = repository.open(head.getObjectId());
                byteArrayOutputStream = new ByteArrayOutputStream();
                ps = new PrintStream(byteArrayOutputStream);
                loader.copyTo(ps);
                content = new String(byteArrayOutputStream.toByteArray(), "UTF-8");
                content = content.substring(129, 139);
                ts = Long.parseLong(content + "000") + 7200;
                lastActivity.add(sdf.format(ts));
            }
        }
		repository.close();

        return lastActivity;
	}
	
	public static ArrayList<Commit> computeCommits(String hashedUuid) throws IOException, GitAPIException {
		hashedUuid = "PLM"+hashedUuid;
		File localPath = new File("repo/");
		if (!localPath.exists()) {
			localPath.mkdir();

			// clone
			//System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
			Git.cloneRepository().setURI(REMOTE_URL).setDirectory(localPath).call();
		}
		
		Repository repository = FileRepositoryBuilder.create(new File(localPath+"/.git"));
		Git git = new Git(repository);

		git.checkout().setName("master").call();
		try {
			git.fetch().call();
		} catch (TransportException ex) {
			System.out.println("Not connected to Internet to fetch the repo.");
		}
		try {
			CreateBranchCommand create = git.branchCreate();
			create.setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM);
			create.setName(hashedUuid);
			create.setStartPoint("origin/" + hashedUuid);
			create.call();
		} catch (RefAlreadyExistsException ex) {

		}

		// checkout the branch of the current user
		git.checkout().setName(hashedUuid).call();
		
		try {
			git.pull().call();
		} catch (TransportException ex) {
			System.out.println("Not connected to Internet to fetch the repo.");
		}
		RevWalk walk = new RevWalk(repository);
		RevCommit commit = null;
		
		Iterable<RevCommit> logs = git.log().call();
		Iterator<RevCommit> i = logs.iterator();
			
		ArrayList<Commit> commits = new ArrayList<>();
		while (i.hasNext()) {
			commit = walk.parseCommit(i.next());
			String commitJson = commit.getFullMessage();
			commits.add(new Commit(commitJson, commit.getCommitTime(),commit.getName()));
		}
		repository.close();
		return commits;
	}
	
	public static Result displayBranch(String hashedUuid, String studentname) throws IOException, InvalidRemoteException, TransportException, GitAPIException, ParseException {
		ArrayList<Commit> commits = computeCommits(hashedUuid);
		ArrayList<Double> eventSummary = new ArrayList<>();
		int chartDay = 15;
		Integer[] startCount = new Integer[chartDay], switchCount = new Integer[chartDay], successCount = new Integer[chartDay], failCount = new Integer[chartDay];
		int cptEvt = 0;
		for(int i = 0; i<chartDay; i++) { // init eventCount for the chart
			startCount[i] = 0;
			switchCount[i] = 0;
			successCount[i] = 0;
			failCount[i] = 0;
		}
		
		for(int i = 0; i < 5; i++) { // init event distribution value
			eventSummary.add(0.0);
		}
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
		Calendar cal = Calendar.getInstance(), endRange = Calendar.getInstance(), beginRange = Calendar.getInstance();
		beginRange.add(Calendar.DAY_OF_YEAR, - (chartDay-1));
		Date dateParsed;
		boolean addToChartEvent = true;
		for(Commit c : commits) {
			cptEvt++;
			
			dateParsed = df.parse(c.commitTime); // get a Date object with the String
			cal.setTime(dateParsed); // use a Calendar
			if(cal.compareTo(endRange) > 0 || cal.compareTo(beginRange) < 0) { // if commit date is not in the range of the chart
				addToChartEvent = false;
			}
			
			switch(c.evt_type) {
				case "Switched":
					eventSummary.set(0, eventSummary.get(0)+1);
					if(addToChartEvent) {
						switchCount[cal.get(Calendar.DAY_OF_YEAR)-beginRange.get(Calendar.DAY_OF_YEAR)]++;
					}
				break;
				case "Success":
					eventSummary.set(1, eventSummary.get(1)+1);
					if(addToChartEvent) {
						successCount[cal.get(Calendar.DAY_OF_YEAR)-beginRange.get(Calendar.DAY_OF_YEAR)]++;
					}
				break;
				case "Failed":
                case "Compile err":
					eventSummary.set(2, eventSummary.get(2)+1);
					if(addToChartEvent) {
						failCount[cal.get(Calendar.DAY_OF_YEAR)-beginRange.get(Calendar.DAY_OF_YEAR)]++;
					}
				break;
				case "Start":
					eventSummary.set(3, eventSummary.get(3)+1);
					if(addToChartEvent) {
						startCount[cal.get(Calendar.DAY_OF_YEAR)-beginRange.get(Calendar.DAY_OF_YEAR)]++;
					}
				break;
				case "Help":
					eventSummary.set(4, eventSummary.get(4)+1);
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
			views.html.commits.render(commits, studentname, summary, eventSummary, startCount, switchCount, successCount, failCount, hashedUuid)
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
		for(String uuid : uuidList) { // for each student
			//System.out.println(uuid);
			checkoutUserBranch(uuid);
			int possible = 0, passed = 0 ;
			String p = course.programmingLanguage; // for the programming language
			possible = 0;
			
			passed = 0;
			Path sourcePath = Paths.get("repo/"+course.name+".summary");
			String summaryLine = "";
			try (BufferedReader reader = Files.newBufferedReader(sourcePath, StandardCharsets.UTF_8)) {
				summaryLine = reader.readLine();
				//System.out.println("Summary : "+ summaryLine);
				
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
			} catch (IOException ex) { // file does not exists maybe
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
				String[] languages = {"Java", "Python", "Scala", "C", "lightbot"};
				if (matcher.matches(name)) { // if file matches
					String s = name + "";
					String[] tab = s.split("\\.", 0);
					String lessonNameTmp = "";
					for (int i = 0; i <= tab.length - 2; i++) { // get the lesson id
						lessonNameTmp += tab[i]+"."; // build the lesson name
					}
					final String lessonName = lessonNameTmp;
					
					// Read lessonID.summary
					Path sourcePath = Paths.get("repo/"+lessonName+"summary"); // the last dot is added in the for loop
					String summaryLine = "";
					try (BufferedReader reader = Files.newBufferedReader(sourcePath, StandardCharsets.UTF_8)) {
						summaryLine = reader.readLine();
						//System.out.println("Summary : "+ summaryLine);
					} catch (IOException ex) {
						//System.out.println("Can't open "+sourcePath);
					}
					// Retrieve informations on per language progression
					JsonParser jsonParser = new JsonParser();
					//System.out.println(name + "  "+ sourcePath +"  :     "+ summaryLine);
					JsonObject jo = (JsonObject)jsonParser.parse(summaryLine);
					int possible = 0, passed = 0 ;
					for (final String p : languages) { // for each programming language, how many exercises are done/possible 
						possible = 0;
						passed = 0;
						try {
							possible = jo.get("possible"+p).getAsInt();
							try {
								passed = jo.get("passed"+p).getAsInt();
							} catch (Exception ex) { // passed information for the current language not available
							}
						} catch (Exception ex) { // in case possibleC is not in summary
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
