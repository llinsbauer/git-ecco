package at.jku.isse.gitecco;

import at.jku.isse.gitecco.git.GitCommitList;
import at.jku.isse.gitecco.git.GitHelper;
import org.logicng.datastructures.Assignment;
import org.logicng.datastructures.Tristate;
import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.logicng.io.parsers.PropositionalParser;
import org.logicng.solvers.MiniSat;
import org.logicng.solvers.SATSolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main app for the git to ecco tool.
 */
public class App {

    /**
     * Main method.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String... args) throws Exception {
        //final String repositoryPath = "C:\\obermanndavid\\git-to-ecco\\test_repo2";
        //old repo: not suitable anymore since we assume there will be only true/false expressions.
        //final String repositoryPath = "C:\\obermanndavid\\git-to-ecco\\test_repo3";
        //final String repositoryPath = "C:\\obermanndavid\\git-ecco-test\\test1\\Unity";

        final String repositoryPath = "C:\\obermanndavid\\git-ecco-test\\test2\\betaflight";
        final GitHelper gitHelper = new GitHelper(repositoryPath);
        final GitCommitList commits = new GitCommitList(repositoryPath);
        commits.enableAutoCommitConfigForEveryModelAndCntArtifacts();
        gitHelper.getAllCommits(commits);

        /*final String path = "C:\\obermanndavid\\git-ecco-test\\test2\\betaflight\\drv_bmp085.c";
        List<String> codelist = Files.readAllLines(Paths.get(path), StandardCharsets.ISO_8859_1);

        codelist.forEach(System.out::println);*/
    }

}
