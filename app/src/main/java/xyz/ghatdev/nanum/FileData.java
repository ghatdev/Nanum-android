package xyz.ghatdev.nanum;

import io.realm.RealmObject;

/**
 * Created by ghatdev on 2015. 9. 26..
 */
public class FileData extends RealmObject {
    private String uuid;
    private String fileName;
    private int key;
    private String path;
    private String type;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fName) {
        fileName = fName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uid) {
        uuid = uid;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int k) {
        key = k;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String p) {
        path = p;
    }

    public String getType() {
        return type;
    }

    public void setType(String t) {
        type = t;
    }
}
