package ampullen;

import java.io.*;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class Main{

	public static void main(String[] args){

        String token = GetToken();
        
        if(token.isEmpty()) {
            System.out.println("token.txt is empty");
            System.exit(0);
        }

        JDA jda = null;
		try {
			jda = new JDABuilder(token)
					.setGame(Game.playing("Ultimate Frisbee"))
					.build();
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
		jda.addEventListener(new TournamentListener());

		
	}

	//reads the discord bot token from token.txt
    private static String GetToken() {

        String token = "";

        File file = new File("token.txt");

        if (!file.exists()) {
            System.out.println("token.txt file not found.");
            System.exit(0);
        }
        
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();

            while (line != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator());
                line = bufferedReader.readLine();
            }
            token = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return token;
    }

}
