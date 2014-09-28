package tr.edu.metu.thesis.gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;


public class AnswerAdapter extends TypeAdapter<GsonParticipantAnswerList>{

    private AnswerItemAdapter _itemAdapter = new AnswerItemAdapter();
    
    public static class AnswerItemAdapter extends TypeAdapter<GsonParticipantAnswer>{

        @Override
        public GsonParticipantAnswer read(JsonReader reader) throws IOException {
        	
        	GsonParticipantAnswer answer = null;
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
                int selectedOption = reader.nextInt();
                reader.endObject();
                
                answer = new GsonParticipantAnswer(id);
                answer.setSelectedOption(selectedOption);
                answer.setValue(value);
                
                return answer;
            }

            return null;
        }

        @Override
        public void write(JsonWriter writer, GsonParticipantAnswer obj) throws IOException {
        }
    }

    @Override
    public GsonParticipantAnswerList read(JsonReader reader) throws IOException {
    	
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        
        List<GsonParticipantAnswer> answers = new ArrayList<GsonParticipantAnswer>();
        reader.beginArray();
        while(reader.hasNext()){
            
        	GsonParticipantAnswer answer = _itemAdapter.read(reader);
            answers.add(answer);
        }
        reader.endArray();

        return new GsonParticipantAnswerList(answers);
    }

    @Override
    public void write(JsonWriter writer, GsonParticipantAnswerList obj) throws IOException {
    }
    
}
