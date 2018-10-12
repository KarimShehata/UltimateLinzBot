package ampullen;

public class JsonTest {

	public static void main(String[] args) {
		
		JsonModel j = new JsonModel();
		
		//j.addTournament(new Tournament("test", 25, "locat", "venuze", "asd"));
		
		System.out.println(j.getTournaments().toString());
		
	}
	
}
