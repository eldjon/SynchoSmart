package tr.edu.metu.thesis.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class GsonParser {

    public static Gson GSON = null; 
    
    static{
    	
        GSON = new GsonBuilder().registerTypeAdapterFactory(
								new GsonAdapterFactory())
								.create();
    }
}
