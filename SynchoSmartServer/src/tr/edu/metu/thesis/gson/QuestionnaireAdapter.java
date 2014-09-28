package tr.edu.metu.thesis.gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tr.edu.metu.thesis.gson.QuestionnaireAdapter.GsonQuestionList;
import tr.edu.metu.thesis.jpa.Question;
import tr.edu.metu.thesis.jpa.QuestionOption;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class QuestionnaireAdapter extends TypeAdapter<GsonQuestionList>{

    private QuestionAdapter _itemAdapter = new QuestionAdapter();
    
    public static class QuestionAdapter extends TypeAdapter<Question>{

        @Override
        public Question read(JsonReader reader) throws IOException {
            
        	Question question = null;
        	List<QuestionOption> options = new ArrayList<QuestionOption>();

            if (reader.peek() == JsonToken.NULL) {
                
                reader.nextNull();
                return null;
            }
            
            if(reader.hasNext()){
                
                reader.beginObject();
                reader.nextName();
                int id = reader.nextInt();
                reader.nextName();
                String value = reader.nextString();
                reader.nextName();
                int optionNr = reader.nextInt();
                if(optionNr > 0){
                	for(int i = 0; i < optionNr; i++){
                		reader.nextName();
                		int optionId = reader.nextInt();
                		reader.nextName();
                		String optionValue = reader.nextString();
                		reader.nextName();
                		String optionSection = reader.nextString();
                		QuestionOption qo = new QuestionOption();
                		qo.setQuestionOptionId(optionId);
                		qo.setValue(optionValue);
                		qo.setSection(optionSection);
                		options.add(qo);
                	}
                }

                reader.endObject();
                question = new Question();
                question.setQuestionId(id);
                question.setValue(value);
                question.setQuestionOptions(options);
                
                return question;
            }

            return null;
        }

        @Override
        public void write(JsonWriter writer, Question obj) throws IOException {

            writer.beginObject();
            writer.name("id");
            writer.value(obj.getQuestionId());
            writer.name("value");
            writer.value(obj.getValue());
        	writer.name("options");
            if(obj.getQuestionOptions() == null || 
            		obj.getQuestionOptions().size() <= 0){
            	writer.value(0);
            } else {
            	
            	writer.value(obj.getQuestionOptions().size());
            	for(QuestionOption qo : obj.getQuestionOptions()){
            		writer.name("option_id");
            		writer.value(qo.getQuestionOptionId());
            		writer.name("option_value");
            		writer.value(qo.getValue());
            		writer.name("option_section");
            		writer.value(qo.getSection());
            	}
            }

            writer.endObject();
        }
    }
    
    @Override
    public GsonQuestionList read(JsonReader reader) throws IOException {

        if (reader.peek() == JsonToken.NULL) {
            
            reader.nextNull();
            return null;
        }
        
        ArrayList<Question> questions = new ArrayList<Question>();
        reader.beginArray();
        while(reader.hasNext()){
            
        	Question question = _itemAdapter.read(reader);
            questions.add(question);
        }
        reader.endArray();

        return new GsonQuestionList(questions);
    }

    @Override
    public void write(JsonWriter writer, GsonQuestionList obj) throws IOException {

        if(obj == null)
            return;
        
        writer.beginArray();
        
        for(int i = 0; i < obj.size(); i++)
            _itemAdapter.write(writer, obj.get(i));
        
        writer.endArray();
    }
    
    public static class GsonQuestionList extends ArrayList<Question>{
    	
    	/**
		 * 
		 */
		private static final long serialVersionUID = 2286141472119544527L;

		public GsonQuestionList(List<Question> questions){
    		super(questions);
    	}
    }
}
