package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
import java.util.Collection;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Arrays;
import java.util.ArrayList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
	private static final String REMOTE_URL = "https://PLM-Test@bitbucket.org/PLM-Test/plm-test-repo.git";
	
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
			String commitMsg;
			commits.add(new Commit(commitJson, commit.getCommitTime()));
		}
		repository.close();
		return commits;
	}
	
	public static Result displayBranch(String uuid, String studentname) throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		ArrayList<Commit> commits = computeCommits(uuid);
		ArrayList<Double> eventSummary = new ArrayList<>();
		eventSummary.add(0.0);eventSummary.add(0.0);eventSummary.add(0.0);eventSummary.add(0.0);
		int cptEvt = 0;
		for(Commit c : commits) {
			cptEvt++;
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
		cptEvt--;
		for(int j =0; j<eventSummary.size(); j++) {
			eventSummary.set(j, eventSummary.get(j)*100/cptEvt);
		}
		final File path = new File("repo");
		
		final ArrayList<ProgressItem> summary = new ArrayList<>();
		
		passed = 0;
		
		String pattern = "*.[0-9]*";
		FileSystem fs = FileSystems.getDefault();
		final PathMatcher matcher = fs.getPathMatcher("glob:" + pattern); // to match file names ending with digits

		FileVisitor<Path> matcherVisitor = new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attribs) {
				Path name = file.getFileName();
				String[] languages = {"java", "py", "scala"};
				if (matcher.matches(name)) { // if the file exists, the tests were run at least once
					String s = name + "";
					String[] tab = s.split("\\.", 0);
					String lessonNameTmp = "";
					for (int i = 0; i < tab.length - 2; i++) { // get the lesson id
						lessonNameTmp += tab[i];
					}
					final String lessonName = lessonNameTmp;
					String ext = tab[tab.length - 2]; // get the programming language
					int possible = Integer.parseInt(tab[tab.length - 1]); // get the number of exercises
					if (possible > 0) {
						for (final String p : languages) { // for each programming language, how many exercises are done
							if (p.equals(ext)) {
								//System.out.println(lessonName + "   " + p + "   " + possible);
								//Game.getInstance().studentWork.setPossibleExercises((String) lessonName, p, possible);
								String pattern = lessonName + ".*." + p + ".DONE";
								FileSystem fs = FileSystems.getDefault();
								final PathMatcher matcher = fs.getPathMatcher("glob:" + pattern);

								FileVisitor<Path> matcherVisitor = new SimpleFileVisitor<Path>() {

									@Override
									public FileVisitResult visitFile(Path file, BasicFileAttributes attribs) {
										Path name = file.getFileName();
										if (matcher.matches(name)) {
											passed = passed + 1; // incr each time we found a correctly done exercise for the programming language p
										}
										return FileVisitResult.CONTINUE;
									}
								};
								try {
									passed = 0;
									Files.walkFileTree(Paths.get(path.getPath()), matcherVisitor);
								} catch (IOException ex) {

								}
								System.out.println(lessonName + "   " + p + "   " + possible + ", " + passed +" done");
								summary.add(new ProgressItem(lessonName, p, possible, passed));
							}
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
		
		return ok(
			views.html.commits.render(commits, studentname, summary, eventSummary)
			);
	}
	
	public static ArrayList<ProgressItem> computeStudentForLesson(ArrayList<String> uuidList, final String lessonname) throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		final ArrayList<ProgressItem> summary = new ArrayList<>();
		int cpt = 0;
		for(String uuid : uuidList) {
		cpt++;
			ArrayList<Commit> commits = computeCommits(uuid);
			ArrayList<Double> eventSummary = new ArrayList<>();
			eventSummary.add(0.0);eventSummary.add(0.0);eventSummary.add(0.0);eventSummary.add(0.0);

			final File path = new File("repo");
			
			passed = 0;
			
			String pattern = "*.[0-9]*";
			FileSystem fs = FileSystems.getDefault();
			final PathMatcher matcher = fs.getPathMatcher("glob:" + pattern); // to match file names ending with digits

			FileVisitor<Path> matcherVisitor = new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attribs) {
					Path name = file.getFileName();
					String[] languages = {"java"};
					if (matcher.matches(name)) { // if the file exists, the tests were run at least once
						String s = name + "";
						String[] tab = s.split("\\.", 0);
						String lessonNameTmp = "";
						for (int i = 0; i < tab.length - 2; i++) { // get the lesson id
							lessonNameTmp += tab[i];
						}
						if(lessonNameTmp.equals(lessonname)) {
							final String lessonName = lessonNameTmp;
							String ext = tab[tab.length - 2]; // get the programming language
							int possible = Integer.parseInt(tab[tab.length - 1]); // get the number of exercises
							if (possible > 0) {
								for (final String p : languages) { // for each programming language, how many exercises are done
									if (p.equals(ext)) {
										//System.out.println(lessonName + "   " + p + "   " + possible);
										//Game.getInstance().studentWork.setPossibleExercises((String) lessonName, p, possible);
										String pattern = lessonName + ".*." + p + ".DONE";
										FileSystem fs = FileSystems.getDefault();
										final PathMatcher matcher = fs.getPathMatcher("glob:" + pattern);

										FileVisitor<Path> matcherVisitor = new SimpleFileVisitor<Path>() {

											@Override
											public FileVisitResult visitFile(Path file, BasicFileAttributes attribs) {
												Path name = file.getFileName();
												if (matcher.matches(name)) {
													passed = passed + 1; // incr each time we found a correctly done exercise for the programming language p
												}
												return FileVisitResult.CONTINUE;
											}
										};
										try {
											passed = 0;
											Files.walkFileTree(Paths.get(path.getPath()), matcherVisitor);
										} catch (IOException ex) {

										}
										System.out.println(lessonName + "   " + p + "   " + possible + ", " + passed +" done");
										summary.add(new ProgressItem(lessonName, p, possible, passed));
									}
								}
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
			if(cpt != summary.size()) {	// the files concerning the lesson doesn't exists
				summary.add(new ProgressItem(lessonname, "java", 1, -1));
			}
		}
		return summary;
	}

}
