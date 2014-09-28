package tr.edu.metu.ii.aaa.gson;

import java.util.ArrayList;
import java.util.List;


public class GsonQuestionList extends ArrayList<Question>{

    private static final long serialVersionUID = -4178105459312984216L;

    public GsonQuestionList(List<Question> questions){
        
        super(questions);
    }
    
}
