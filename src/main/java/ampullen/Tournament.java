package ampullen;

public class Tournament extends Observable{

	String name;
	long date;
	String location;
	String format;
	String venue;
	
	public Tournament(String name, long date, String location, String format, String venue) {
		super();
		this.name = name;
		this.date = date;
		this.location = location;
		this.format = format;
		this.venue = venue;
	}
	
}
