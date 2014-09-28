package tr.edu.metu.ii.aaa.db;

import java.util.ArrayList;
import java.util.List;

import tr.edu.metu.ii.aaa.db.tables.GyroTable;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Classes related to database operations are not used within the project.
 * After some tests it was concluded that db operations are too expensive
 * and from the performance perspective cache mechanism and direct persistence
 * to files is more beneficial.
 * 
 * @author eldi
 *
 */
public class SQLiteHelper extends SQLiteOpenHelper{

    protected static final String DB_NAME    = "tr.metu.edu.ii.aaa_db";
    protected static final int    DB_VERSION = 1;
    
    protected List<SQLiteTable> _tables = new ArrayList<SQLiteTable>();
    
    public SQLiteHelper(Context context,
                        String name,
                        CursorFactory factory,
                        int version) {

        super(context,
              name,
              factory,
              version);
    }
    
    public SQLiteHelper(Context context){
        
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        initTables();
        
        for(SQLiteTable table : _tables)
            table.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        for(SQLiteTable table : _tables)
            table.onUpgrade(db, oldVersion, newVersion);
    }
    
    protected void initTables(){
        
        _tables.clear();
        _tables.add(GyroTable.getInstance());
    }
}
