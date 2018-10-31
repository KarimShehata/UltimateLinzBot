package ampullen;

import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.EventListener;

public class TestListener implements EventListener {

    @Override
    public void onEvent(Event event)
    {
        System.out.println(event.getClass().getName());
    }

}
