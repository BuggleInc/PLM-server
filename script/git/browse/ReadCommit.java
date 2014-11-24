package git.browse;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

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
		
		// Retrieve all commits
		Iterable<RevCommit> commits = git.log().all().call();

	    RevWalk walk = new RevWalk(repo);
		// Traverse all commits, accumulating some statistics about it
		int totalCount=0;
		
		Map<String,Integer> passed = new HashMap<String,Integer>();
		Map<String,Integer> failed = new HashMap<String,Integer>();
		Map<String,Integer> compile = new HashMap<String,Integer>();
		Map<String,Integer> switched = new HashMap<String,Integer>();
		Map<String,Integer> start = new HashMap<String,Integer>();
		Map<String,Integer> help = new HashMap<String,Integer>();
		Map<String,Integer> tip = new HashMap<String,Integer>();
		Map<String,Integer> revert = new HashMap<String,Integer>();
		
		for(RevCommit rev: commits) {
			totalCount++;
		} 
		System.out.print("Reading all "+totalCount+" commits...");

		totalCount = 0;
		commits = git.log().all().call();
		for(RevCommit rev: commits) {
			totalCount++;
			Commit commit = new Commit(rev.getFullMessage(), rev.getCommitTime(), rev.getName());

			if(totalCount%100 == 0) {
				if (totalCount % 1000 == 0) {
					System.out.print("("+totalCount+" done)");
					if (totalCount % 5000 == 0)
						System.out.println();
				} else {
					System.out.print(".");
				}
//				System.out.println("outcome:" + commit.outcome);
//				System.out.println("commitTime: "+rev.getCommitTime());
//				System.out.println("commitMessage: "+rev.getFullMessage());
//				System.out.println("commitTree: "+rev.getTree());
			}
			
			RevCommit targetCommit = walk.parseCommit(repo.resolve(rev.getName()));
			for (Ref branch : branches) {
				if (walk.isMergedInto(targetCommit, walk.parseCommit(branch.getObjectId()))) {
					String branchName = branch.getName().substring(20);

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
						if (!rev.getFullMessage().equals("Empty initial commit") &&
							!rev.getFullMessage().equals("Initial commit") &&
							!rev.getFullMessage().startsWith("Merge remote-tracking branch 'origin/PLM") &&
							!rev.getFullMessage().equals("Manual merging")) {
							System.err.println("Unhandled evt_type: '"+commit.evt_type+"' ("+rev.getFullMessage()+")");
						}
					}

					if (whereTo == null)
						continue;
					
					if (whereTo.get(branchName) == null) {
						whereTo.put(branchName,1);
					} else {
						whereTo.put(branchName, whereTo.get(branchName) + 1);
					}
				}
			}
		}
		System.out.println(" done.");
		
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
	}
	static private String display(Integer val) {
		if (val == null) 
			return "0, ";
		return ""+val+", ";
	}
}
