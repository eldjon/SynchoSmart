package tr.edu.metu.ii.aaa.db;


public interface SQLiteTableListener {
    
    public void onCommit(SQLiteTable table);
    
    public void onUpdate(SQLiteTable table);
    
    public void onDelete(SQLiteTable table);
    
    public void onTableCreate(SQLiteTable table);
    
    public void onTableUpgrade(SQLiteTable table);

}
