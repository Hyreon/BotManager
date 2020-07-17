package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GlobalBanCommand extends FrostbalanceCommandBase {

    public GlobalBanCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "ban"
        }, true);
    }

    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {
        String result = "";
        boolean found = false;

        String targetId = Utilities.findUserId(event.getGuild(), message);

        if (targetId == null) {
            result += "Could not find user " + message + ".";
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        User targetUser = event.getJDA().getUserById(targetId);

        if (targetUser == null) {
            result += "Could not find user " + message + ".";
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        bot.globallyBanUser(targetUser);

        result += "Removed player '" + targetUser.getAsMention() + "' from all servers. They will not return until pardoned.";

        Utilities.sendGuildMessage(event.getChannel(), result);

    }

    @Override
    public String publicInfo() {
        return "**" + bot.getPrefix() + "ban PLAYER** - bans a player across all servers.";
    }

    @Override
    public String privateInfo() {
        return null;
    }

}
