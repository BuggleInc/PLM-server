package controllers;

import models.Student;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.RecursiveAction;

/**
 * Created by Ced on 07/09/2014.
 */
public class ForkLastActivity extends RecursiveAction {

    private String[] lastActivityString;

    private List<Student> students;

    private String repoNumber;

    private int start, length;

    public ForkLastActivity (List<Student> students, int start, int length, String repoNumber, String[] lastActivityString) {
        this.start = start;
        this.length = length;
        this.repoNumber = repoNumber;
        this.lastActivityString = lastActivityString;
        this.students = students;
    }

    protected void computeDirectly() {
        String hashedUuid, result = "";
        for(int i = start; i<start+length; i++) {
            hashedUuid = "PLM"+students.get(i).hashedUuid;
            File localPath = new File("repo-"+repoNumber+"/");
            try {
                if (!localPath.exists()) {
                    localPath.mkdir();

                    // clone
                    //System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
                    Git.cloneRepository().setURI(JGit.REMOTE_URL).setDirectory(localPath).call();
                }
                Repository repository = FileRepositoryBuilder.create(new File(localPath + "/.git"));
                Git git = new Git(repository);

                git.checkout().setName("master").call();
                try {
                    CreateBranchCommand create = git.branchCreate();
                    create.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM);
                    create.setName(hashedUuid);
                    create.setStartPoint("origin/" + hashedUuid);
                    create.call();
                } catch (RefAlreadyExistsException ex) {

                }

                // checkout the branch of the current user
                git.checkout().setName(hashedUuid).call();

//		try {
//			git.pull().call();
//		} catch (TransportException ex) {
//			System.out.println("Not connected to Internet to fetch the repo.");
//		}
                RevWalk walk = new RevWalk(repository);
                RevCommit commit = null;

                Iterable<RevCommit> logs = git.log().call();
                Iterator<RevCommit> iterator = logs.iterator();

                if (iterator.hasNext()) {
                    commit = walk.parseCommit(iterator.next());
                    result = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(commit.getCommitTime() * 1000L));
                }
                repository.close();
            } catch(IOException | GitAPIException e) {

            }
            lastActivityString[i] = result;
        }
    }


    @Override
    protected void compute() {
        if(length < 5) {
            computeDirectly();
            return;
        }

        int split = length / 2;

        invokeAll(new ForkLastActivity(students, start, split, repoNumber+"0", lastActivityString),
                new ForkLastActivity(students, start + split, length-split, repoNumber+"1", lastActivityString));
    }
}
