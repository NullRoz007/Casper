package Commands.LFG;

import Core.Bot;
import Core.PropertyKeys;
import Commands.AbstractCommand;
import Exceptions.NoArgumentsGivenException;
import LFG.Group;
import LFG.LFGHandler;
import net.dv8tion.jda.core.entities.Message;

import java.text.ParseException;
import java.util.ArrayList;

public class Post extends AbstractCommand {

    private static String command = "post";
    private static String desc = "temp";
    private static String[] inputs = {"name", "date(MM/dd)", "time", "timezone"};

    @Override
    public String[] getInputs() {
        return inputs;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public String getDescription() {
        return desc;
    }

    @Override
    public int getCategory() {
        return 0;
    }

    @Override
    public String[] getInputArgs(Message msg){
        ArrayList<String> temp = new ArrayList<String>();

        // Removes delimiter and splits input
        String[] args1 = msg.getContentRaw().replaceFirst(Bot.props.getProperty(PropertyKeys.DELIMITER_KEY), "").split("\"");
        temp.add(args1[1]);

        String[] args2 = args1[2].trim().split(" ");

        for(int i = 0; i < args2.length; i++){
            temp.add(args2[i]);
        }

        String[] result = new String[temp.size()];
        temp.toArray(result);
        return result;
    }

    @Override
    public void run(Message msg) throws NoArgumentsGivenException {
        String response = "";
        try {
            String[] args;

            // Give input the best chance at passing
            if(msg.getContentRaw().contains("\"")){
                args = getInputArgs(msg);
            } else {
                args = super.getInputArgs(msg);
            }

            Group g = LFGHandler.post(args[0], args[1], args[2], args[3], msg.getMember());
            response = g.toString();
        } catch (ParseException e){
            e.printStackTrace();
            response = "Unable to parse date/time. Required format:"
                    + "```MM/dd hh:mmaa zzz```"
                    + "M - Month\n"
                    + "d - Day\n"
                    + "h - Hour\n"
                    + "m - Minute\n"
                    + "a - AM/PM\n"
                    + "z - Timezone\n";
        }
        msg.getChannel().sendMessage(response).queue();
    }
}
