package git.browse;

import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;

import plm.core.lang.ProgrammingLanguage;
import plm.core.model.Game;

public class CommitThread extends Thread {

	private GitEvent commit;
	private PrintStream ps;
	private int nbCo;
	private Student student;

	public CommitThread(GitEvent commit, PrintStream ps, int nbCo, Student student) {
		this.commit = commit;
		this.ps = ps;
		this.nbCo = nbCo;
		this.student = student;
	}

	@Override
	public void run() {
		synchronized(this) {
			System.out.println("Thread start ("+commit.codeLink+")");
			if(commit.isValid() && commit.evt_type.equalsIgnoreCase("Failed")) {
				nbCo++;
				ps.println(nbCo + " --> " + commit.codeLink + " : ");
				try {
					ps.println(GitUtils.getSource(commit));
				} catch (MissingObjectException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IncorrectObjectTypeException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (CorruptObjectException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				ps.println("------------------");

				System.out.println(nbCo+ " --> " + commit.codeLink + " : ");
				//System.out.println(GitUtils.getSource(commit));
				System.out.println("------------------");

				ProgrammingLanguage lang1 = Game.JAVA;
				switch(commit.exolang.toLowerCase()) {
				case "scala": 
					lang1 = Game.SCALA;
					break;
				case "python":
					lang1 = Game.PYTHON;
					break;
				}
				final ProgrammingLanguage lang = lang1;

				String lessonID = "lessons."+commit.exoname.split(".lessons.")[0];

				String exoID = commit.exoname;

				if(HarvesterPIDR.lessonsName.contains(lessonID)) {
					String code;
					try {
						code = GitUtils.getSource(commit);
						HarvesterPIDR.execCode(commit, student.name, code, lang, lessonID, exoID);
						Thread.sleep(50);
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			//System.out.println("Thread stop ("+commit.codeLink+")");
			notify();
		}
	}
	
	public int getNbCo() {
		return nbCo;
	}

}
