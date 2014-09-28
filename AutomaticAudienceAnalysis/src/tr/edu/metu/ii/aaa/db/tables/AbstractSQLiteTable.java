package tr.edu.metu.ii.aaa.db.tables;

import java.util.ArrayList;
import java.util.List;

import tr.edu.metu.ii.aaa.db.SQLiteTable;
import tr.edu.metu.ii.aaa.db.SQLiteTableListener;
import android.database.sqlite.SQLiteDatabase;


public abstract class AbstractSQLiteTable implements SQLiteTable {
    
    public final static String COLUMN_TIMESTAMP = "timestamp";
    
    protected List<SQLiteTableListener> _tableListeners = null;
    
    @Override
    public void onCreate(SQLiteDatabase db) {

        _tableListeners = new ArrayList<SQLiteTableListener>();
    }
    
    @Override
    public void addListener(SQLiteTableListener listener){

        _tableListeners.add(listener);
    }

    @Override
    public void removeListener(SQLiteTableListener listener){
        
        _tableListeners.remove(listener);
    }

}
