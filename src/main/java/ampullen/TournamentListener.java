package ampullen;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import ampullen.jsondb.JsonModel;
import ampullen.model.ListenerAdapterCommand;
import ampullen.model.Tournament;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class TournamentListener extends ListenerAdapterCommand{
	
	public TournamentListener() {
		super("/t");
	}
	
	public void set(MessageReceivedEvent event, String[] msg){
		
		if(msg.length >= 2){

			String s = msg.length == 2 ? "Folgende Felder stehen zur Auswahl: \n" : "";
			for(String field : TournamentCreator.translations){
				
				String[] arr = field.split("-");
				
				if(msg.length != 2){
					
					if(msg[2].toLowerCase().startsWith(arr[0])){
						
						if(TournamentCreator.parseFieldInto(null, msg[3], msg[2])){
							s = "Feld geändert";
						}else{
							s = "Feld nicht geändert - Fehler aufgetreten";
						}
						
					}
					
				}else{
					
					//Display Info
					s += String.format(" - %s: %s\n", arr[0], arr[1]);
					
				}
			}
			
			send(event.getChannel(), s);
			
		}else{
			
			help(event, null);
			
		}
		
	}
	
	public void delete(MessageReceivedEvent event, String[] msg){
		
		new Thread(() -> {
		
		if(msg.length >= 2){
			
			String tournament = msg[2];
			
			String response = new Prompt("Do you really want to delete " + tournament, event.getChannel(), event.getAuthor()).promptSync();
			
			response = response.toLowerCase();
			
			if(response.startsWith("y") || response.startsWith("j")){
				
				List<Tournament> list = JsonModel.getInstance().tournaments();
				List<Tournament> t = list.stream().filter(x -> x.getName().toLowerCase().startsWith(tournament.toLowerCase())).collect(Collectors.toList());
				list.removeAll(t);
				
				send(event.getChannel(), "Tournament " + list.stream().map(x -> x.getName()).reduce((x, y) -> x + ", " + y).orElse("none") + " deleted!");
				
			}else{
				send(event.getChannel(), "Deletion aborted");
			}
			
			System.out.println("Deleted " + tournament);
			
		}
		
		}).start();
		
	}

	public void create(MessageReceivedEvent event, String[] msg){
		
		String response = "Der Erstellungsprozess wird als Privatnachricht fortgesetzt";
		
		event.getChannel().sendMessage(response).complete();
		
		PrivateChannel channel = event.getAuthor().openPrivateChannel().complete();
		
		new TournamentCreator(channel).create(x -> JsonModel.getInstance().tournaments().add(x));
		
	}
	
	public void list(MessageReceivedEvent event, String[] msg){
		
		System.out.println(JsonModel.getInstance().tournaments().size());
		
    	String s = "Created Tournaments: " + JsonModel.getInstance().tournaments().stream().map(x -> x.getName()).reduce((x, y) -> x + ", " + y).orElse("Keine Turniere zurzeit verfuegbar");
		
		send(event.getChannel(), s);
		
	}
	
	public void info(MessageReceivedEvent event, String[] msg){
		
		if(msg.length >= 3){
			
			String tournament = Arrays.asList(Arrays.copyOfRange(msg, 2, msg.length)).stream().reduce("", (x, y) -> x + " " + y).trim() ;
			
			System.out.println(msg.toString());
			
			Tournament t = JsonModel.getInstance().tournaments().stream().filter(x -> x.getName().toLowerCase().startsWith(tournament.toLowerCase())).findFirst().orElse(null);
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
