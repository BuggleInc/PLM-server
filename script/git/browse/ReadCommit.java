package git.browse;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

public class ReadCommit {

	private final static String repoUrl = "https://github.com/mquinson/PLM-data";
	private final static String repoPath = System.getProperty("user.home") + System.getProperty("file.separator")
			+ ".plm-data-browse";
	
	public static void main(String args[]) {
		File repoDirectory = new File(repoPath);
		
		if(!repoDirectory.isDirectory()) {
			System.out.println("Repo not yet existing, downloading it...");
			try {
				Git.cloneRepository()
					    .setURI(repoUrl).setDirectory(repoDirectory).call();
				System.out.println("Download finished!");
			} catch (GitAPIException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Repo already existing...");
		}
		
		Git git = null;
		try {
			git = Git.open(repoDirectory);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		List<Ref> branches = null;
		try {
			branches = git.branchList().setListMode(ListMode.REMOTE).call();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		System.out.println(branches.size() + " branches!");
		
		Iterable<RevCommit> commits = null;
		try {
			commits = git.log().all().call();
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		int i=0;
		for(RevCommit commit: commits) {
			i++;
			int commitTime = commit.getCommitTime();
			String commitMessage = commit.getFullMessage();
			String commitID = commit.getId().getName();
			int commitTree = commit.getTree().getType();
			
			
			if(i%1000 == 0) {
				System.out.println("commitTime: "+commitTime);
				System.out.println("commitMessage: "+commitMessage);
				System.out.println("commitID: "+commitID);
				System.out.println("commitTree: "+commitTree);
			}
		}
 		System.out.println(i+" commits!");
	}
}
