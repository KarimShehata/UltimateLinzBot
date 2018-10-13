package ampullen;

import ampullen.jsondb.JsonModel;
import ampullen.model.ListenerAdapterCommand;
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
		
		System.out.println("create");
		
	}
	
	@Override
	public void help(MessageReceivedEvent event, String[] msg){
		
		String help = "Mit /tournament erstellst und verwaltest du Turniere\n"
				+ "Alle verfügbaren Optionen:"
				+ "  * help - Ruft die Hilfe auf"
				+ "  * create - erstellt ein neues Turnier";
		
		send(event.getChannel(), help);
		
		System.out.println("help");
		
	}
	
	

}
