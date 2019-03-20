package ampullen.tournament;

import ampullen.Main;
import ampullen.MessageTimer;
import ampullen.Utilities;
import ampullen.helper.Conversation;
import ampullen.helper.Prompt;
import ampullen.jsondb.JsonModel;
import ampullen.model.Blocking;
import ampullen.model.ListenerAdapterCommand;
import ampullen.model.Permissioned;
import ampullen.model.Tournament;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TournamentListener extends ListenerAdapterCommand{
	
	public TournamentListener() {
		super("t");
	}

	private Tournament getTournament(String[] args, int index, Message msg){

		if(args.length > index){

			String name = args[index];
			return JsonModel.getInstance().findTouramentByName(name);

		}

		String tournamentname = new Prompt("Welches Turnier?", msg.getChannel(), msg.getAuthor()).promptSync();
		return JsonModel.getInstance().findTouramentByName(tournamentname);

	}

	@Permissioned({"Vorstand"})
	@Blocking
	public void editinfo(MessageReceivedEvent event, String[] msg){

		//TODO Argument 2 und 3 funktionieren nicht richtig

		Tournament tournament = getTournament(msg, 3, event.getMessage());
		
		if(tournament == null) {
			send(event.getChannel(), "Turnier nicht gefunden!");
			return;
		}
		
		String parameter = "";
		
		deleteCommandAfter(30000);
		
		if(msg.length == 2) {
			
			String prompt = "Folgende Felder stehen zur Auswahl: \n";
			for(String field : TournamentCreator.translations){
				
				String[] arr = field.split("-");

				prompt += String.format(" - %s: %s\n", arr[0], arr[1]);
				
			}
			prompt += "Welches Infofeld willst du ändern?";
			
			parameter = new Prompt(prompt, event.getChannel(), event.getAuthor()).setDelete(30000).promptSync();
			
			System.out.println("Param: " + parameter);
			
			String finalParameter = parameter;
			
			boolean exists = Arrays.stream(TournamentCreator.translations).map(x -> x.split("-")[0]).anyMatch(x -> x.startsWith(finalParameter.toLowerCase()));
			
			if(!exists) {
				
				parameter = new Prompt("Feld existiert nicht - du kannst nochmal probieren", event.getChannel(), event.getAuthor()).setDelete(30000).promptSync();
				
				String finalParameter2 = parameter;
				
				exists = Arrays.stream(TournamentCreator.translations).map(x -> x.split("-")[0]).anyMatch(x -> x.startsWith(finalParameter2.toLowerCase()));
				
				if(!exists) {
					Message m = event.getChannel().sendMessage("Abbruch").complete();
					MessageTimer.deleteAfter(m, 20000);
					return;
				}
				
			}
			
		}else if(msg.length > 2) {
			
			parameter = Arrays.stream(Arrays.copyOfRange(msg, 2, msg.length)).reduce((x, y) -> x + " " + y).orElse(null);
			
		}
		
		String value = new Prompt("Neuer Wert?", event.getChannel(), event.getAuthor()).setDelete(30000).promptSync();
		
		TournamentCreator.parseFieldInto(tournament, value, TournamentCreator.getFieldFromLabel(parameter));
		
		/*MessageTimer.deleteAfter(*/sendSync(event.getChannel(), "Feld " + parameter + " gesetzt auf " + value);//, 30000);
		
	}

	@Permissioned({"Vorstand", "Moderator"})
	@Blocking
	public void archive(MessageReceivedEvent event, String[] msg){
		
		Tournament tournament = getTournament(msg, 2, event.getMessage());
		
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

				if(c == null){
					System.out.println("Category Archive is not created!!");
					return;
				}

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

	@Permissioned({"Vorstand", "Moderator"})
	public void create(MessageReceivedEvent event, String[] msg){
		
		PrivateChannel channel;
		
		if(event.getChannel().getType() != ChannelType.PRIVATE) {
			
			String response = "Der Erstellungsprozess wird als Privatnachricht fortgesetzt";
			
			event.getChannel().sendMessage(response).complete();
			
			channel = event.getAuthor().openPrivateChannel().complete();
			
		}else {
			channel = event.getPrivateChannel();
		}

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
					newc.getManager().putPermissionOverride(y, raw, Permission.MESSAGE_MANAGE.getRawValue()).complete();
				});
			}
			
			//Deny Vereinsmitglied Writing Permission
			Role member = guild.getRolesByName("Vereinsmitglied", true).stream().findFirst().orElse(null);
			if(member != null) {
				
				newc.getManager().putPermissionOverride(member, 0, Permission.MESSAGE_WRITE.getRawValue());
				
			}
			
			//Give the bot the power he deserves
			Role bot = guild.getRolesByName("Bot", true).stream().findFirst().orElse(null);
			if(bot != null) {
				newc.getManager().putPermissionOverride(bot, Permission.ALL_CHANNEL_PERMISSIONS | Permission.ALL_TEXT_PERMISSIONS, 0);
			}

			PinMessageRemoveListener pinremover = new PinMessageRemoveListener(newc);

			event.getJDA().addEventListener(pinremover);

			//Create Info post
			Message m = newc.sendMessage(x.getInfoMarkup()).complete();
			m.pin().submit();
			x.getVotes().setAttendanceMsg(m);
			
			m = newc.sendMessage("Fleisch / Veggie").complete();
			x.getVotes().setEatingMsg(m);
			//newc.pinMessageById(m.getIdLong()).complete();
			
			/*new EmoteLimiter(m)
			.setAllowedEmotes(Arrays.asList("in", "50", "out"))
			.setDisplayAllowed(true)
			.setLimitReactions(true).start(event.getChannel());*/

			event.getJDA().removeEventListener(pinremover);
			
			x.setAnnouncementChannel(newc.getIdLong());
			
			//event.getGuild().getController().modifyTextChannelPositions().selectPosition(newc).moveTo(last + 1);

			x.init(event.getJDA());
			JsonModel.getInstance().tournaments().add(x);
			
			Main.initTournament(x);
		};
		
		new TournamentCreator(channel).create(consumer);
		
	}
	
	@Blocking
	public void test(MessageReceivedEvent event, String[] msg) {
		
		/*String response = new Prompt("PromptTest", event.getChannel(), event.getAuthor()).promptSync();
		System.out.println(response);
		
		send(event.getChannel(), response);*/

		/*Message m = event.getChannel().getMessageById(501649064887189504L).complete();
		
		new EmoteLimiter(m)
		.setAllowedEmotes(Arrays.asList("in", "50", "out"))
		.setDisplayAllowed(true)
		.setLimitReactions(true).start(event.getChannel());
		System.out.println("test");*/

		Message m = event.getChannel().sendMessage("Testmessage").complete();
		PinMessageRemoveListener pinremover = new PinMessageRemoveListener(event.getTextChannel());
		event.getJDA().addEventListener(pinremover);
		m.pin().complete();

		event.getJDA().removeEventListener(pinremover);
		
		Conversation c = new Conversation()
				.build()
				.addStepA("Wie gehts dir?", (x, y) -> y.c.put("1", x))
				.addOption("Wirklich?", x -> x.equals("Ja") ? "true" : "false")
					.addOption("false", "Was dann?", (x, y) -> x.equals("Ja") ? "ja" : "nein")
						.addStepLast("ja", "Wieso dann nicht wirklich?? Antworte nicht", (x, y) -> {})
						.addStepLast("nein", "Ok." , null)
						.done()
					.addStepLast("true", "Consistent", (x, y) -> {})
					.done()
				.finished(x -> System.out.println("How: " + x.c.get("1")))
				.start(event.getChannel(), event.getAuthor())
				.out();
			
		;
		
	}
	
	public void list(MessageReceivedEvent event, String[] msg){
		
		System.out.println(JsonModel.getInstance().tournaments().size());
		
    	String s = "Created Tournaments: \n" + JsonModel.getInstance().tournaments().stream()
    			.sorted((x, y) -> Long.compare(x.getDate(), y.getDate()))
    			.map(Tournament::getName)
    			.reduce((x, y) -> x + "\n" + y).orElse("Keine Turniere zurzeit verfuegbar");
		
		Message m = sendSync(event.getChannel(), s);
		MessageTimer.deleteAfter(m, 15000);
		deleteCommandAfter(15000);
		
	}
	
	public void info(MessageReceivedEvent event, String[] msg){
		Tournament t = getTournament(msg, 2, event.getMessage());
		
		if(t != null){
			MessageTimer.deleteAfter(sendSync(event.getChannel(), t.getInfoMarkup()), 30000);
		}else{
			MessageTimer.deleteAfter(sendSync(event.getChannel(), "Turnier konnte nicht gefunden werden!"), 15000);
		}
		deleteCommandAfter(3000);
	}
	
	@Override
	public void help(MessageReceivedEvent event, String[] msg){

		int padding = 12;

		String info = "Mit \"/tournament\" oder \"/t\" erstellst und verwaltest du Turniere\n"
				+ "Alle verfügbaren Optionen:\n";

		String commands = Utilities.padRight("help", padding) + "Ruft die Hilfe auf\n"
						+ Utilities.padRight("create", padding) + "Erstellt ein neues Turnier\n"
						+ Utilities.padRight("info [x]", padding) + "Infos zu aktuellen Turnier [x = Turniername]"
						+ Utilities.padRight("list", padding) + "Listet alle erstellten Turniere"
						+ Utilities.padRight("archive", padding) + "Archiviert ein Turnier";

		MessageBuilder messageBuilder = new MessageBuilder();
		messageBuilder.append(info);
		messageBuilder.appendCodeBlock(commands, "");

		send(event.getChannel(), messageBuilder.build());
		
		System.out.println("help");
	}
}