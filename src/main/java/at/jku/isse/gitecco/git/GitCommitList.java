package at.jku.isse.gitecco.git;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for storing a row of commits
 * With the option to react to added commits
 * depending on their type.
 */
public class GitCommitList extends ArrayList<GitCommit> {
    private final List<GitCommitListener> observersC = new ArrayList();
    private final List<GitBranchListener> observersB = new ArrayList();
    private final List<GitMergeListener> observersM = new ArrayList();

    public GitCommitList(List<GitCommit> l) {
        super(l);
    }

    public GitCommitList() {
        super();
    }

    /**
     * Adds a GitCommitListener to the Object.
     * @param gcl
     */
    public void addGitCommitListener(GitCommitListener gcl) {
        observersC.add(gcl);
    }

    /**
     * Adds a GitMergeListener to the Object.
     * @param gml
     */
    public void addGitMergeListener(GitMergeListener gml) {
        observersM.add(gml);
    }

    /**
     * Adds a GitBranchListener to the Object.
     * @param gbl
     */
    public void addGitBranchListener(GitBranchListener gbl) {
        observersB.add(gbl);
    }

    /**
     * adds an element to the list.
     * also triggers the observers.
     * @param gitCommit
     * @return
     */
    @Override
    public boolean add(GitCommit gitCommit) {
        notifyObservers(gitCommit);
        return super.add(gitCommit);
    }

    private void notifyObservers(GitCommit gc) {
        for (GitCommitListener oc : observersC) {
            oc.onCommit(gc);
        }
        for (GitCommitType gct : gc.getType()) {
            if(gct.equals(GitCommitType.BRANCH)) {
                for (GitBranchListener ob : observersB) {
                    ob.onBranch(gc);
                }
            }
            if(gct.equals(GitCommitType.MERGE)) {
                for (GitMergeListener gm : observersM) {
                    gm.onMerge(gc);
                }
            }
        }
    }

}
