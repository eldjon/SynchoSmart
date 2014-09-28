package tr.edu.metu.thesis.gson;

import tr.edu.metu.thesis.gson.QuestionnaireAdapter.GsonQuestionList;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

public class GsonAdapterFactory implements TypeAdapterFactory{

	@SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson context, TypeToken<T> token) {

        TypeAdapter<T> adapter = null;
        
        if(token.getRawType() == GsonQuestionList.class){
         
        	adapter = (TypeAdapter<T>) new QuestionnaireAdapter();
        }else if(token.getRawType() == GsonParticipantAnswerList.class){
            
            adapter = (TypeAdapter<T>) new AnswerAdapter();
        }
        return adapter;
    }
    
}
