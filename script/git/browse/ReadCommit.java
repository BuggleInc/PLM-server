package git.browse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Commit;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import controllers.JGit;

/* This code is terribly inefficient as we are looping over all the commits and for each of them, we seach for all branches the commit in which it is... */
// http://stackoverflow.com/questions/15822544/jgit-how-to-get-all-commits-of-a-branch-without-changes-to-the-working-direct

public class ReadCommit {

	private final static String repoUrl = "https://github.com/mquinson/PLM-data";
	private final static String repoPath = System.getProperty("user.home") + System.getProperty("file.separator")
			+ ".plm-data-browse";

	public static void main(String args[]) throws Exception {
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

		Repository repo =  new FileRepositoryBuilder()
		.setWorkTree(repoDirectory) 
		.build();
		Git git = new Git(repo);//Git.open(repoDirectory);


		List<Ref> branches = git.branchList().setListMode(ListMode.REMOTE).call();
		System.out.println("There is "+ branches.size() + " branches!");

		int totalCommits = 0;
		int branchRank = 0;

		for(Ref branch:branches) {
			if(branch.getName().contains("PLM")) {				
				String branchName = branch.getName().substring(20);
				Student student = new Student(branchName);
				
				if (student.exoAttempted.size()>20) {
					branchRank++;
					if (branchRank % 20 == 0)
						System.out.println("# branch, passedNoDupplicate, attemptedNoDupplicate, passed, failed, compile, help, tip, start, switch, revert");

					System.out.println(student);
				}
			}
		}

		System.out.println("There is "+ totalCommits + " valid commits!");
	}
	static private String display(Integer val) {
		if (val == null) 
			return "0, ";
		return ""+val+", ";
	}
	
}
