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
		
		Map<String,Integer> passed = new HashMap<String,Integer>();
		Map<String,Integer> failed = new HashMap<String,Integer>();
		Map<String,Integer> compile = new HashMap<String,Integer>();
		Map<String,Integer> switched = new HashMap<String,Integer>();
		Map<String,Integer> start = new HashMap<String,Integer>();
		Map<String,Integer> help = new HashMap<String,Integer>();
		Map<String,Integer> tip = new HashMap<String,Integer>();
		Map<String,Integer> revert = new HashMap<String,Integer>();
		
		int totalCommits = 0;
		int branchesComputed = 0;
		
		for(Ref branch:branches) {
			if(branch.getName().contains("PLM")) {
				String branchName = branch.getName().substring(20);
				ArrayList<Commit> commits = JGit.computeCommits(branchName.substring(3));
				totalCommits += commits.size();
				for(Commit commit: commits) {
					Map<String,Integer> whereTo = null;
					switch (commit.evt_type) {
					case "Success":
						whereTo = passed;
						break;
					case "Failed": 
						whereTo = failed;
						break;
					case "Compilation error":
						whereTo = compile;
						break;
					case "Help":
						whereTo = help;
						break;
					case "Start":
						whereTo = start;
						break;
					case "Switched":
						whereTo = switched;
						break;
					case "Reverted":
						whereTo = revert;
						break;
					case "Stop":
						break;
					case "Read": // Tip
						whereTo = tip;
						break;
					default: 
						//System.err.println("Unhandled evt_type: '"+commit.evt_type+"' ("+commit.codeLink+")");
					}

					if (whereTo == null)
						continue;
					
					if (whereTo.get(branchName) == null) {
						whereTo.put(branchName,1);
					} else {
						whereTo.put(branchName, whereTo.get(branchName) + 1);
					}
				}
				branchesComputed++;
				if(branchesComputed%50 ==0) {
					System.out.println(branchesComputed + " branches done!");
				}
			}
		}
		
		System.out.println("# branch, passed, failed, compile, help, tip, start, switch, revert");
		// Display the stats about the branches
		for (Ref branch : branches) {
			String branchName = branch.getName().substring(20);
			System.out.println(
					branchName+", "+
					display(passed.get(branchName))+
					display(failed.get(branchName))+
					display(compile.get(branchName))+
					display(help.get(branchName))+
					display(tip.get(branchName))+
					display(start.get(branchName))+
					display(switched.get(branchName))+
					display(revert.get(branchName))+

					" ");
		}
		
		System.out.println("There is "+ totalCommits + " commits!");
	}
	static private String display(Integer val) {
		if (val == null) 
			return "0, ";
		return ""+val+", ";
	}
}
