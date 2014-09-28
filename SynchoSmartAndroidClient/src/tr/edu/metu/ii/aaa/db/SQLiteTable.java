package tr.edu.metu.ii.aaa.db;

import android.database.sqlite.SQLiteDatabase;


public interface SQLiteTable {
    
    public static final String ID_COLUMN = "_id";
    
    
    public void onCreate(SQLiteDatabase db);
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    public void addListener(SQLiteTableListener listener);
    
    public void removeListener(SQLiteTableListener listener);

    public void notifyListeners(TableActionType action);
    
    public String getTableName();
    
    public enum TableActionType {
        
        COMMIT,
        UPDATE,
        DELETE
    };

}
