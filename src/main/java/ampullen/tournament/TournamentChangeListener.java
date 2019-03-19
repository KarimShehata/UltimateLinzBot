package ampullen.tournament;

import ampullen.jsondb.IObservable;
import ampullen.jsondb.Observer;
import ampullen.model.Tournament;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class TournamentChangeListener implements Observer {

    JDA jda;

    public TournamentChangeListener(JDA jda){
        this.jda = jda;
    }

    @Override
    public void update(IObservable observable) {
        if(observable instanceof Tournament){

            Tournament tournament = (Tournament) observable;
            TextChannel channel = jda.getTextChannelById(tournament.getAnnouncementChannel());

            if(channel != null){
                Message message = channel.getMessageById(tournament.getVotes().attendanceMsgId).complete();

                if(message != null){

                    message.editMessage(tournament.getInfoMarkup()).complete();

                }else{
                    System.err.println("TournamentChangeListenere - Message not found");
                    Thread.dumpStack();
                }

            }else{
                System.err.println("TournamentChangeListenere - Channel not found");
            }

        }
    }

}
