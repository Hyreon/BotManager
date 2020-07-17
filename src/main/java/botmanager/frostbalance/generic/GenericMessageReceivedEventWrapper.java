package botmanager.frostbalance.generic;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class GenericMessageReceivedEventWrapper {

    Frostbalance bot;

    PrivateMessageReceivedEvent privateEvent;
    GuildMessageReceivedEvent publicEvent;

    public GenericMessageReceivedEventWrapper(Frostbalance bot, PrivateMessageReceivedEvent privateEvent) {
        this.bot = bot;
        this.privateEvent = privateEvent;
    }

    public GenericMessageReceivedEventWrapper(Frostbalance bot, GuildMessageReceivedEvent publicEvent) {
        this.bot = bot;
        this.publicEvent = publicEvent;
    }

    private boolean isPublic() {
        if (publicEvent != null) return true;
        if (privateEvent != null) return false;
        throw new IllegalStateException("GenericMessageReceivedEventWrapper is neither public nor private, as no valid event was found!");
    }

    public JDA getJDA() {
        if (isPublic()) {
            return publicEvent.getJDA();
        }
        return privateEvent.getJDA();
    }

    public Guild getGuild() {
        if (isPublic()) {
            return publicEvent.getGuild();
        }
        return bot.getUserDefaultGuild(getAuthor());
    }

    public User getAuthor() {
        if (isPublic()) {
            return publicEvent.getAuthor();
        }
        return privateEvent.getAuthor();
    }

    public Member getMember() {
        if (isPublic()) {
            return publicEvent.getMember();
        }
        return getGuild().getMember(getAuthor());
    }

    public Message getMessage() {
        if (isPublic()) {
            return publicEvent.getMessage();
        }
        return privateEvent.getMessage();
    }

    public MessageChannel getChannel() {
        if (isPublic()) {
            return publicEvent.getChannel();
        }
        return privateEvent.getChannel();
    }

    public void sendResponse(String message) {
        if (isPublic()) {
            Utilities.sendGuildMessage((TextChannel) getChannel(), message);
        } else {
            Utilities.sendPrivateMessage(getAuthor(), message);
        }
    }



}
