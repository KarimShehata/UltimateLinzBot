package ampullen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.function.Consumer;

import ampullen.model.Tournament;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class TournamentCreator extends ListenerAdapter{

	PrivateChannel channel;
	
	private String[] arr = new String[]{"Wie heiﬂt das Turnier?", "Wann ist das Turnier?", "Wo ist das Turnier?", "In welchem Format?", "Mixed, Open, Women?", "Teamfee?", "Playersfee?", "Registration Deadline?", "Payment Deadline?"};
	public static final String[] fields = new String[]{"name", "date", "location", "format", "division", "teamFee", "playersFee", "registrationDeadline", "paymentDeadline"};
	String[] values;
	
	int position = 0;
	
	Consumer<Tournament> consumer;
	
	public TournamentCreator(PrivateChannel channel) {
		this.channel = channel;
		this.values = new String[arr.length];
	}
	
	public void create(Consumer<Tournament> consumer){
		this.consumer = consumer;
		channel.getJDA().addEventListener(this);
		nextQuestion();
	}
	
	public void nextQuestion(){
		
		if(position >= arr.length){

			//Creation
			Tournament t = new Tournament();
			
			for(int i = 0 ; i < fields.length ; i++){
				
				for(Method m : Tournament.class.getMethods()){
					
					if(m.getName().equalsIgnoreCase("set" + fields[i])){

						System.out.println(fields[i]);
						
						try {
							if(m.getParameters()[0].getType().getName().equals(long.class.getName())){
								
								m.invoke(t, new SimpleDateFormat("DD.MM.yyyy").parse(values[i]).getTime());
							}else{
								m.invoke(t, values[i]);
							}
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ParseException e) {
							e.printStackTrace();
						}
						
					}
					
				}
				
			}
			
			channel.sendMessage("Das Turnier wurde erstellt!").complete();
			channel.sendMessage(t.toString()).complete();
			channel.getJDA().removeEventListener(this);
			consumer.accept(t);
			
		}else{
			
			channel.sendMessage(arr[position]).complete();
			
		}
	}
	
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		super.onPrivateMessageReceived(event);
		
		if(!event.getAuthor().equals(event.getJDA().getSelfUser())){
			
			String content = event.getMessage().getContentRaw();
			values[position] = content;
			
			position++;
			
			nextQuestion();
			
		}
	}
	
}
