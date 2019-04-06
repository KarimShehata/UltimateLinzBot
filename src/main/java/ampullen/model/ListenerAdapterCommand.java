package ampullen.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import ampullen.Main;
import ampullen.MessageTimer;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public abstract class ListenerAdapterCommand extends ListenerAdapter{
	
	protected String cmd;
	Message lastMessage;
	
	public ListenerAdapterCommand(String commandString) {
		this.cmd = commandString;
		this.lastMessage = null;
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		super.onMessageReceived(event);
		
		MessageChannel channel = event.getChannel();
		String msg = event.getMessage().getContentRaw();
		
		if(!event.getJDA().getSelfUser().equals(event.getAuthor())){
			if(channel.getType() == ChannelType.GROUP || channel.getType() == ChannelType.PRIVATE || channel.getType() == ChannelType.TEXT){
				if(msg.startsWith(cmd + " ")){
					
					System.out.println("Message: " + event.getMessage().getContentDisplay() + " MessageId " + event.getMessageId());
					lastMessage = event.getMessage();
					boolean b = command(event, msg);
					
					if(!b){
						//TODO Command not recognized
					}
					
				}
			}
		}
	}
	
	public abstract void help(MessageReceivedEvent event, String[] msg);

	private boolean command(MessageReceivedEvent event, String msg) {
		
		String[] tokens = msg.split(" ");
		
		if(tokens.length < 2){
			help(event, tokens);
			return true;
		}
		
		for(Method m : this.getClass().getMethods()){
			
			if(m.getName().equalsIgnoreCase(tokens[1])){

				boolean hasPermission = false;

				if(m.isAnnotationPresent(Permissioned.class)){

					Permissioned permissioned = m.getAnnotation(Permissioned.class);

					List<Role> roles = event.getJDA().getGuilds().get(0).getMember(event.getAuthor()).getRoles();
					hasPermission = roles.stream()  //Guilds noch ordentlich machen...
						.anyMatch(x ->
								Arrays.stream(permissioned.value()).anyMatch(y -> y.equalsIgnoreCase(x.getName()))
						);
				}else{
					hasPermission = true;
				}

				if(hasPermission) {
					try {
						if (m.isAnnotationPresent(Blocking.class)) {
							new Thread(() -> {
								try {
									m.invoke(this, event, tokens);
								} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
									e.printStackTrace();
								}
							}).start();
						} else {
							m.invoke(this, event, tokens);
						}
						return true;
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						//TODO, Command not found, help()
						e.printStackTrace();
					}
				}else{
					send(event.getChannel(), "Für diesen Befehl hast du nicht die nötigen Berechtigungen");
				}
			}
		}
		deleteCommandAfter(5000);
		return false;
	}
	
	public void send(MessageChannel c, String msg){
		c.sendMessage(msg).submit();
	}

	public void send(MessageChannel messageChannel, Message message){
		messageChannel.sendMessage(message).submit();
	}
	
	public Message sendSync(MessageChannel c, String msg) {
		return c.sendMessage(msg).complete();
	}

	public void deleteCommandAfter(long millis) {
		MessageTimer.deleteAfter(lastMessage, millis);
	}
	
}
