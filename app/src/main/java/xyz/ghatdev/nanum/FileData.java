package xyz.ghatdev.nanum;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ghatdev on 2015. 9. 26..
 */
public class FileData extends RealmObject
{
    private String uuid;
    private String fileName;
    private int key;
    private String path;
    private String type;

    public void setUuid(String uid){uuid=uid;}
    public String getFileName(){return fileName;}
    public void setFileName(String fName){fileName=fName;}
    public String getUuid(){return uuid;}
    public void setKey(int k){key=k;}
    public int getKey(){return key;}
    public void setPath(String p){path=p;}
    public String getPath(){return path;}
    public void setType(String t){type=t;}
    public String getType(){return type;}
}
