package ampullen;

import ampullen.jsondb.JsonModel;
import ampullen.model.Tournament;

public class JsonTest {

	public static void main(String[] args) {
		
		//j.addTournament(new Tournament("test", 25, "locat", "venuze", "asd"));
		
		System.out.println(JsonModel.getInstance().tournaments().toString());
		
		Tournament t = new Tournament();
		t.setName("asds");
		t.setLocation("asfs");
		
		System.out.println(JsonModel.getInstance().tournaments().add(t));
		
	}
	
}
