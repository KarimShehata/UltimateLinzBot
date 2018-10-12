package ampullen;

import java.io.IOException;
import java.net.ServerSocket;

import javax.security.auth.login.LoginException;

import org.json.JSONTokener;
import org.json.JSONWriter;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class Main{

	public static void main(String[] args){
		
		String port = System.getenv("PORT");
		System.out.println(port);
		try {
			ServerSocket ss = new ServerSocket(Integer.parseInt(port));
			new Thread(() -> {
				try {
					while(!ss.isClosed()){
						ss.accept();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
		} catch (NumberFormatException | IOException e1) {
			e1.printStackTrace();
		}
		
		
		JDA jda = null;
		try {
			jda = new JDABuilder("NDk4OTQ5MTg4MzQ4OTM2MTky.Dp3YEg.jjeXldfvrQncYJZDwC3Sl_o8QBE").build();
		} catch (LoginException e) {
			e.printStackTrace();
		}
		
		//JDABot bot = jda.asBot();
		jda.addEventListener(new EventListener() {
			
			public void onEvent(Event event) {
				if(event instanceof ReadyEvent) {
					System.out.println("API is ready");
				}
			}
		});
		jda.addEventListener(new MainListener());
		jda.addEventListener(new MenuListener());
		
		
	}
	
}
