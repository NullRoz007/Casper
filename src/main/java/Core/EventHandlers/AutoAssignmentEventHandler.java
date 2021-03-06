package Core.EventHandlers;

import Core.Bot;
import Core.PropertyKeys;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class AutoAssignmentEventHandler implements net.dv8tion.jda.core.hooks.EventListener {

    private static String ROLE_DELIMITER = "%";
    private static String SERVER_NAME = Bot.props.getProperty(PropertyKeys.SERVER_NAME_KEY);
    private static String ASSIGNMENT_CHANNEL = Bot.props.getProperty(PropertyKeys.ROLE_ASSIGNMENT_CHANNEL_KEY);
    private static String MONITORED_REACTION = Bot.props.getProperty(PropertyKeys.MONITORED_REACTION_KEY); //Unicode, this may not work after the props file has been re-saved
    private static List<String> ROLES = new ArrayList<String>();
    private static Map<String, String> MSG_KEYS = new HashMap<String, String>(); // Maps MSG ID to Role String

    @Override
    public void onEvent(Event e){
        try {
            // Ignore reactions from self
            String id = "";
            if(e instanceof GenericMessageReactionEvent){
                id = ((GenericMessageReactionEvent) e).getUser().getId();
            }

            if(id.equals(Bot.SELF_USER_ID)){
                // Do nothing
            } else if(e instanceof MessageReactionAddEvent){
                onMessageReactionAddEvent((MessageReactionAddEvent) e);
            } else if(e instanceof MessageReactionRemoveEvent){
                onMessageReactionRemoveEvent((MessageReactionRemoveEvent) e);
            }
        } catch (Exception err){
            System.out.println(err.getMessage());
        }
    }

    public void onMessageReactionAddEvent(MessageReactionAddEvent e){
        if(MSG_KEYS.containsKey(e.getMessageId())){
            Role newRole = e.getGuild().getRolesByName(MSG_KEYS.get(e.getMessageId()), true).get(0);
            e.getGuild().getController().addSingleRoleToMember(e.getMember(), newRole).complete();
        }
    }

    public void onMessageReactionRemoveEvent(MessageReactionRemoveEvent e){
        if(MSG_KEYS.containsKey(e.getMessageId())){
            Role roleToRemove = e.getGuild().getRolesByName(MSG_KEYS.get(e.getMessageId()), true).get(0);
            e.getGuild().getController().removeSingleRoleFromMember(e.getMember(), roleToRemove).complete();
        }
    }

    public static void load(JDA bot){
        Guild guild = bot.getGuildsByName(SERVER_NAME, false).get(0);
        TextChannel channel = guild.getTextChannelsByName(ASSIGNMENT_CHANNEL, false).get(0);

        loadRoles();
        clearChannel(channel);

        // Post Messages to channel
        for(int i = 0; i < ROLES.size(); i++){
            channel.sendMessage("React to add role: " + ROLES.get(i)).queue();
        }

        // Add base reactions to messages
        Emote reaction = getEmote(bot);
        List<Message> messages = channel.getHistory().retrievePast(ROLES.size()).complete();
        for(int i = 0; i < messages.size(); i++){
            messages.get(i).addReaction(reaction).queue();
            MSG_KEYS.put(messages.get(i).getId(), ROLES.get(ROLES.size()-1-i));
        }

    }

    public static void loadRoles(){
//        ROLES.add("Escalation Protocol");
//        ROLES.add("PS4-Raider");
//        ROLES.add("PC-Raider");

        ROLES = new ArrayList<String>();

        String[] temp = Bot.props.getProperty(PropertyKeys.AUTO_ROLES_KEY).split(ROLE_DELIMITER);

        for(int i = 0; i < temp.length; i++){
            ROLES.add(temp[i]);
        }
    }

    public static void saveRoles(JDA bot){
        String result = "";

        for(int i = 0; i < ROLES.size(); i++){
            result = result + ROLES.get(i);

            if(i+1 < ROLES.size()){
                result = result + ROLE_DELIMITER;
            }
        }
        Bot.props.setProperty(PropertyKeys.AUTO_ROLES_KEY, result);

        try {
            Bot.props.store(new FileOutputStream("bot.properties"), null);
            load(bot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void clearChannel(TextChannel channel){
        List<Message> messages = channel.getHistory().retrievePast(50).complete();

        for(int i = 0; i < messages.size(); i++){
            messages.get(i).delete().queue();
        }
    }

    public static List<String> getRoles(){
        return ROLES;
    }

    public static Emote getEmote(JDA bot){
        //TODO
        Guild server = bot.getGuildsByName(Bot.props.getProperty(PropertyKeys.SERVER_NAME_KEY), false).get(0);
        Emote emote = server.getEmotesByName(Bot.props.getProperty(PropertyKeys.MONITORED_REACTION_KEY), true).get(0);
        return emote;
    }

}