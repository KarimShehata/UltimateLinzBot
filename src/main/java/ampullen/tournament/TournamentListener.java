package ampullen.tournament;

import ampullen.MessageTimer;
import ampullen.Utilities;
import ampullen.helper.PersistentMessage;
import ampullen.helper.Prompt;
import ampullen.jsondb.JsonModel;
import ampullen.model.*;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static ampullen.jsondb.JsonModel.getInstance;

public class TournamentListener extends ListenerAdapterCommand{
	
	public TournamentListener(String commandString) {
		super(commandString);
	}

	private Tournament getTournament(String[] args, int index, Message msg){

		if(args.length > index){

			String name = args[index];
			return getInstance().tournaments().stream()
                    .filter(x -> x.getName().toLowerCase().startsWith(name.toLowerCase()))
                    .findFirst().orElse(null);

		}

		String tournamentname = new Prompt("Welches Turnier?", msg.getChannel(), msg.getAuthor()).promptSync();
		return getInstance().tournaments().stream()
                .filter(x -> x.getName().toLowerCase().startsWith(tournamentname.toLowerCase()))
                .findFirst().orElse(null);

	}

	@Permissioned({"Vorstand", "Moderator"})
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
				
				Category c = guild.getCategoriesByName("Turnierarchiv", true).stream().findFirst().orElse(null);

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

			new Thread(() -> {

				try {
					Thread.sleep(12000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				List<Role> roles = guild.getRolesByName("Vereinsmitglied", true);
				System.out.println(roles.toString());
				if(roles.size() > 0) {

					List<Permission> permissions = new ArrayList<>(Arrays.asList(
							Permission.MESSAGE_ADD_REACTION,
							Permission.VIEW_CHANNEL,
							Permission.MESSAGE_WRITE,
							Permission.MESSAGE_READ,
							Permission.MESSAGE_TTS,
							Permission.MESSAGE_EMBED_LINKS,
							Permission.MESSAGE_ATTACH_FILES,
							Permission.MESSAGE_EXT_EMOJI
					));

					long raw = Permission.getRaw(permissions);

					roles.forEach(y -> {

						newc.getManager().putPermissionOverride(y, raw, 0).complete();

					});
				}
			}).start();

			
			//Deny Vereinsmitglied Writing Permission
//			Role member = guild.getRolesByName("Vereinsmitglied", true).stream().findFirst().orElse(null);
//			if(member != null) {
//
//				newc.getManager().putPermissionOverride(member, 0, Permission.MESSAGE_WRITE.getRawValue()).complete();
//
//			}

			//Give the bot the power he deserves
			Role bot = guild.getRolesByName("Bot", true).stream().findFirst().orElse(null);
			if(bot != null) {
				newc.getManager().putPermissionOverride(bot, Permission.ALL_CHANNEL_PERMISSIONS | Permission.ALL_TEXT_PERMISSIONS, 0).complete();
			}

			PinMessageRemoveListener pinremover = new PinMessageRemoveListener(newc);

			pinremover.afterCount(2, () ->
					event.getJDA().removeEventListener(pinremover));

			event.getJDA().addEventListener(pinremover);

			//Create Info post
			Message m = newc.sendMessage(x.getInfoMarkup()).complete();
			x.getVotes().setAttendanceMsg(m);

			Message tableMessage = newc.sendMessage("AttendanceTable").complete();
			x.getVotes().setTableMessage(tableMessage);
			
			m = newc.sendMessage("Fleisch / Veggie").complete();
			x.getVotes().setEatingMsg(m);

			m.pin().complete(); //Pin here, so the Info Message appears on the top
			
			x.setAnnouncementChannel(newc.getIdLong());
			
			//event.getGuild().getController().modifyTextChannelPositions().selectPosition(newc).moveTo(last + 1);

			x.init(event.getJDA());
			getInstance().tournaments().add(x);
		};
		
		new TournamentCreator(channel).create(consumer);
		
	}
	
	@Blocking
	public void test(MessageReceivedEvent event, String[] msg) {


		List<Role> roles = event.getGuild().getRolesByName("Mixed", true); //TODO Better, so typos or more Divisions are supported
		System.out.println(roles.toString());
		if(roles.size() > 0) {

			List<Permission> permissions = new ArrayList<>(Arrays.asList(
					Permission.MESSAGE_ADD_REACTION,
					Permission.VIEW_CHANNEL,
					Permission.MESSAGE_WRITE,
					Permission.MESSAGE_READ,
					Permission.MESSAGE_TTS,
					Permission.MESSAGE_EMBED_LINKS,
					Permission.MESSAGE_ATTACH_FILES,
					Permission.MESSAGE_EXT_EMOJI
			));

//				List<Permission> permissions = Arrays.stream(Permission.values())
//				.filter(Permission::isChannel)
//				.filter(p -> p.getRawValue() != Permission.MESSAGE_MANAGE.getRawValue())
//				.collect(Collectors.toList());
			long raw = Permission.getRaw(permissions);

			roles.forEach(y -> {

//				permissions.forEach(p -> event.getGuild().getTextChannelById(564098472295399424L).getManager().putPermissionOverride(y, p.getRawValue(), 0).complete());
				event.getGuild().getTextChannelById(564098472295399424L).getManager().putPermissionOverride(y, raw, 0).complete();
			});
		}

//		PersistentMessage info = new PersistentMessage(event.getMessage(),
//				Arrays.asList("in", "50", "out"),
//				true);
//
//		info.init();

//		event.getJDA().getTextChannelById(557833273456328716L).getMessageById(557833275889025034L).complete()
//		.getReactions().forEach(x -> {
//			System.out.println(x.toString() + " | ");
//			x.getUsers().complete().forEach(y -> System.out.print(y.getName()));
//			System.out.println();
//		});
		
//		Conversation c = new Conversation()
//				.build()
//				.addStepA("Wie gehts dir?", (x, y) -> y.c.put("1", x))
//				.addOption("Wirklich?", x -> x.equals("Ja") ? "true" : "false")
//					.addOption("false", "Was dann?", (x, y) -> x.equals("Ja") ? "ja" : "nein")
//						.addStepLast("ja", "Wieso dann nicht wirklich?? Antworte nicht", (x, y) -> {})
//						.addStepLast("nein", "Ok." , null)
//						.done()
//					.addStepLast("true", "Consistent", (x, y) -> {})
//					.done()
//				.finished(x -> System.out.println("How: " + x.c.get("1")))
//				.start(event.getChannel(), event.getAuthor())
//				.out();
			
		;
		
	}
	
	public void list(MessageReceivedEvent event, String[] msg){
		
		System.out.println(getInstance().tournaments().size());
		
    	String s = "Created Tournaments: \n" + getInstance().tournaments().stream()
    			.sorted((x, y) -> Long.compare(x.getDate(), y.getDate()))
    			.map(Tournament::getName)
    			.reduce((x, y) -> x + "\n" + y).orElse("Keine Turniere zurzeit verfuegbar");

		Message m = event.getChannel().sendMessage(new MessageBuilder().appendCodeBlock(s, "").build()).complete();
//		MessageTimer.deleteAfter(m, 15000);
//		deleteCommandAfter(15000);
		
	}

	@Permissioned({"Moderator", "Admin"})
	@Blocking
	public void rereadPolls(MessageReceivedEvent event, String[] msg){

		List<Tournament> tournaments = JsonModel.getInstance().tournaments();

		tournaments.forEach(x -> {
			TextChannel channel = event.getJDA().getTextChannelById(x.getAnnouncementChannel());

			Map<String, TournamentVotes.Choices> map = new HashMap<>();

			Message attmsg = channel.getMessageById(x.getVotes().attendanceMsgId).complete();

			attmsg.getReactions().forEach(y -> {
				y.getUsers().complete().forEach(u -> {
					TournamentVotes.Choices c = new TournamentVotes.Choices();
					if(y.getReactionEmote().getName().equals(":in:")){

						c.setAttendance(TournamentVotes.AttendanceChoice.IN);

					}else if(y.getReactionEmote().getName().equals(":out:")){
						c.setAttendance(TournamentVotes.AttendanceChoice.OUT);
					}
					map.put(u.getName(), c);
				});
			});

			Message eatmsg = channel.getMessageById(x.getVotes().eatingMsgId).complete();

			eatmsg.getReactions().forEach(y -> {
				y.getUsers().complete().forEach(u -> {
					TournamentVotes.Choices c = new TournamentVotes.Choices();
					if(y.getReactionEmote().getName().equals(":meat:")){

						c.setEating(TournamentVotes.EatingChoice.MEAT);

					}else if(y.getReactionEmote().getName().equals(":veggie:")){
						c.setEating(TournamentVotes.EatingChoice.VEGGIE);
					}
					map.put(u.getName(), c);
				});
			});

			x.getVotes().attendance = map;

			event.getChannel().sendMessage("Restart Bot now").complete();

			System.err.println("Restart Bot now!!");

			event.getJDA().shutdownNow();

		});

	}

	@Blocking
	public void info(MessageReceivedEvent event, String[] msg){
		Tournament t = getTournament(msg, 3, event.getMessage());
		
		if(t != null){
			sendSync(event.getChannel(), t.getInfoMarkup());
		}else{
			MessageTimer.deleteAfter(sendSync(event.getChannel(), "Turnier konnte nicht gefunden werden!"), 15000);
		}
	}
	
	@Override
	public void help(MessageReceivedEvent event, String[] msg){

		int padding = 12;

		String info = "Mit \"" + this.cmd + "\" erstellst und verwaltest du Turniere\n"
				+ "Alle verfügbaren Optionen:\n";

		String commands = Utilities.padRight("help", padding) + "Ruft die Hilfe auf\n"
						+ Utilities.padRight("create", padding) + "Erstellt ein neues Turnier\n"
						+ Utilities.padRight("info [x]", padding) + "Infos zu aktuellen Turnier [x = Turniername]\n"
						+ Utilities.padRight("editinfo", padding) + "Ändert die Infos eines Turniers\n"
						+ Utilities.padRight("list", padding) + "Listet alle erstellten Turniere\n"
						+ Utilities.padRight("archive", padding) + "Archiviert ein Turnier\n";

		MessageBuilder messageBuilder = new MessageBuilder();
		messageBuilder.append(info);
		messageBuilder.appendCodeBlock(commands, "");

		send(event.getChannel(), messageBuilder.build());
		
		System.out.println("help");
	}
}