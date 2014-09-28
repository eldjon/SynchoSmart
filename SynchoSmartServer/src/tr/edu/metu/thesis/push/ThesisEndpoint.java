package tr.edu.metu.thesis.push;

import org.primefaces.push.annotation.OnMessage;
import org.primefaces.push.annotation.PushEndpoint;
import org.primefaces.push.impl.JSONEncoder;


@PushEndpoint("/thesisEndpoint")
public class ThesisEndpoint {

	 @OnMessage(encoders = {JSONEncoder.class})
	 public String onMessage(String count) {

		 System.out.println("called endpoint");
		 return "skjdhfkjshdfkjhskjdhfkjshd";
	 }
}
