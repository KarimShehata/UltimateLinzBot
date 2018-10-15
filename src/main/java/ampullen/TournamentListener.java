package ampullen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.collections4.multiset.SynchronizedMultiSet;

import ampullen.jsondb.JsonModel;
import ampullen.model.ListenerAdapterCommand;
import ampullen.model.Tournament;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.IPermissionHolder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.Permission;

public class TournamentListener extends ListenerAdapterCommand{
	
	public TournamentListener() {
		super("/t");
	}
	
	public Tournament getTournamentByChannel(MessageChannel c, User author, boolean ask) {
		
		Tournament tournament = JsonModel.getInstance()
				.tournaments().stream()
				.filter(x -> x.getAnnouncementChannel() == c.getIdLong())
				.findFirst().orElse(null);
		
		if(tournament == null && ask) {
			
			String tournamentname = new Prompt("Welches Turnier?", c, author).promptSync();
			
			tournament = JsonModel.getInstance().tournaments().stream()
			.filter(x -> x.getName().toLowerCase().startsWith(tournamentname.toLowerCase()))
			.findFirst().orElse(null);
			
		}
		
		return tournament;
		
	}
	
	public void editinfo(MessageReceivedEvent event, String[] msg){
		
		Tournament tournament = getTournamentByChannel(event.getChannel(), event.getAuthor(), true);
		
		if(tournament == null) {
			send(event.getChannel(), "Turnier nicht vorhanden - Abbruch");
			return;
		}
		
		String parameter = "";
		
		if(msg.length == 2) {
			
			String prompt = "Folgende Felder stehen zur Auswahl: \n";
			for(String field : TournamentCreator.translations){
				
				String[] arr = field.split("-");
				
				prompt += String.format(" - %s: %s\n", arr[0], arr[1]);
				
			}
			prompt += "Welches Infofeld willst du ändern?";
			
			parameter = new Prompt(prompt, event.getChannel(), event.getAuthor()).promptSync();
			
			String finalParameter = parameter;
			
			boolean exists = Arrays.asList(TournamentCreator.translations).stream().map(x -> x.split("-")[0]).anyMatch(x -> x.startsWith(finalParameter.toLowerCase()));
			
			if(!exists) {
				
				parameter = new Prompt("Feld existiert nicht - du kannst nochmal probieren", event.getChannel(), event.getAuthor()).promptSync();
				
				String finalParameter2 = parameter;
				
				exists = Arrays.asList(TournamentCreator.translations).stream().map(x -> x.split("-")[0]).anyMatch(x -> x.startsWith(finalParameter2.toLowerCase()));
				
				if(!exists) {
					event.getChannel().sendMessage("Abbruch").complete();
				}
				
			}
			
		}else if(msg.length > 2) {
			
			parameter = Arrays.asList(Arrays.copyOfRange(msg, 2, msg.length)).stream().reduce((x, y) -> x + " " + y).orElse(null);
			
		}
		
		String value = new Prompt("Neuer Wert?", event.getChannel(), event.getAuthor()).promptSync();
		
		TournamentCreator.parseFieldInto(tournament, value, parameter);
		
	}
	
	public void archive(MessageReceivedEvent event, String[] msg){
		
		Tournament tournament = getTournamentByChannel(event.getChannel(), event.getAuthor(), false);
		
		new Thread(() -> {
		
		if(msg.length >= 2){
			
			if(tournament == null) {
				System.out.println("Tournament null!");
				return;
			}
			
			String response = new Prompt("Do you really want to archive " + tournament.getName() + "?", event.getChannel(), event.getAuthor()).promptSync();
			
			response = response.toLowerCase();
			
			if(response.startsWith("y") || response.startsWith("j")){
				
				
				//Removal operations
				
				TextChannel channel = event.getJDA().getTextChannelById(tournament.getAnnouncementChannel());

				Guild guild = event.getJDA().getGuilds().get(0);
				
				Category c = guild.getCategoriesByName("Archiv", true).stream().findFirst().orElse(null);
				
				List<Channel> channels = c.getChannels();
				
				System.out.println(channel.getPositionRaw() + "," + channel.getPosition());
				
				int position = channels.size() > 0 ? channels.get(channels.size()-1).getPositionRaw() + 1 : c.getPositionRaw();
				channel.getManager().setParent(c);
				channel.getManager().setPosition(position);
				//guild.getController().modifyTextChannelPositions().selectPosition(channel).moveTo(position).complete();
				
				//JsonModel.getInstance().tournaments().remove(tournament);
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(tournament.getDate()));
				
				tournament.setName(tournament.getName() + "-" + cal.get(Calendar.YEAR));
				channel.getManager().setName(tournament.getName().replaceAll(" ", "-")).complete();
				
				send(event.getChannel(), "Tournament " + tournament.getName() + " archived!");
				
			}else{
				send(event.getChannel(), "Archivation aborted");
			}
			
			System.out.println("Archived " + tournament);
			
		}
		
		}).start();
		
	}

	public void create(MessageReceivedEvent event, String[] msg){
		
		String response = "Der Erstellungsprozess wird als Privatnachricht fortgesetzt";
		
		event.getChannel().sendMessage(response).complete();
		
		PrivateChannel channel = event.getAuthor().openPrivateChannel().complete();
		
		Consumer<Tournament> consumer = x -> {
			
			Guild guild = event.getJDA().getGuilds().get(0);
			
			Category c = guild.getCategoriesByName("Turniere", true).get(0);
			List<Channel> channels = c.getChannels();
			int last = channels.size() > 0 ? channels.get(channels.size()-1).getPosition() : 0;
			
			TextChannel newc = (TextChannel)c.createTextChannel(x.getName()).complete();
			
			//Permissions
			List<Role> roles = guild.getRolesByName(x.getDivision(), true); //TODO Better, so typos or more Divisions are supported
			System.out.println(roles.toString());
			if(roles.size() > 0) {
				
				List<Permission> permissions = Arrays.stream(Permission.values())
				.filter(Permission::isChannel)
				.filter(p -> p.getRawValue() != Permission.MESSAGE_MANAGE.getRawValue())
				.collect(Collectors.toList());
				long raw = Permission.getRaw(permissions);
				
				roles.forEach(y -> {
					newc.getManager().putPermissionOverride(roles.get(0), raw, Permission.MESSAGE_MANAGE.getRawValue()).complete();
				});
			}
			
			Message m = newc.sendMessage(x.getInfoMarkup()).complete();
			newc.pinMessageById(m.getIdLong()).complete();
			x.setAnnouncementChannel(newc.getIdLong());
			
			//event.getGuild().getController().modifyTextChannelPositions().selectPosition(newc).moveTo(last + 1);
			
			JsonModel.getInstance().tournaments().add(x);
		};
		
		new TournamentCreator(channel).create(consumer);
		
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
