package ampullen.tournament;

import ampullen.helper.AsciiTable;
import ampullen.helper.AsciiTable.ColumnDefinition;
import ampullen.jsondb.IObservable;
import ampullen.jsondb.Observer;
import ampullen.model.TournamentVotes;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

public class AsciiMessageObserver implements Observer {

    AsciiTable<String> table;

    public AsciiMessageObserver(TournamentVotes votes, Message message){

        table = new AsciiTable<String>(message, columns());

    }

    private ColumnDefinition<String> columns() {

        return new ColumnDefinition<String>()
                .addPrimaryColumn("Name")
                .addColumn("In", x -> x.equalsIgnoreCase("in"))
                .addColumn("Out", x -> x.equalsIgnoreCase("out"));

    }

    public void answerChanged(String name, String choice){

        table.data(name, choice);

        editMessage();

    }

    public void editMessage(){

        Message newmessage = new MessageBuilder()
                .appendCodeBlock(table.renderAscii(), "")
                .build();

        table.getTableMessage().editMessage(newmessage).queue();
    }


    @Override
    public void update(IObservable observable) {

        if(observable instanceof TournamentVotes){

            TournamentVotes votes = (TournamentVotes)observable;

//            votes.getAttendance().forEach((s, choices) -> );

        }

    }
}
