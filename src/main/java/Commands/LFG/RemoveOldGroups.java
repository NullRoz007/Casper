package Commands.LFG;

import Core.PermissionHandler;
import Commands.AbstractCommand;
import Commands.CommandCategory;
import Exceptions.InvalidPermissionsException;
import LFG.Group;
import LFG.LFGHandler;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RemoveOldGroups extends AbstractCommand {

    private static String command = "removeoldgroups";
    private static String desc = "temp";
    private static String[] inputs = {};

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
        return CommandCategory.GENERAL;
    }

    public void run(Message msg) throws InvalidPermissionsException {

        PermissionHandler.checkModPermissions(msg.getMember());

        String response = "";

        ArrayList<Group> toRemove = new ArrayList<Group>();

        for(int i = 0; i < LFGHandler.getGroups().size(); i++){
            Group g = LFGHandler.getGroups().get(i);
            long diff = LFGHandler.getDateDiff(new Date(), g.getDate(), TimeUnit.MINUTES);
            if(diff <= 0){
                response = response + g.getID() + " ";
                toRemove.add(g);
            }
        }

        // For safe removal
        for(int i = 0; i < toRemove.size(); i++){
            LFGHandler.getGroups().remove(toRemove.get(i));
        }

        if(response.equals("")){
            response = "No groups to remove";
        } else {
            response = "Removed groups: " + response;
        }
        msg.getChannel().sendMessage(response).queue();
    }
}
