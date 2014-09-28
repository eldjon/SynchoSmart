package tr.edu.metu.ii.aaa.gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class QuestionnaireAdapter extends TypeAdapter<GsonQuestionList>{

    private QuestionAdapter _itemAdapter = new QuestionAdapter();
    
    public static class QuestionAdapter extends TypeAdapter<Question>{

        @Override
        public Question read(JsonReader reader) throws IOException {
            
        	Question question    = null;
        	List<Option> options = new ArrayList<Option>();

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
                		Option qo = new Option(optionId);
                		qo.setValue(optionValue);
                		qo.setSection(optionSection);
                		options.add(qo);
                	}
                }

                reader.endObject();
                question = new Question(id);
                question.setValue(value);
                question.setOptions(options);
                
                return question;
            }

            return null;
        }

        @Override
        public void write(JsonWriter writer, Question obj) throws IOException {
            
            System.out.println("QuestionnaireAdapter.QuestionAdapter.write()");
            writer.beginObject();
            writer.name("question_id");
            writer.value(obj.getId());
            writer.name("answer");
            writer.value(obj.getAnswer());
            writer.name("selected_option");
            writer.value(obj.getSelectedOption());
            writer.endObject();
            
//            writer.beginObject();
//            writer.name("id");
//            writer.value(obj.getId());
//            writer.name("value");
//            writer.value(obj.getValue());
//        	writer.name("options");
//            if(obj.getOptions() == null || 
//            		obj.getOptions().size() <= 0){
//            	writer.value(0);
//            } else {
//            	
//            	writer.value(obj.getOptions().size());
//            	for(Option qo : obj.getOptions()){
//            		writer.name("option_id");
//            		writer.value(qo.getId());
//            		writer.name("option_value");
//            		writer.value(qo.getValue());
//            		writer.name("option_section");
//            		writer.value(qo.getSection());
//            	}
//            }
//
//            writer.endObject();
        }
    }
    
    @Override
    public GsonQuestionList read(JsonReader reader) throws IOException {

        if (reader.peek() == JsonToken.NULL) {
            
            reader.nextNull();
            return null;
        }
        
        List<Question> questions = new ArrayList<Question>();
        reader.beginArray();
        while(reader.hasNext()){
            
        	Question question = _itemAdapter.read(reader);
        	if(question != null)
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

}
