package tr.edu.metu.ii.aaa.gson;


public class Option {

    private int     _id;
    private String  _value;
    private String  _section;
    
    public Option(int id){
        
        _id = id;
    }
    
    public String getText(){
        
        return _section.concat("  ").concat(_value);
    }
    
    public int getId(){
        return _id;
    }
    
    public String getValue(){
        
        return _value;
    }
    
    public void setValue(String value){
        
        _value = value;
    }
    
    public String getSection(){
        
        return _section;
    }
    
    public void setSection(String section){
        
        _section = section;
    }
}
