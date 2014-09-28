package tr.edu.metu.ii.aaa.gson;

import java.util.List;



public class Question {

    private int          _id;
    private List<Option> _options;
    private String       _value = "";
    
    private String _answer = "";
    private int    _selectedOption = -1;
    
    public Question(int id){
        _id = id;
    }
    
    @Override
    public boolean equals(Object o) {

        if(!(o instanceof Question))
            return false;
        
        if(_id == ((Question)o)._id)
            return true;
        
        return false;
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
    
    public List<Option> getOptions(){
        
        return _options;
    }
    
    public void setOptions(List<Option> options){
        
        _options = options;
    }

    
    public String getAnswer() {
    
        return _answer;
    }
    
    public void setAnswer(String _answer) {
    
        this._answer = _answer;
    }

    public int getSelectedOption() {
    
        return _selectedOption;
    }
    
    public void setSelectedOption(int _selectedOption) {
    
        this._selectedOption = _selectedOption;
    }

    
}