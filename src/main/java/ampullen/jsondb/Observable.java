package ampullen.jsondb;

import java.util.ArrayList;
import java.util.List;

public class Observable{
	
	private transient List<Observer> observers = new ArrayList<>();
	
	public void notifyObservers(){
		observers.forEach(x -> x.update(this));
	}
	
	public void addObserver(Observer o){
		if(!observers.contains(o)){
			observers.add(o);
		}
	}
	
	public Observer removeObserver(Observer o){
		return observers.remove(o) ? o : null;
	}
	
}
