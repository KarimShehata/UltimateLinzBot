package ampullen;

import java.io.*;

import javax.security.auth.login.LoginException;

import ampullen.jsondb.JsonModel;
import ampullen.model.Tournament;
import ampullen.registration.RegistrationListener;
import ampullen.tournament.TournamentListener;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class Main{

    public static JDA jda;

	public static void main(String[] args){

        String token = GetToken();

		try {
			jda = new JDABuilder(token)
					.setGame(Game.playing("Ultimate Frisbee"))
					.build();
		} catch (LoginException e) {
		    e.printStackTrace();
		}

		//JDABot bot = jda.asBot();
		jda.addEventListener((EventListener) event -> {
            if(event instanceof ReadyEvent) {
                System.out.println("API is ready");
                
                initTournaments();
            }
        });
		jda.addEventListener(new MainListener());
		jda.addEventListener(new TournamentListener());
        jda.addEventListener(new RegistrationListener());
        //jda.addEventListener(new TestListener());
	}
	
	private static void initTournaments() {
		
		for(Tournament t : JsonModel.getInstance().tournaments()){
			initTournament(t);
		};
		
	}
	
	
	public static void initTournament(Tournament t) {
		TextChannel announcementchannel = jda.getTextChannelById(t.getAnnouncementChannel());
		if(announcementchannel != null) {
			if(t.getName().contains("anta")) {
				t.getVotes().setAttendanceMsg(announcementchannel.getMessageById(501334882945859584L).complete());
			}
			try {
				t.getVotes().setAttendanceMsg(announcementchannel.getMessageById(t.getVotes().attendanceMsgId).complete());
			}catch(Exception e) {
				System.out.println("Attendancemessage not found");
			}
			try {
				t.getVotes().setEatingMsg(announcementchannel.getMessageById(t.getVotes().eatingMsgId).complete());
			}catch(Exception e) {
				System.out.println("Eatingmessage not found");
			}
			
		}
	}

	//reads the discord bot token from token.txt
    private static String GetToken() {

        String token = "NDk4OTQ5MTg4MzQ4OTM2MTky.Dp3YEg.jjeXldfvrQncYJZDwC3Sl_o8QBE";

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

        return token.trim();
    }

}
