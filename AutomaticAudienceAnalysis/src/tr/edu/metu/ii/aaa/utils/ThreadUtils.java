package tr.edu.metu.ii.aaa.utils;


public class ThreadUtils {
    
    public static void sleep(long milliseconds){
        
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
