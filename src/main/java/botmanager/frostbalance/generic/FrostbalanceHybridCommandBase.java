package botmanager.frostbalance.generic;

import botmanager.Utilities;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public abstract class FrostbalanceHybridCommandBase extends FrostbalanceCommandBase {

    public FrostbalanceHybridCommandBase(BotBase bot, String[] keywords, boolean adminOnly) {
        super(bot, keywords, adminOnly);
    }

    @Override
    public void run(Event genericEvent) {
        String message;
        boolean found = false;

        if (genericEvent instanceof GuildMessageReceivedEvent) {
            message = ((GuildMessageReceivedEvent)genericEvent).getMessage().getContentRaw();
        } else if (genericEvent instanceof PrivateMessageReceivedEvent) {
            message = ((PrivateMessageReceivedEvent)genericEvent).getMessage().getContentRaw();
        } else {
            return;
        }

        for (String keyword : KEYWORDS) {
            if (message.equalsIgnoreCase(keyword)) {
                message = message.replace(keyword, "");
                found = true;
                break;
            } else if (message.startsWith(keyword + " ")) {
                message = message.replace(keyword + " ", "");
                found = true;
                break;
            }
        }

        if (!found) {
            return;
        }

        if (genericEvent instanceof GuildMessageReceivedEvent) {
            GuildMessageReceivedEvent event = (GuildMessageReceivedEvent) genericEvent;
            if (ADMIN_ONLY && !event.getMember().getRoles().contains(bot.getSystemRole(event.getGuild()))) {
                Utilities.sendGuildMessage(event.getChannel(), "You must be a system administrator here to do this.");
                return;
            }
            runHybrid(new GenericMessageReceivedEventWrapper(bot, event), message);
        } else if (genericEvent instanceof PrivateMessageReceivedEvent) {
            PrivateMessageReceivedEvent event = (PrivateMessageReceivedEvent) genericEvent;
            if (ADMIN_ONLY && bot.hasSystemRoleEverywhere(event.getAuthor())) {
                Utilities.sendPrivateMessage(event.getAuthor(), "You must be a system administrator in all servers to do this.");
                return;
            }
            runHybrid(new GenericMessageReceivedEventWrapper(bot, event), message);
        }
    }

    protected abstract void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String message);

    /**
     * A default override for hybrid commands; rather than defaulting to 'null', it will default to returning the public info.
     * @return The public information for this function.
     */
    @Override
    public String privateInfo() {
        return publicInfo();
    }

}
