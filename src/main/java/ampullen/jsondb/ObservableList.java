package ampullen.jsondb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObservableList<T extends IObservable> extends ArrayList<T> implements IObservable, Observer{

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
		element.addObserver(this);
		notifyObservers();
	}
	
	@Override
	public boolean add(T e) {
		boolean b = super.add(e);
		if(b) {
			e.addObserver(this);
			notifyObservers();
		}
		return b;
	}
	
	@Override
	public T remove(int index) {
		T t = super.remove(index);
		if(t != null) {
			t.removeObserver(this);
			notifyObservers();
		}
		return t;
	}
	
	@Override
	public boolean remove(Object o) {
		boolean b = super.remove(o);
		if(b) {
			if(o instanceof IObservable) {
				((IObservable)o).removeObserver(this);
			}
			notifyObservers();
		}
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
		if(b) {
			c.forEach(x -> x.addObserver(this));
			notifyObservers();
		}
		return b;
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		boolean b = super.addAll(index, c);
		if(b) {
			c.forEach(x -> x.addObserver(this));
			notifyObservers();
		}
		return b;
	}
	
	@Override
	public T set(int index, T element) {
		T t = super.set(index, element);
		if(t != null) {
			t.addObserver(this);
			notifyObservers();
		}
		return t;
	}

	@Override
	public void update(IObservable observable) {
		notifyObservers();
	}
	
}
