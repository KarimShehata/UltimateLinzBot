package ampullen;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.RequestFuture;

public class Prompt extends ListenerAdapter{
	
    private CountDownLatch latch = null;
    
	String prompt;
	MessageChannel channel;
	Consumer<String> callback = null;
	User user;
	
	Message tempMessage;
	
	long deleteDelay = -1L;
	
	String ret = null;

	public Prompt(String prompt, MessageChannel channel, User promptedUser) {
		super();
		this.prompt = prompt;
		this.channel = channel;
		this.user = promptedUser;
	}
	
	public Prompt setDelete(long millis) {
		if(millis != -1){
			deleteDelay = millis;
			System.out.println("Delete set to " + millis);
		}
		return this;
	}
	
	public String promptSync(){

		tempMessage = channel.sendMessage(prompt).complete();
		channel.getJDA().addEventListener(this);
		latch = new CountDownLatch(1);
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		channel.getJDA().removeEventListener(this);
		
		System.out.println("Returning answer");
		
		return ret;
	}
	
	public void promptAsync(Consumer<String> callback){
		
		this.callback = callback;

		tempMessage = channel.sendMessage(prompt).complete();
		channel.getJDA().addEventListener(this);
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		super.onMessageReceived(event);
		
		if(channel.equals(event.getChannel()) && event.getAuthor().equals(user)){
			ret = event.getMessage().getContentRaw();
			if(latch != null){
				latch.countDown();
			}else if(callback != null){
				channel.getJDA().removeEventListener(this);
				callback.accept(ret);
			}else {
				System.out.println("Impossible case");
				return;
			}
			if(deleteDelay != -1){
				MessageTimer.deleteAfter(tempMessage, deleteDelay);
				MessageTimer.deleteAfter(event.getMessage(), deleteDelay);
			}
			System.out.println("Callback");
		}
		//System.out.println("Wrong channel or user");
	}
	
}
