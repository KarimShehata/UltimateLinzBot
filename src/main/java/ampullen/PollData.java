package ampullen;

import java.util.ArrayList;

public class PollData {
    public String MessageId;
    public String Name;
    public PollType Type;
    public ArrayList<VoteOption> VoteOptions;

    public PollData() {
        VoteOptions = new ArrayList<>();
    }
}
