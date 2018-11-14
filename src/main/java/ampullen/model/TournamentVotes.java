package ampullen.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import ampullen.helper.EmoteLimiter.EmoteListener;
import ampullen.helper.PersistentMessage;
import ampullen.jsondb.IObservable;
import ampullen.jsondb.Observable;
import ampullen.jsondb.Observer;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;

public class TournamentVotes extends Observable implements Observer{
	
	Map<String, Choices> attendance = new HashMap<>();
	
	public transient PersistentMessage attendanceMsg;
	public transient PersistentMessage eatingMsg;
	public long attendanceMsgId;
	public long eatingMsgId;
	
	public TournamentVotes() {
		
	}
	
	public Choices getChoices(String user) {
		return attendance.get(user);
	}
	
	public void addChoices(String user, Choices c) {
		if(!attendance.containsKey(user)) {
			attendance.put(user, c);
		}else {
			new OperationNotSupportedException("Choice already set").printStackTrace();
		}
	}
	
	public void setAttendanceMsg(Message m) {
		PersistentMessage info = new PersistentMessage(m,
				Arrays.asList("in", "50", "out"),
				true);
		
		attendanceMsgId = m.getIdLong();
		info.init();
		
		final TournamentVotes votes = this;
		
		info.limiter.addEmoteListener(new EmoteListener() {
			
			@Override
			public void emoteRemove(MessageReactionAddEvent e, MessageReaction r) {
				
				/*String uname = e.getUser().getName();
				String emote = e.getReactionEmote().getEmote().getName();
				AttendanceChoice c = AttendanceChoice.fromEmoteString(emote);
				if(attendance.containsKey(uname)) {
					Choices choices = attendance.get(uname);
					if(choices.getAttendance().equals(c)) {
						//choices.
						//TODO Removed sinnvoll, vllt nur ändern möglioch amcehn
					}
				}else {
					System.out.println("Not existent Emote removed... Was the Bot down?");
				}*/
			}
			
			@Override
			public void emoteAdd(MessageReactionAddEvent e) {
				String uname = e.getUser().getName();
				String emote = e.getReactionEmote().getEmote().getName();
				System.out.println(emote);
				AttendanceChoice c = AttendanceChoice.fromEmoteString(emote);
				Choices choices;
				if(attendance.containsKey(uname)) {
					choices = attendance.get(uname);
				}else {
					choices = new Choices();
					choices.addObserver(votes);
					attendance.put(uname, choices);
				}
				choices.setAttendance(c);
			}
		});
		notifyObservers();
	}
	
	public void setEatingMsg(Message m) {
		PersistentMessage v = new PersistentMessage(m,
				Arrays.asList("meat", "veggie"),
				true);
		
		eatingMsgId = m.getIdLong();
		v.init();
		
		TournamentVotes votes = this;
		
		v.limiter.addEmoteListener(new EmoteListener() {
			
			@Override
			public void emoteRemove(MessageReactionAddEvent e, MessageReaction r) {
				
			}
			
			@Override
			public void emoteAdd(MessageReactionAddEvent e) {
				
				String uname = e.getUser().getName();
				String emote = e.getReactionEmote().getEmote().getName();
				System.out.println(emote);
				EatingChoice c = EatingChoice.fromEmoteString(emote);
				Choices choices;
				if(attendance.containsKey(uname)) {
					choices = attendance.get(uname);
				}else {
					choices = new Choices();
					choices.addObserver(votes);
					attendance.put(uname, choices);
				}
				choices.setEating(c);
				
			}
		});
		
		notifyObservers();
	}
	
	@Override
	public void addObserver(Observer o) {
		super.addObserver(o);
		attendance.values().forEach(x -> x.addObserver(o));
	}

	@Override
	public void update(IObservable observable) {
		this.notifyObservers();
	}
	
	public Map<String, Choices> getAttendance() {
		return attendance;
	}

	public void setAttendance(Map<String, Choices> attendance) {
		this.attendance = attendance;
	}
	
	
	
	public static class Choices extends Observable{
		
		AttendanceChoice a;
		EatingChoice e;
		
		public AttendanceChoice getAttendance() {
			return a;
		}
		public void setAttendance(AttendanceChoice a) {
			this.a = a;
			notifyObservers();
		}
		public EatingChoice getEating() {
			return e;
		}
		public void setEating(EatingChoice e) {
			this.e = e;
			notifyObservers();
		}
		
		
	}
	
	public static enum AttendanceChoice{
		IN, MAYBE, OUT, NOTSET;
		
		public static AttendanceChoice fromEmoteString(String name) {
			AttendanceChoice c = AttendanceChoice.NOTSET;
			switch(name.toLowerCase()) {
				case "in":
					c = AttendanceChoice.IN; break;
				case "out":
					c = AttendanceChoice.OUT; break;
				case "50":
					c = AttendanceChoice.MAYBE; break;
			}
			return c;
		}
		
	}
	
	public static enum EatingChoice{
		MEAT, VEGGIE, NOTSET;

		public static EatingChoice fromEmoteString(String emote) {
			EatingChoice e = EatingChoice.NOTSET;
			switch(emote.toLowerCase()) {
				case "meat":
					e = EatingChoice.MEAT; break;
				case "veggie":
					e = EatingChoice.VEGGIE; break;
			}
			return e;
		}
	}
	
}
