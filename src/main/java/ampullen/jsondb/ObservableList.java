package ampullen.jsondb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObservableList<T> extends ArrayList<T> implements IObservable{

	private static final long serialVersionUID = 1L;
	
	private transient List<Observer> observers = new ArrayList<>();
	
	public ObservableList() {
		super();
	}
	
	public ObservableList(Collection<? extends T> c) {
		super(c);
	}
	
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
	
	@Override
	public void add(int index, T element) {
		super.add(index, element);
		notifyObservers();
	}
	
	@Override
	public boolean add(T e) {
		boolean b = super.add(e);
		if(b)
			notifyObservers();
		return b;
	}
	
	@Override
	public T remove(int index) {
		T t = super.remove(index);
		if(t != null)
			notifyObservers();
		return t;
	}
	
	@Override
	public boolean remove(Object o) {
		boolean b = super.remove(o);
		if(b)
			notifyObservers();
		return b;
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		boolean b = super.removeAll(c);
		if(b)
			notifyObservers();
		return b;
	}
	
	@Override
	public void clear() {
		super.clear();
		notifyObservers();
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean b = super.addAll(c);
		if(b)
			notifyObservers();
		return b;
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		boolean b = super.addAll(index, c);
		if(b)
			notifyObservers();
		return b;
	}
	
	@Override
	public T set(int index, T element) {
		T t = super.set(index, element);
		if(t != null)
			notifyObservers();
		return t;
	}
	
}
