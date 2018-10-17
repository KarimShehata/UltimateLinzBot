package ampullen.model;

import java.util.HashMap;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import ampullen.jsondb.Observable;

public class TournamentVotes extends Observable{
	
	Map<String, Choices> attendance = new HashMap<>();
	
	public Choices getChoices(String user) {
		return attendance.get(user);
	}
	
	public void addChoices(String user, Choices c) {
		if(!attendance.containsKey(user)) {
			attendance.put(user, c);
		}else {
			new OperationNotSupportedException("Choice already set").printStackTrace();
		}
	}
	
	/*@Override
	public void addObserver(Observer o) {
		super.addObserver(o);
		attendance.values().forEach(x -> x.addObserver(o));
	}*/
	
	public static class Choices extends Observable{
		
		AttendanceChoice a;
		EatingChoice e;
		
		public AttendanceChoice getAttendance() {
			return a;
		}
		public void setAttendance(AttendanceChoice a) {
			this.a = a;
			notifyObservers();
		}
		public EatingChoice getEating() {
			return e;
		}
		public void setEating(EatingChoice e) {
			this.e = e;
			notifyObservers();
		}
		
		
	}
	
	public static enum AttendanceChoice{
		IN, MAYBE, OUT, NOTSET
	}
	
	public static enum EatingChoice{
		MEAT, VEGGIE, NOTSET
	}
	
}
