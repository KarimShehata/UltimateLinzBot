package ampullen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class Conversation implements Cloneable{
	
	Map<String, BiFunction<String, Conversation, String>> map = new HashMap<>();
	String current = null;
	
	Map<String, String> pointers = new HashMap<>();
	Map<String, String> links = new HashMap<>();
	
	String last;
	
	boolean askLastQuestion = false;
	
	public Conversation() {
		
	}
	
	public Conversation(Map<String, BiFunction<String, Conversation, String>> map, String current){
		this.map = map;
		this.current = current;
	}
	
	public Conversation addStep(String question, BiFunction<String, Conversation, String> function){
		
		if(current == null){
			current = question;
		}
		
		String questionstr = null;
		
		if(question.contains("-")){
			String[] arr = question.split("-");
			pointers.put(arr[0], arr[1]);
			questionstr = arr[1];
			links.put(last, arr[0]);
		}else{
			questionstr = question;
			links.put(last, questionstr);
		}
		last = questionstr;
		
		map.put(questionstr, function);
		
		return this;
	}
	
	public Conversation addStep(String question, Function<String, String> function){
		return addStep(question, (x, y) -> {return function.apply(x);});
	}
	
	public Conversation addStepA(String question, Consumer<String> function){
		//String last = this.last;
		return addStep(question, (x) -> {
			function.accept(x); 
			return links.get(question);
		});
	}
	
	public Conversation addStepA(String question, BiConsumer<String, Conversation> function){
		String last = this.last;
		return addStep(question, (x, y) -> {
			function.accept(x, y); 
			return links.get(question);
		});
	}
	
	Map<String, Object> store = new HashMap<>();
	
	public void put(String s, Object o){
		store.put(s, o);
	}
	
	public Object get(String s){
		return store.get(s);
	}
	
	Thread t;
	
	public Conversation start(MessageChannel channel, User user){
		t = new Thread(() -> {
			while(current != null){
				next(channel, user);
			}
			finished.forEach(x -> x.accept(this));
		});
		t.start();
		return this;
	}
	
	List<Consumer<Conversation>> finished = new ArrayList<>();
	
	public Conversation finished(Consumer<Conversation> consumer){
		finished.add(consumer);
		return this;
	}
	
	public Conversation clear(){
		//TODO implement
		return this;
	}
	
	public Conversation askAtLastQuestion(boolean ask){
		askLastQuestion = ask; //TODO implement functionality
		return this;
	}
	
	public void next(MessageChannel channel, User user){
		
		/*if(links.get(current) == null){
			
			channel.sendMessage(current);
			
		}else{*/

			String answer = new Prompt(current, channel, user).promptSync();
			
		
		
		
		String next = map.get(current).apply(answer, this);
		current = pointers.containsKey(next) ? pointers.get(next) : next;
		
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new Conversation(map, current);
	}
}
