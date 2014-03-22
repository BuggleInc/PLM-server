package controllers;

import java.io.File;
import java.io.IOException;

import java.util.Calendar;
import java.util.Iterator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.jcraft.jsch.JSchException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import play.mvc.Controller;
import play.mvc.Result;

public class JGit extends Controller {
	private static final String REMOTE_URL = "https://PLM-Test@bitbucket.org/PLM-Test/plm-test-repo.git";

	public static Result index() {
		return ok("You called the index method of the Git controller.");
	}

	public static Result cloneRepo() throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		// prepare a new folder for the cloned repository
		File localPath = File.createTempFile("TestGitRepository", "");
		localPath.delete();

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
			s = s + commit.getFullMessage() + "<br />\n";

		}
		repository.close();
		
		System.out.println(s);
		
		return ok(s);
	}

}
