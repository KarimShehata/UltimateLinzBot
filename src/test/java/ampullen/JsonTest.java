package ampullen;

import ampullen.jsondb.JsonModel;

public class JsonTest {

	public static void main(String[] args) {
		
		//j.addTournament(new Tournament("test", 25, "locat", "venuze", "asd"));
		
		System.out.println(JsonModel.getInstance().getTournaments().toString());
		
	}
	
}
