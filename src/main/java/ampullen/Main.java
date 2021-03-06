package ampullen;

import java.io.*;

import javax.security.auth.login.LoginException;

import ampullen.jsondb.JsonModel;
import ampullen.model.Tournament;
import ampullen.tournament.TournamentListener;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class Main{

	public static JDA JDA;
	public static DataModel DataModel;

	static String prefix;
	public static boolean isInDeveloperMode = true;

	public static void main(String[] args){

        String token = GetToken();
		prefix = isInDeveloperMode ? "?" : "!";

		DataModel = ampullen.DataModel.Initialize();

		try {
			JDA = new JDABuilder(token)
					.setGame(Game.playing(prefix +"help"))
					.build();
		} catch (LoginException e) {
		    e.printStackTrace();
		}

		//JDABot bot = JDA.asBot();
		JDA.addEventListener((EventListener) event -> {
            if(event instanceof ReadyEvent) {
                System.out.println("API is ready");

                JsonModel.getInstance().tournaments();
            }
        });

		JDA.addEventListener(new TournamentListener(prefix + "t"));
		JDA.addEventListener(new PollListener(prefix + "p"));
		JDA.addEventListener(new HelpListener());

		//JDA.addEventListener(new MainListener());
		//JDA.addEventListener(new RegistrationListener());
	}

	//reads the discord bot token from token.txt
    private static String GetToken() {

        String token = "NTQ5MjIyMjAzNzc0OTkyMzg2.D1Qu0Q.jnmG2y3lN9OL8QFpdNr_8pdwEKU";

        File file = new File("token.txt");

        if (!file.exists()) {
            System.out.println("token.txt file not found. Using internal token");
            return token;
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
            System.out.println("Something went wrong. Using internal token");
        }

        isInDeveloperMode = false;

        return token.trim();
    }

}