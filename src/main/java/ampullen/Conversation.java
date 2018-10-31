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
	
	Option first = null;
	
	public Conversation() {
		
	}
	
	Map<String, Object> store = new HashMap<>();
	
	public Option build() {
		return first = new Option(this, null);
	}
	
	public void put(String s, Object o){
		store.put(s, o);
	}
	
	public Object get(String s){
		return store.get(s);
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
	
	public class Option{
		
		Thread t;
		
		//List<String> list = new ArrayList<>();
		Object current = null;
		
		Map<Object, BiFunction<String, Option, Object>> map = new HashMap<>();
		List<Object> order = new ArrayList<>();
		Map<String, String> questions = new HashMap<>();
		
		Conversation c;
		Option parent;
		
		List<Consumer<Option>> finished = new ArrayList<>();
		
		public Option(Conversation c, Option parent) {
			this.c = c;
			this.parent = parent;
		}
		
		public Option addOption(String question, BiFunction<String, Option, Object> function) {
			
			Option o = new Option(c, this);
			o.addStep(question, function);
			
			return o;
		}
		
		public Option addOption(String question, Function<String, Object> function) {
			return addOption(question, (x, y) -> function.apply(x));
		}
		
		public Option addStep(String question, BiFunction<String, Option, Object> function){
			
			if(current == null){
				current = question;
			}
			
			String questionstr = null;
			
			if(question.contains("-")){
				String[] arr = question.split("-");
				questions.put(arr[0], arr[1]);
				questionstr = arr[1];
			}else{
				questionstr = question;
			}
			
			map.put(questionstr, function);
			order.add(questionstr);
			
			return this;
		}
		
		public Option addStep(String id, String question, BiFunction<String, Option, Object> function) {
			
			return addStep(id + "-" + question, function);
		}
		
		public Option addStep(String question, Function<String, Object> function){
			return addStep(question, (x, y) -> {return function.apply(x);});
		}
		
		public Option addStep(String id, String question, Function<String, Object> function) {
			return addStep(id + "-" + question, function);
		}
		
		public Option addStepA(String question, Consumer<String> function){
			//String last = this.last;
			return addStep(question, (x) -> {
				function.accept(x); 
				return getNext();
			});
		}
		
		public Option addStepA(String question, BiConsumer<String, Option> function){
			//String last = this.last;
			return addStep(question, (x, y) -> {
				function.accept(x, y); 
				return getNext();
			});
		}
		
		String last;
		
		public Option addStepLast(String question, BiConsumer<String, Option> function) {
			addStepA(question, function);
			last = question;
			return this;
		}
		
		public Object getNext() {
			int index = order.indexOf(current);
			if(index < order.size()-1) {
				return order.get(index + 1);
			}
			return null;
		}
		
		public Option done() {
			return parent;
		}
		
		public Conversation out() {
			return c;
		}

		public Option finished(Consumer<Option> consumer){
			finished.add(consumer);
			return this;
		}
		
		protected boolean completed = false;
		
		public Option start(MessageChannel channel, User user) {
			
			current = order.get(0);
			
			Runnable run = () -> {
				while(current != null){
					askNext(channel, user);
				}
				finished.forEach(x -> x.accept(this));
				completed = true;
			};
			
			if(parent != null) {
				t = new Thread(run);
				t.start();
			}else {
				run.run();
			}
			
			return this;
		}

		private void askNext(MessageChannel channel, User user) {
			
			String answer = null;
			
			if(current instanceof String) {
				
				answer = new Prompt((String)current, channel, user).promptSync();
				
			}else if(current instanceof Option) {
				
				Option o = (Option)current;
				o.start(channel, user);
				answer = o.completed + "";
				
			}
			map.get(current).apply(answer, this);
			current = getNext();
			
		}
		
	}
}
