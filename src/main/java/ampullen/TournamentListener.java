package ampullen;

import java.util.Arrays;

import ampullen.jsondb.JsonModel;
import ampullen.model.ListenerAdapterCommand;
import ampullen.model.Tournament;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class TournamentListener extends ListenerAdapterCommand{
	
	public TournamentListener() {
		super("/tournament");
	}

	public void create(MessageReceivedEvent event, String[] msg){
		
		String response = "Der Erstellungsprozess wird als Privatnachricht fortgesetzt";
		
		event.getChannel().sendMessage(response).complete();
		
		PrivateChannel channel = event.getAuthor().openPrivateChannel().complete();
		
		new TournamentCreator(channel).create(x -> JsonModel.getInstance().addTournament(x));
		
	}
	
	public void list(MessageReceivedEvent event, String[] msg){
		
		System.out.println(JsonModel.getInstance().getTournaments().size());
		
		String s = "Created Tournaments: " + JsonModel.getInstance().getTournaments().stream().map(x -> x.getName()).reduce((x, y) -> x + ", " + y).orElse("Keine Turniere zurzeit verfügbar");
		
		send(event.getChannel(), s);
		
	}
	
	public void info(MessageReceivedEvent event, String[] msg){
		
		if(msg.length >= 3){
			
			String tournament = Arrays.asList(Arrays.copyOfRange(msg, 2, msg.length)).stream().reduce("", (x, y) -> x + " " + y).trim() ;
			
			System.out.println(msg.toString());
			
			Tournament t = JsonModel.getInstance().getTournaments().stream().filter(x -> x.getName().toLowerCase().startsWith(tournament.toLowerCase())).findFirst().orElse(null);
			if(t != null){
				
				send(event.getChannel(), t.getInfoMarkup());
				
			}else{
				send(event.getChannel(), "Turnier konnte nicht gefunden werden!");
			}
			
		}
		
	}
	
	@Override
	public void help(MessageReceivedEvent event, String[] msg){
		
		String help = "Mit /tournament erstellst und verwaltest du Turniere\n"
				+ "Alle verfügbaren Optionen:"
				+ "  * help - Ruft die Hilfe auf"
				+ "  * create - erstellt ein neues Turnier"
				+ "  * info x - Info zu Turnier x";
		
		send(event.getChannel(), help);
		
		System.out.println("help");
		
	}
	
	

}
