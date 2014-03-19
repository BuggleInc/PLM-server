package controllers;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

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

		repository.close();

		return ok("A remote repository has been cloned successfully.");
	}

}
