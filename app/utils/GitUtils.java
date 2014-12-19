package utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import models.GitEvent;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
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

public class GitUtils {
	public static final String REMOTE_URL = "https://github.com/mquinson/PLM-data.git";
	public static final File LOCAL_PATH = new File("repo/");
	public static Repository repository = null;
	
	public static void fetchRepo(Boolean refresh) throws InvalidRemoteException, TransportException, GitAPIException {
		if (!LOCAL_PATH.exists()) {
			LOCAL_PATH.mkdir();

			// clone
			System.err.println("Cloning from " + REMOTE_URL + " to " + LOCAL_PATH);
			Git.cloneRepository().setURI(REMOTE_URL).setDirectory(LOCAL_PATH).call();
		}

		try {
			repository = FileRepositoryBuilder.create(new File(LOCAL_PATH + "/.git"));

			//git.checkout().setName("master").call();

			if (refresh) {
				System.err.println("Fetch all remote events");
				new Git(repository).fetch().setCheckFetchedObjects(true).call();
			} else {
				System.err.println("Repository already there; don't fetch remote changes");
			}

			repository.close();
		} catch (IOException | GitAPIException e) {
			System.out.println(e);
		}

	}


	public static ArrayList<GitEvent> computeCommits(String hashedUuid) throws IOException, GitAPIException {
		hashedUuid = "PLM" + hashedUuid;

		if (repository == null)
			repository = FileRepositoryBuilder.create(new File(new File("repo/") + "/.git"));
		
		Ref ref = repository.getRef("refs/remotes/origin/" + hashedUuid);
		RevWalk walk = new RevWalk(repository);
		RevCommit startCommit = walk.parseCommit(ref.getObjectId());
		walk.markStart(startCommit);
		ArrayList<GitEvent> commits = new ArrayList<>();

		for (RevCommit rev : walk) 
			commits.add(new GitEvent(rev));

		walk.dispose();

		return commits;
	}

	static public String getFileContent(RevCommit commit, String path) throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		if (repository == null)
			repository = FileRepositoryBuilder.create(new File(new File("repo/") + "/.git"));

		// Using commit's tree, find the path
		RevTree tree = commit.getTree();
		TreeWalk treeWalk = new TreeWalk(repository);
		treeWalk.addTree(tree);
		treeWalk.setRecursive(true);
		treeWalk.setFilter(PathFilter.create(path));
		if (!treeWalk.next()) 
			return null;
		
		// Get a stream loader onto that object
		ObjectId objectId = treeWalk.getObjectId(0);
		ObjectLoader loader = repository.open(objectId);
		InputStream is = loader.openStream();		
		
		// And convert the stream into a String object -- damn java
		final char[] buffer = new char[2048];
		final StringBuilder out = new StringBuilder();

		final Reader in = new InputStreamReader(is, "UTF-8");
		try {
			for (;;) {
				int rsz = in.read(buffer, 0, buffer.length);
				if (rsz < 0)
					break;
				out.append(buffer, 0, rsz);
			}
		}
		finally {
			in.close();
		}
		return out.toString();
	}
	static Map<String,String> langExt=null;
	static private void initLangExt() {
		langExt = new HashMap<String, String>();
		langExt.put("Python","py");
		langExt.put("Java","java");
		langExt.put("C","c");
		langExt.put("Scala","scala");
		langExt.put("lightbot","ignored");
	}

	static public final String getLangFile(GitEvent commit, String fmtName) throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		if (langExt == null) 
			initLangExt();

		String file = String.format(fmtName, langExt.get(commit.exolang));
		
		String source = GitUtils.getFileContent(commit.rev, file);
		if (source == null) {
			for (String ext: langExt.values()) {
				file = String.format(fmtName, ext);
				source = GitUtils.getFileContent(commit.rev, file);
				if (source != null)
					break;
			}
		}
		return source;
	}
	
	static public final String getSource(GitEvent commit) throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		return getLangFile(commit, commit.exoname+".%s.code");
	}
	static public final String getError(GitEvent commit) throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		return getLangFile(commit, commit.exoname+".%s.error");
	}
}