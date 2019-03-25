package ampullen.jsondb;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Observable implements IObservable{
	
	private final transient List<Observer> observers = new CopyOnWriteArrayList<>();
	
	public void notifyObservers(){
		synchronized (observers){
			observers.forEach(x -> x.update(this));
		}
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
