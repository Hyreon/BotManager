package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

//TODO
public class FlagCommand extends FrostbalanceCommandBase {


    public FlagCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "flag"
        });
    }

    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {
        String id = event.getAuthor().getId();
        String result = "";
        boolean found = false;

        result += "This server has no flags, " + event.getMember().getEffectiveName() + "!";

        Utilities.sendGuildMessage(event.getChannel(), result);

    }

    @Override
    public String publicInfo() {
        return "**" + bot.getPrefix() + "flag FLAG** - apply a flag to this server, if you are staff\n" +
                "**" + bot.getPrefix() + "flag** - view all flags for this server - any user";
    }

    @Override
    public String privateInfo() {
        return "**" + bot.getPrefix() + "flag FLAG** - apply a flag to this server, if you are staff\n" +
                "**" + bot.getPrefix() + "flag** - view all flags for this server - any user";
    }

}
