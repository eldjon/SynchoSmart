package tr.edu.metu.ii.aaa.db.tables;

import android.database.sqlite.SQLiteDatabase;


public class GyroTable extends AbstractSQLiteTable{

    public static final String GYRO_TABLE_NAME = "gyro_table";
    public static final String COLUMN_X_COORD  = "x_coord";
    public static final String COLUMN_Y_COORD  = "y_coord";
    public static final String COLUMN_Z_COORD  = "z_coord";
    
    private static final String SQL_TABLE_CREATE = "create table IF NOT EXISTS "
                                                   + GYRO_TABLE_NAME
                                                   + "("
                                                   + ID_COLUMN
                                                   + " integer primary key autoincrement, "
                                                   + COLUMN_TIMESTAMP
                                                   + " text not null, "
                                                   + COLUMN_X_COORD
                                                   + " real not null, "
                                                   + COLUMN_Y_COORD
                                                   + " real not null, "
                                                   + COLUMN_Z_COORD
                                                   + " real not null "
                                                   + ");" ;
    
    private static GyroTable _instance;
    
    private GyroTable(){
        
        super();
    }
    
    public synchronized static GyroTable getInstance(){
        
        if(_instance == null)
            _instance = new GyroTable();
        
        return _instance;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + GYRO_TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void notifyListeners(TableActionType action) {

    }

    @Override
    public String getTableName() {

        return GYRO_TABLE_NAME;
    }

}
