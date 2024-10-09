package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class Commit implements Serializable {

    private String message;

    /** something that keeps tracks of what files this commit is tracking.  file name is the key, value is the blobs or the sha1.*/
    private HashMap<String, String> Tracker;

    /** the Head commit of a commit object, will be a file name which were we can find the parent.*/
    private String parent;

    /** the Hash of the given Commit object */
    private String ownHash;

    /** the variable representing the date of a commit object */
    private String _date;

    private String _merge;

    public Commit(String message, HashMap<String,String> Tracker, String parent, String date, String merge) {
        this.Tracker = Tracker;
        this.message = message;
        _date = date;
        this.parent = parent;
        ownHash = Utils.sha1((Object) Utils.serialize(this), getMessage(), getDate());
        this._merge = merge;
    }

    public String getMessage() {
        return this.message;
    }

    public String getDate() {
        return _date;
    }
    public String getOwnHash() {
        return ownHash;
    }

    public String getParent() {
        return this.parent;
    }
    public HashMap<String, String> getTracker() {
        return this.Tracker;
    }
    public String getMerge() {
        return this._merge;
    }

}
