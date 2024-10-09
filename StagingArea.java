package gitlet;

import java.io.Serializable;
import java.util.*;

public class StagingArea implements Serializable {
    private Map<String, String> addingFiles;
    private Set<String> removingFiles;


    public StagingArea() {
        this.addingFiles = new HashMap<>();
        this.removingFiles = new HashSet<>();
    }
    public void clearStagingArea() {
        this.addingFiles.clear();
        this.removingFiles.clear();
    }


    }




