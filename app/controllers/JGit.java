package controllers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Course;
import models.Feedback;
import models.GitEvent;
import models.ProgressItem;
import models.Student;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import play.mvc.Controller;
import play.mvc.Result;
import utils.GitUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class JGit extends Controller {
	public static Result index() {
		return ok("You called the index method of the Git controller.");
	}

	public static Result fetchRepoOnDemand() {
		GitUtils.fetchRepo(false);
		return ok(
				views.html.home.render(request().username())
		);
	}

	public static ArrayList<String> getLastActivity(List<Student> students) throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		ArrayList<String> lastActivity = new ArrayList<>();
		int idx;
		File localPath = new File("repo/");
		if (!localPath.exists()) {
			localPath.mkdir();
			Git.cloneRepository().setURI(GitUtils.REMOTE_URL).setDirectory(localPath).call();
		}

		Repository repository = FileRepositoryBuilder.create(new File(localPath + "/.git"));
		Git git = new Git(repository);
		Ref head;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ObjectLoader loader;
		ByteArrayOutputStream byteArrayOutputStream;
		PrintStream ps;
		String content;
		long ts;
		for (Student s : students) {
			head = repository.getRef("refs/remotes/origin/PLM" + s.hashedUuid);

			if (head == null) { // create local branch if ref not found
				try {
					CreateBranchCommand create = git.branchCreate();
					create.setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM);
					create.setName("PLM" + s.hashedUuid);
					create.setStartPoint("origin/PLM" + s.hashedUuid);
					create.call();
				} catch (GitAPIException ex) {
					//System.out.println(ex);
				}
				head = repository.getRef("refs/heads/PLM" + s.hashedUuid); // try again to retrieve branch info
			}

			if (head == null) { // if it's still null
				lastActivity.add("0"); // to have students and lastActivity with the same length
			} else {
				loader = repository.open(head.getObjectId()); // read info
				byteArrayOutputStream = new ByteArrayOutputStream();
				ps = new PrintStream(byteArrayOutputStream);
				loader.copyTo(ps);
				content = new String(byteArrayOutputStream.toByteArray(), "UTF-8"); // export info as a String
				//System.out.println(content);
				idx = content.indexOf(">"); // to find the timestamp
				content = content.substring(idx + 2, idx + 12);
				ts = Long.parseLong(content + "000"); // timestamps
				lastActivity.add(sdf.format(ts));
			}
		}
		repository.close();

		return lastActivity;
	}

	public static Result displayBranch(String hashedUuid, String studentname) throws IOException, InvalidRemoteException, TransportException, GitAPIException, ParseException {
		ArrayList<GitEvent> commits = GitUtils.computeCommits(hashedUuid);
		ArrayList<Double> eventSummary = new ArrayList<>();
		int chartDay = 15;
		Integer[] startCount = new Integer[chartDay], switchCount = new Integer[chartDay], successCount = new Integer[chartDay], failCount = new Integer[chartDay];
		int cptEvt = 0;
		for (int i = 0; i < chartDay; i++) { // init eventCount for the chart
			startCount[i] = 0;
			switchCount[i] = 0;
			successCount[i] = 0;
			failCount[i] = 0;
		}

		for (int i = 0; i < 5; i++) { // init event distribution value
			eventSummary.add(0.0);
		}
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
		Calendar cal = Calendar.getInstance(), endRange = Calendar.getInstance(), beginRange = Calendar.getInstance();
		beginRange.add(Calendar.DAY_OF_YEAR, -(chartDay - 1));
		Date dateParsed;
		boolean addToChartEvent = true;
		for (GitEvent c : commits) {
			cptEvt++;

			dateParsed = df.parse(c.commitTime); // get a Date object with the String
			cal.setTime(dateParsed); // use a Calendar
			if (cal.compareTo(endRange) > 0 || cal.compareTo(beginRange) < 0) { // if commit date is not in the range of the chart
				addToChartEvent = false;
			}

			switch (c.evt_type) {
				case "Switched":
					eventSummary.set(0, eventSummary.get(0) + 1);
					if (addToChartEvent) {
						switchCount[cal.get(Calendar.DAY_OF_YEAR) - beginRange.get(Calendar.DAY_OF_YEAR)]++;
					}
					break;
				case "Success":
					eventSummary.set(1, eventSummary.get(1) + 1);
					if (addToChartEvent) {
						successCount[cal.get(Calendar.DAY_OF_YEAR) - beginRange.get(Calendar.DAY_OF_YEAR)]++;
					}
					break;
				case "Failed":
				case "Compile err":
					eventSummary.set(2, eventSummary.get(2) + 1);
					if (addToChartEvent) {
						failCount[cal.get(Calendar.DAY_OF_YEAR) - beginRange.get(Calendar.DAY_OF_YEAR)]++;
					}
					break;
				case "Start":
					eventSummary.set(3, eventSummary.get(3) + 1);
					if (addToChartEvent) {
						startCount[cal.get(Calendar.DAY_OF_YEAR) - beginRange.get(Calendar.DAY_OF_YEAR)]++;
					}
					break;
				case "Help":
					eventSummary.set(4, eventSummary.get(4) + 1);
					break;
			}
		}
		cptEvt -= 2; // 2 useless commits for statistics: "Empty initial commit" and "Create README.md"
		for (int j = 0; j < eventSummary.size(); j++) {
			eventSummary.set(j, eventSummary.get(j) * 100 / cptEvt);
		}

		final ArrayList<ProgressItem> summary = new ArrayList<>();

		computeProgress(summary, "PLM" + hashedUuid);

		return ok(
				views.html.gitevent.render(commits, studentname, summary, eventSummary, startCount, switchCount, successCount, failCount, hashedUuid)
		);
	}

	/**
	 * Compute the progression of students for a given course
	 *
	 * @param hashedList the list of students uuid
	 * @param course     the course
	 */
	public static ArrayList<ProgressItem> computeStudentForLesson(ArrayList<String> hashedList, Course course) throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		JGit.fetchRepoOnDemand();
		ArrayList<ProgressItem> summary = new ArrayList<>();
		File localPath = new File("repo/");
		int possible, passed;
		ByteArrayOutputStream byteArrayOutputStream;
		PrintStream ps;
		JsonParser jsonParser;
		JsonObject jo;
		String content;
		String programmingLanguage = course.programmingLanguage, courseName = course.name; // for the course programming language
		ObjectId lastCommitId;
		Repository repository = FileRepositoryBuilder.create(new File(localPath + "/.git"));

		for (String hashedUuid : hashedList) { // for each student
			lastCommitId = repository.resolve("refs/remotes/origin/PLM" + hashedUuid);
            if(lastCommitId == null) {
                summary.add(new ProgressItem(courseName, programmingLanguage, 1, -1));
                continue;
            }
			RevWalk revWalk = new RevWalk(repository);
			RevCommit commit = revWalk.parseCommit(lastCommitId);
			RevTree tree = commit.getTree();
			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(tree);
			treeWalk.setRecursive(false);
			treeWalk.setFilter(PathFilter.create(courseName + ".summary"));
			if (!treeWalk.next()) {
				summary.add(new ProgressItem(courseName, programmingLanguage, 1, -1)); // file does not exists in git
				revWalk.dispose();
			} else {
				ObjectId objectId = treeWalk.getObjectId(0);
				ObjectLoader loader = repository.open(objectId);

				// and then one can the loader to read the file
				byteArrayOutputStream = new ByteArrayOutputStream();
				ps = new PrintStream(byteArrayOutputStream);
				loader.copyTo(ps);
				content = new String(byteArrayOutputStream.toByteArray(), "UTF-8"); // export info as a String

				revWalk.dispose();
				passed = -1;
				possible = 0;

				jsonParser = new JsonParser();
				jo = (JsonObject) jsonParser.parse(content);

				try {
					possible = jo.get("possible" + programmingLanguage).getAsInt();
					try {
						passed = jo.get("passed" + programmingLanguage).getAsInt();
					} catch (Exception ex) { // passed information for the current language not available
					}
				} catch (Exception ex) { // in case a language is not in summary
				}
				//System.out.println(course.name + "   " + p + "   " + possible + ", " + passed +" done");
				if (passed > -1) {
					summary.add(new ProgressItem(courseName, programmingLanguage, possible, passed));
				} else { // not attemp
					summary.add(new ProgressItem(courseName, programmingLanguage, 1, -1));
				}
			}
		}
		repository.close();
		return summary;
	}

	/**
	 * Compute progression for the current repo state
	 */
	private static void computeProgress(final ArrayList<ProgressItem> summary, String branchName) {
		String pattern = ".*summary", content;
		String[] languages = {"Java", "Python", "Scala", "C", "lightbot"};
		int possible, passed;
		ByteArrayOutputStream byteArrayOutputStream;
		PrintStream ps;
		JsonParser jsonParser = new JsonParser();
		JsonObject jo;

		File localPath = new File("repo/");
		try {
			Repository repository = FileRepositoryBuilder.create(new File(localPath + "/.git"));

			Ref ref = repository.getRef("refs/remotes/origin/" + branchName);
			RevWalk walk = new RevWalk(repository);
			RevCommit commit = walk.parseCommit(ref.getObjectId());
			RevTree revtree = walk.parseTree(commit.getTree().getId());
			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(revtree);
			treeWalk.setRecursive(false); // summary files are at root
			while (treeWalk.next()) {
				if (treeWalk.getPathString().matches(pattern)) {
					String lessonName = treeWalk.getPathString().substring(0, treeWalk.getPathString().length() - 8); // remove .summary to file name
					ObjectId lastCommitId = repository.resolve("refs/remotes/origin/" + branchName);

					RevWalk revWalk = new RevWalk(repository);
					RevCommit studentCommits = revWalk.parseCommit(lastCommitId);
					RevTree tree = studentCommits.getTree();

					TreeWalk studentTreeWalk = new TreeWalk(repository);
					studentTreeWalk.addTree(tree);
					studentTreeWalk.setRecursive(false);
					studentTreeWalk.setFilter(PathFilter.create(lessonName + ".summary"));
					if (!studentTreeWalk.next()) {
						//System.out.println("Did not find expected file 'welcome.summary'");
					}

					ObjectId objectId = studentTreeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);

					byteArrayOutputStream = new ByteArrayOutputStream();
					ps = new PrintStream(byteArrayOutputStream);
					loader.copyTo(ps);
					content = new String(byteArrayOutputStream.toByteArray(), "UTF-8"); // export info as a String
					revWalk.dispose();

					jo = (JsonObject) jsonParser.parse(content);
					for (final String p : languages) { // for each programming language, how many exercises are done/possible
						possible = 0;
						passed = -1;
						try {
							possible = jo.get("possible" + p).getAsInt();
							try {
								passed = jo.get("passed" + p).getAsInt();
							} catch (Exception ex) { // passed information for the current language not available
							}
						} catch (Exception ex) { // in case a language is not in summary
						}
						//System.out.println(lessonName + "   " + p + "   " + possible + ", " + passed +" done");
						if (passed >= 0) {
							summary.add(new ProgressItem(lessonName, p, possible, passed));
						}
					}
				}
			}
			walk.dispose();
			repository.close();
		} catch (IOException e) {//} catch (IOException| GitAPIException e) {
			System.out.println(e);
		}
	}

	public static ArrayList<Feedback> getFeedBack(List<Student> students) throws IOException, GitAPIException {
		ArrayList<Feedback> feedbacks = new ArrayList<>();
		File localPath = new File("repo/");
		if (!localPath.exists()) {
			localPath.mkdir();
			Git.cloneRepository().setURI(GitUtils.REMOTE_URL).setDirectory(localPath).call();
		}

		Repository repository = FileRepositoryBuilder.create(new File(localPath + "/.git"));
		Git git = new Git(repository);
		Ref head;
		JsonParser jsonParser = new JsonParser();
		JsonObject jo;
		for (Student s : students) {
			head = repository.getRef("refs/remotes/origin/PLM" + s.hashedUuid);

			if (head == null) { // create local branch if ref not found
				try {
					CreateBranchCommand create = git.branchCreate();
					create.setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM);
					create.setName("PLM" + s.hashedUuid);
					create.setStartPoint("origin/PLM" + s.hashedUuid);
					create.call();
				} catch (GitAPIException ex) {
					//System.out.println(ex);
				}
				head = repository.getRef("refs/heads/PLM" + s.hashedUuid); // try again to retrieve branch info
			}

			if (head != null) {
				RevWalk walk = new RevWalk(repository);

				RevCommit commit = walk.parseCommit(head.getObjectId());
				walk.markStart(commit);
				for (RevCommit rev : walk)
					if (rev.getFullMessage().contains("exoDifficulty")) {
						jo = (JsonObject) jsonParser.parse(rev.getFullMessage());
						Feedback feedback = new Feedback();
						try {
							feedback.exercise = jo.get("exo").getAsString();
							feedback.exoInterest = jo.get("exoInterest").getAsString();
							feedback.exoDifficulty = jo.get("exoDifficulty").getAsString();
							feedback.exoComment = jo.get("exo").getAsString();
						} catch (Exception ex) {
							feedback.exercise = "";
							feedback.exoInterest = "";
							feedback.exoDifficulty = "";
							feedback.exoComment = "";
						}
						feedbacks.add(feedback);
					}
				walk.dispose();
			}
		}
		repository.close();

		return feedbacks;
	}

	public static ArrayList<GitEvent> computeCommits(String hashedUuid) {
		// TODO Auto-generated method stub
		return null;
	}

}
