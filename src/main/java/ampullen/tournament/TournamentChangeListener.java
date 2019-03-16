package ampullen.tournament;

import ampullen.jsondb.IObservable;
import ampullen.jsondb.Observer;
import ampullen.model.Tournament;
import net.dv8tion.jda.core.JDA;

public class TournamentChangeListener implements Observer {

    JDA jda;

    TournamentChangeListener(JDA jda){
        this.jda = jda;
    }

    @Override
    public void update(IObservable observable) {
        if(observable instanceof Tournament){

            Tournament tournament = (Tournament) observable;
            jda.getTextChannelById(tournament.getAnnouncementChannel()).getMessageById(tournament.getVotes().attendanceMsgId).complete()
                    .editMessage(tournament.getInfoMarkup()).complete();

        }
    }

}
