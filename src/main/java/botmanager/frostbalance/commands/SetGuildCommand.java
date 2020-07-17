package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import java.util.List;

public class SetGuildCommand extends FrostbalanceCommandBase {

    public SetGuildCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "guild"
        });
    }

    @Override
    public void runPrivate(PrivateMessageReceivedEvent event, String message) {

        String[] words;
        String name;
        String result = "";

        words = message.split(" ");

        if (message.isEmpty()) {
            bot.resetUserDefaultGuild(event.getAuthor());

            result += "Removed default guild.";
            Utilities.sendPrivateMessage(event.getAuthor(), result);
            return;
        }

        name = Utilities.combineArrayStopAtIndex(words, words.length);
        List<Guild> guilds = event.getJDA().getGuildsByName(name, true);

        if (guilds.isEmpty()) {
            Utilities.sendPrivateMessage(event.getAuthor(), "Couldn't find guild '" + name + "'.");
            return;
        }

        bot.setUserDefaultGuild(event.getAuthor(), guilds.get(0));

        result += "Set default guild to **" + guilds.get(0).getName() + "**.";

        Utilities.sendPrivateMessage(event.getAuthor(), result);

    }

    @Override
    public String publicInfo() {
        return null;
    }

    @Override
    public String privateInfo() {
        return "**" + bot.getPrefix() + "guild GUILDNAME** - sets your default guild when running commands through PM." + "\n" +
                "**" + bot.getPrefix() + "guild** - resets your default guild.";
    }

}
