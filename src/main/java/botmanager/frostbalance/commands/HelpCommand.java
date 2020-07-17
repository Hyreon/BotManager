package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.frostbalance.generic.GenericMessageReceivedEventWrapper;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

/**
 *
 * @author MC_2018 <mc2018.git@gmail.com>
 */
public class HelpCommand extends FrostbalanceCommandBase {

    public HelpCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "help"
        });
    }
    
    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {
        String result = "__**Frostbalance**__\n\n";

        for (FrostbalanceCommandBase command : bot.getCommands()) {
            String info = command.publicInfo();

            if (!command.isAdminOnly() || command.wouldAuthorize(event.getGuild(), event.getAuthor())) { //hide admin commands
                if (info != null) {
                    result += info + "\n";
                }
            }

        }
        
        Utilities.sendGuildMessage(event.getChannel(), result);
    }

    @Override
    public void runPrivate(PrivateMessageReceivedEvent event, String message) {
        String result = "__**Frostbalance**__\n\n";

        for (FrostbalanceCommandBase command : bot.getCommands()) {
            String info = command.privateInfo();

            if (!command.isAdminOnly() || command.wouldAuthorize(new GenericMessageReceivedEventWrapper(bot, event).getGuild(), event.getAuthor())) { //hide admin commands
                if (info != null) {
                    result += info + "\n";
                }
            }
        }

        Utilities.sendPrivateMessage(event.getAuthor(), result);
    }

    @Override
    public String publicInfo() {
        return "**" + bot.getPrefix() + "help** in public chat - rattles off a bunch of useless public commands\n" +
                "**" + bot.getPrefix() + "help** in DM - rattles off a bunch of useless private commands";
    }

    @Override
    public String privateInfo() {
        return "**" + bot.getPrefix() + "help** in DM - rattles off a bunch of useless private commands\n" +
                "**" + bot.getPrefix() + "help** in public chat - rattles off a bunch of useless public commands";
    }

}
