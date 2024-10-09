package gitlet;

import afu.org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;


public class Repository {
    private File CWD = new File(System.getProperty("user.dir"));
    static final File GITLET_FOLDER = new File(".gitlet");
    static final File COMMITS_FOLDER = Utils.join(GITLET_FOLDER, "commits");
    static final File STAGINGAREA_FOLDER = Utils.join(GITLET_FOLDER,"stagingArea");
    static final File ADDINGFILES_STAGE = Utils.join(STAGINGAREA_FOLDER, "staged");
    static final File REMOVEFILES_STAGE = Utils.join(STAGINGAREA_FOLDER, "remove");
    static final File BRANCHES_FOLDER = Utils.join(GITLET_FOLDER, "branches");
    static final File BLOBS_FOLDER = Utils.join(GITLET_FOLDER, "blobs");
    private static StagingArea stage;

    public Repository() {

    }
    /** Creates a new Gitlet version-control system in the current directory.
     This system will automatically start with one commit: a commit that contains no files and has the commit message initial commit
     (just like that, with no punctuation). It will have a single branch: master, which initially points to this initial commit,
     and master will be the current branch. The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970 in whatever
     format you choose for dates (this is called "The (Unix) Epoch", represented internally by the time 0.) Since the initial commit in all
     repositories created by Gitlet will have exactly the same content, it follows that all repositories will automatically share this commit
     (they will all have the same UID) and all commits in all repositories will trace back to it.*/
    public static void init() {
        if (GITLET_FOLDER.exists()) {
            System.out.println("A gitlet version-control system already "
                    + "exists in the current directory.");
        } else {

            GITLET_FOLDER.mkdirs();
            COMMITS_FOLDER.mkdirs();
            STAGINGAREA_FOLDER.mkdirs();
            ADDINGFILES_STAGE.mkdirs();
            REMOVEFILES_STAGE.mkdirs();
            BRANCHES_FOLDER.mkdirs();
            BLOBS_FOLDER.mkdirs();
        }


        Commit initial = new Commit("initial commit", new HashMap<>(), null, "Wed Dec 31 16:00:00 1969 -0800","");
        // not the best way to add two shah1 values together.
        String Hash = initial.getOwnHash();
        File initialHash = Utils.join(COMMITS_FOLDER, Hash);
        Utils.writeObject(initialHash, initial);

        // makes mater branch file
        File Master = Utils.join(BRANCHES_FOLDER, "master");
        Utils.writeContents(Master, Hash);

        // Main branch file lowkey useless and redundent
        // File MainBranch = Utils.join(BRANCHES_FOLDER, "Main.txt");
        // Utils.writeContents(MainBranch, "master");


        //Head file
        File Head = Utils.join(BRANCHES_FOLDER, "Head.txt");
        Utils.writeContents(Head, Hash);


    }
    /** Adds a copy of the file as it currently exists to the staging area (see the description of the commit command).
     For this reason, adding a file is also called staging the file for addition. Staging an already-staged file overwrites the
     previous entry in the staging area with the new contents. The staging area should be somewhere in .gitlet.
     If the current working version of the file is identical to the version in the current commit, do not stage it to be added,
     and remove it from the staging area if it is already there (as can happen when a file is changed, added, and then changed back).
     the file will no longer be staged for removal (see gitlet rm), if it was at the time of the command. */
    public void add(String filename) {
        File Add = Utils.join(CWD, filename);
        Commit current = getCommit();
        HashMap<String, String> f = current.getTracker();
        if (!Add.exists()) {
            System.out.println("file does not exist");
            return;
        }
        String blob = Utils.sha1(Utils.serialize(Utils.readContentsAsString(Add)));
        if (f.containsKey(filename)) {
            File stage = Utils.join(REMOVEFILES_STAGE, filename);
            if (stage.exists()) {
                Utils.writeContents(stage, Utils.readContentsAsString(stage));
                stage.delete();
                return;
            }
            if (f.get(filename).equals(blob)) {
                return;
            }
        }
        File p = Utils.join(ADDINGFILES_STAGE, filename);
        if (p.exists()) {
            if (Utils.readContentsAsString(p).equals(blob)) {
                System.out.println("files are the same");
                return;
            }
        }
        Utils.writeContents(p, blob);
        File Blob = Utils.join(BLOBS_FOLDER, blob);
        Utils.writeContents(Blob, Utils.readContentsAsString(Add));
    }


    /** Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time,
     creating a new commit. The commit is said to be tracking the saved files. By default, each commit's snapshot of files will be
     exactly the same as its parent commit's snapshot of files; it will keep versions of files exactly as they are, and not update them.
     A commit will only update the contents of files it is tracking that have been staged for addition at the time of commit,
     in which case the commit will now include the version of the file that was staged instead of the version it got from its parent.
     A commit will save and start tracking any files that were staged for addition but weren't tracked by its parent.
     Finally, files tracked in the current commit may be untracked in the new commit as a result being staged for removal by the rm command (below).
     The bottom line: By default a commit is the same as its parent. Files staged for addition and removal are the updates to the commit.
     Of course, the date (and likely the message) will also different from the parent. */
    public void commit(String msg) {
        if (msg.isEmpty()) {
            System.out.println("Please enter a commit message");
        }
        List<String> add = Utils.plainFilenamesIn(ADDINGFILES_STAGE);
        List<String> removedFiles = Utils.plainFilenamesIn(REMOVEFILES_STAGE);
        if (add.size() == 0 && removedFiles.size() == 0) {
            System.out.println("no files staged to commit");
        }
        Commit current = getCommit();
        HashMap<String, String> files = current.getTracker();
        // for this section take a look at rm and possibly status?
        for (String f : add) {
            File var = Utils.join(ADDINGFILES_STAGE, f);
            String contents = Utils.readContentsAsString(var);
            files.put(f, contents);
            Utils.writeContents(var, contents);
            var.delete();
        }
        for (String f : removedFiles) {
            File remove = Utils.join(REMOVEFILES_STAGE, f);
            Utils.writeContents(remove, Utils.readContentsAsString(remove));
            remove.delete();
        }
        Date date = new Date();
        String t = _dateformat.format(date);

        String parent = current.getOwnHash();
        Commit newestCommit = new Commit(msg, files, parent, t,"");
        String Hash = newestCommit.getOwnHash();
        File NewHash = Utils.join(COMMITS_FOLDER, Hash);
        File H = Utils.join(BRANCHES_FOLDER, "Head.txt");
        Utils.writeContents(H, Hash);
        Utils.writeObject(NewHash, newestCommit);
    }

    public void rm(String fileName) {
        Commit current = getCommit();
        File add = Utils.join(CWD, fileName);
        List<String> Add = Utils.plainFilenamesIn(ADDINGFILES_STAGE);
        String blob = Utils.sha1(Utils.serialize(Utils.readContentsAsString(add)));
        if (current.getTracker().size() == 0 && (Add == null || !Add.contains(fileName))) {
            System.out.println("No reason to remove this file");
            return;
        }
        if (Add.contains(fileName)) {
            File unstage = Utils.join(ADDINGFILES_STAGE, fileName);
            Utils.writeContents(unstage, blob);
            unstage.delete();
        }
        if(current.getTracker() != null) {
            File remove = Utils.join(REMOVEFILES_STAGE, fileName);
            Utils.writeContents(remove, blob);
            // remove.delete();
            File RemovedCwd = Utils.join(CWD, fileName);
            Utils.writeContents(RemovedCwd, blob);
            RemovedCwd.delete();
            return;

        }
    }

    /** Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit,
     following the first parent commit links, ignoring any second parents found in merge commits. (In regular Git, this is what you get with git log --first-parent).
     This set of commit nodes is called the commit's history. For every node in this history, the information it should display is the commit id,
     the time the commit was made, and the commit message. Here is an example of the exact format it should follow: There is a === before each commit and an empty line after it.
     As in real Git, each entry displays the unique SHA-1 id of the commit object. The timestamps displayed in the commits reflect the current timezone, not UTC; as a result,
     the timestamp for the initial commit does not read Thursday, January 1st, 1970, 00:00:00, but rather the equivalent Pacific Standard Time.
     Display commits with the most recent at the top. By the way, you'll find that the Java classes java.util.Date and java.util.Formatter are useful for getting and formatting times.
     Look into them instead of trying to construct it manually yourself!
     For merge commits (those that have two parent commits), add a line just below the first, as in where the two hexadecimal numerals following "Merge:" consist of the first seven digits
     of the first and second parents' commit ids, in that order. The first parent is the branch you were on when you did the merge; the second is that of the merged-in branch.
     This is as in regular Git.*/
    public void log() {
        Commit commitCurr = getCommit();
        while( commitCurr != null) {
            System.out.println("===");
            System.out.println("commit " + commitCurr.getOwnHash());
            /**  checking to see if this works */
            if (!commitCurr.getMerge().equals("")) {
                System.out.println("Merge: " + commitCurr.getParent().substring(0, 7)
                + " " + commitCurr.getMerge().substring(0, 7));
            }
            /** making sure im printing everything */
            System.out.println("Date: " + commitCurr.getDate());
            System.out.println(commitCurr.getMessage());
            System.out.println();
            if(commitCurr.getParent() != null) {
                File c = Utils.join(COMMITS_FOLDER, commitCurr.getParent());
                commitCurr = Utils.readObject(c, Commit.class);
            } else {
                break;
            }
        }
    }

    public void globalLog() {
        List<String> commits = Utils.plainFilenamesIn(COMMITS_FOLDER);
        for (String commit : commits) {
            File getCommit = new File(commit);
            Commit curr = Utils.readObject(getCommit, Commit.class);
            System.out.println("===");
            System.out.println("commit " + curr.getOwnHash());
            /**  checking to see if this works */
            if (!curr.getMerge().equals("")) {
                System.out.println("Merge: " + curr.getParent().substring(0, 7)
                        + " " + curr.getMerge().substring(0, 7));
            }
            /** making sure im printing everything */
            System.out.println("Date: " + curr.getDate());
            System.out.println(curr.getMessage());
            System.out.println();
            if(curr.getParent() != null) {
                File c = Utils.join(COMMITS_FOLDER, curr.getParent());
                curr = Utils.readObject(c, Commit.class);
            } else {
                break;
            }
        }
    }

    /** Takes the version of the file as it exists in the head commit, the front of the current branch, and puts it in the working directory,
     * overwriting the version of the file that's already there if there is one. The new version of the file is not staged.
     Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory,
     overwriting the version of the file that's already there if there is one. The new version of the file is not staged.
     Takes all files in the commit at the head of the given branch, and puts them in the working directory,
     overwriting the versions of the files that are already there if they exist. Also, at the end of this command,
     the given branch will now be considered the current branch (HEAD). Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
     The staging area is cleared, unless the checked-out branch is the current branch (see Failure cases below).*/
    public void checkout(String[] args) {
        if (args.length == 3) {
            String name = args[2];
            checkout(name);
        } else if (args.length == 4) {
            String CommitId = args[1];
            String name = args[3];
            checkout(CommitId, name);
        } else if(args.length == 2) {
            String BranchName = args[1];
            checkout3(BranchName);
        }
    }

    /** part 1 of checkout */
    public void checkout(String filename) {
        Commit commitCurr = getCommit();
        HashMap<String, String> files = commitCurr.getTracker();
        if (!files.containsKey(filename)) {
            System.out.println("file does not exist in that commit");
            return;
        }
        File GetBlob = Utils.join(BLOBS_FOLDER, files.get(filename));
        String blob = Utils.readContentsAsString(GetBlob);
        File main = Utils.join(CWD, filename);
        Utils.writeContents(main, blob);
        return;
    }
    /** part 2 checkout */
    public void checkout(String CommitID, String filename) {
        File commit = Utils.join(COMMITS_FOLDER, CommitID);
        if (!commit.exists()) {
            System.out.println(" No commit with that id exists");
            return;
        }
        Commit givenCommit = Utils.readObject(commit, Commit.class);
        HashMap<String, String> f = givenCommit.getTracker();
        if (!f.containsKey(filename)) {
            System.out.println(" file does not exist in that commit");
            return;
        }
        File GetBlob = Utils.join(BLOBS_FOLDER, f.get(filename));
        String blob = Utils.readContentsAsString(GetBlob);
        File main = Utils.join(CWD, filename);
        Utils.writeContents(main, blob);
    }

    public void checkout3(String BranchName) {
        File branch = Utils.join(BRANCHES_FOLDER, BranchName);
        File head = Utils.join(BRANCHES_FOLDER, "Head.txt");
        String ContentsOfHead = Utils.readContentsAsString(head);
        String ContentsOfBranch = Utils.readContentsAsString(branch);
        if (!branch.exists()) {
            System.out.println(" No such branch exists");
            return;
        } else if (ContentsOfHead.equals(ContentsOfBranch)) {
            System.out.println("No need to checkout the current branch");
            return;
        }
        Utils.writeContents(head, Utils.readContentsAsString(branch));
        File branchcommit = Utils.join(COMMITS_FOLDER, ContentsOfBranch);
        Commit destinationcommit = Utils.readObject(branchcommit, Commit.class);
        this.untrackedFiles(destinationcommit);
        // clear CWD
        HashMap<String, String> files = destinationcommit.getTracker();
        for (String v : files.keySet()) {
            File GetBlob = Utils.join(BLOBS_FOLDER, v);
            String blob = Utils.readContentsAsString(GetBlob);
            File m = Utils.join(CWD, v);
            Utils.writeContents(m, blob);
        }
        // add all the files in files HASH in CWD
        // make sure to clear the staging area.
        List<String> add = Utils.plainFilenamesIn(ADDINGFILES_STAGE);
        List<String> removedFiles = Utils.plainFilenamesIn(REMOVEFILES_STAGE);
        for (String f : add) {
            File var = Utils.join(ADDINGFILES_STAGE, f);
            String contents = Utils.readContentsAsString(var);
            files.put(f, contents);
            var.delete();
        }
        for (String f : removedFiles) {
            File remove = Utils.join(REMOVEFILES_STAGE, f);
            files.remove(f);
            remove.delete();
        }
    }
    // untracked if not staged, not tracked by head, doesn't exist in commit i.e = not in tracker or different blob value.
    private void untrackedFiles (Commit desiredCommit) {
        for (String f : Utils.plainFilenamesIn(CWD)) {
            File getfile = Utils.join(CWD, f);
            File stageAdd = Utils.join(ADDINGFILES_STAGE, f);
            // File stageRemove = Utils.join(REMOVEFILES_STAGE, f);
            String blobs = Utils.readContentsAsString(getfile);
            HashMap<String, String> T = desiredCommit.getTracker();
            if (!stageAdd.exists() && !T.containsKey(f) && !T.containsValue(blobs)) {
                System.out.println(" There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }
    }
    public void reset(String CommitID) {
        File H = Utils.join(BRANCHES_FOLDER, "Head.txt");
        File commit = Utils.join(COMMITS_FOLDER, CommitID);
        String blob = Utils.readContentsAsString(H);
        String branchName = "";
        if (!commit.exists()) {
            System.out.println(" No commit with that id exists");
            return;
        }

        Commit givenCommit = Utils.readObject(commit, Commit.class);
        Utils.writeContents(H, givenCommit.getOwnHash());
        List<String> branches = Utils.plainFilenamesIn(BRANCHES_FOLDER);
        for (String b : branches) {
            File branch = Utils.join(BRANCHES_FOLDER, b);
            String contentsofbranch = Utils.readContentsAsString(branch);
            if (contentsofbranch.equals(blob)) {
                branchName = b;
                break;
            }
        }
        String OBranch = branchName;
        Utils.writeObject(H, "");
        checkout3(OBranch);
    }
    public void branch(String branchName) {
        File newBranch = Utils.join(BRANCHES_FOLDER, branchName);
        if(newBranch.exists()) {
            System.out.println(" A branch with that name already exists");
            return;
        }
        File head = Utils.join(BRANCHES_FOLDER, "Head.txt");
        String shah1_value = Utils.readContentsAsString(head);
        Utils.writeContents(newBranch, shah1_value);
    }
    public void rmBranch(String branchName) {
        File Branch = Utils.join(BRANCHES_FOLDER, branchName);
        if (!Branch.exists()) {
            System.out.println(" A branch with that name does not exist");
            return;
        }
        File head = Utils.join(BRANCHES_FOLDER, "Head.txt");
        if (head.equals(Branch)) {
            System.out.println(" cannot remove the branch your currently on");
            return;
        }
        Utils.writeObject(Branch, null);
    }

    public void find(String CommitMessage) {
        List<String> commits = new ArrayList<>(Utils.plainFilenamesIn(COMMITS_FOLDER));
        Iterator<String> it = commits.iterator();
        boolean foundmatchincommit = false;;
        while (it.hasNext()) {
            File getCommit = Utils.join(COMMITS_FOLDER, it.next());
            Commit curr = Utils.readObject(getCommit, Commit.class);
            if (curr.getMessage().equals(CommitMessage)) {
                System.out.println(curr.getOwnHash());
                System.out.println();
                foundmatchincommit = true;
            }
        }
        if (!foundmatchincommit) {
            System.out.println(" Found no commit with that message");
            return;
        }
        System.out.println(" Found no commit with that message");
        return;
    }

    public void status() {
        File CurrentBranch = Utils.join(BRANCHES_FOLDER, "Head.txt");
        String CurrentBranchName = Utils.readContentsAsString(CurrentBranch);
        List<String> branches = new ArrayList<>(Utils.plainFilenamesIn(BRANCHES_FOLDER));
        Iterator<String> it = branches.iterator();
        int i = 0;
        while (it.hasNext()) {
            if (it.next().contains(".txt")) {
                branches.remove(i);
                break;
            }
            i += 1;
        }
        System.out.println("=== Branches ===");
        Collections.sort(branches);
        for (String branch : branches) {
            if(branch.equals(CurrentBranchName)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        List<String> add = Utils.plainFilenamesIn(ADDINGFILES_STAGE);
        List<String> removedFiles = Utils.plainFilenamesIn(REMOVEFILES_STAGE);
        Collections.sort(add);
        Collections.sort(removedFiles);
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String f : add) {
            System.out.println(f);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String f : removedFiles) {
            System.out.println(f);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public Commit getCommit() {
        File pointToHead = Utils.join(BRANCHES_FOLDER, "head.txt");
        File pointToCommit = Utils.join(COMMITS_FOLDER, Utils.readContentsAsString(pointToHead));
        Commit current = Utils.readObject(pointToCommit, Commit.class);
        return current;
    }


    private static SimpleDateFormat _dateformat = new SimpleDateFormat("EEE " +  "MMM d HH:mm:ss yyyy " + "-0800");


}
