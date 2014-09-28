package tr.edu.metu.thesis.gson;

public class GsonParticipantAnswer {

    private int    _questionId;
    private int    _option;
    private String _value;
    
    public GsonParticipantAnswer(int questionId){
        
        _questionId = questionId;
    }
    
    public int getQuestionId(){
        
        return _questionId;
    }
    
    public int getSelectedOption(){
        
        return _option;
    }
    
    public void setSelectedOption(int option){
        
        _option = option;
    }
    
    public String getValue(){
        
        return _value;
    }
    
    public void setValue(String value){
        
        _value = value;
    }
}
