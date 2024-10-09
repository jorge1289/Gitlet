package gitlet;
import java.io.File;
import java.util.Arrays;
import java.util.Date;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Jorge Emanuel Nunez
 */
public abstract class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> ....
     *  java gitlet.Main add hello.txt*/

    private static File CWD = new File(System.getProperty("user.dir"));
    private static Repository repo = new Repository();


    public static void main(String... args) {
        if (args[0].equals("add")) {
            repo.add(args[1]);
        }
        if (args[0].equals("init")) {
            repo.init();
        }
        if (args[0].equals("commit")) {
            repo.commit(args[1]);
        }
        if (args[0].equals("log")) {
            repo.log();
        }
        if (args[0].equals("checkout")) {
            repo.checkout(args);
        }
        if (args[0].equals("branch")) {
            repo.branch(args[1]);
        }
        if (args[0].equals("global-log")) {
            repo.globalLog();
        }
        if (args[0].equals("find")) {
            repo.find(args[1]);
        }
        if (args[0].equals("rm-branch")) {
            repo.rmBranch(args[1]);
        }
        if (args[0].equals("status")) {
            repo.status();
        }
        if (args[0].equals("reset")) {
            repo.reset(args[1]);
        }
        if (args[0].equals("rm")) {
            repo.rm(args[1]);
        }
    }
}
