package ampullen.jsondb;

public interface IObservable {
	
	public void notifyObservers();
	
	public void addObserver(Observer o);
	
	public Observer removeObserver(Observer o);
}
