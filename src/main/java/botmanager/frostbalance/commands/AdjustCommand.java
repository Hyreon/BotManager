package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class AdjustCommand extends FrostbalanceCommandBase {

    public AdjustCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "adjust"
        }, true);
    }

    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {
        String[] words;
        String id;
        String name;
        double amount;
        words = message.split(" ");

        if (words.length < 2) {
            Utilities.sendGuildMessage(event.getChannel(), "Proper format: " + "**" + bot.getPrefix() + "adjust USER AMOUNT**");
            return;
        }

        try {
            amount = Double.parseDouble(words[words.length - 1]);

            if (amount == Double.NaN) {
                Utilities.sendGuildMessage(event.getChannel(), "NOPE.");
                return;
            }
        } catch (NumberFormatException e) {
            Utilities.sendGuildMessage(event.getChannel(), "Proper format: " + "**" + bot.getPrefix() + "adjust USER AMOUNT**");
            return;
        }

        name = Utilities.combineArrayStopAtIndex(words, words.length - 1);
        id = Utilities.findUserId(event.getGuild(), name);

        if (id == null) {
            Utilities.sendGuildMessage(event.getChannel(), "Couldn't find user '" + name + "'.");
            return;
        }
        bot.changeUserInfluence(event.getGuild().getMemberById(id), amount);

        event.getMessage().delete();

        Utilities.sendGuildMessage(event.getChannel(),
                event.getMember().getEffectiveName() + " has adjusted the influence of "
                        + event.getGuild().getMemberById(id).getEffectiveName()
                        + ".");

        if (amount != 0) {
            Utilities.sendPrivateMessage(event.getGuild().getMemberById(id).getUser(),
                    event.getMember().getEffectiveName() + " has adjusted your influence, changing it by " + String.format("%.3f", amount) + " in " +
                            event.getGuild().getName() + ".");
        }
    }

    @Override
    public void runPrivate(PrivateMessageReceivedEvent event, String message) {
        String[] words;
        String id;
        String name;
        String result;
        double amount;

        Guild guild = bot.getUserDefaultGuild(event.getAuthor());

        if (guild == null) {
            result = "You need to set a default guild to adjust influence.";
            Utilities.sendPrivateMessage(event.getAuthor(), result);
            return;
        }

        words = message.split(" ");

        if (words.length < 2) {
            Utilities.sendPrivateMessage(event.getAuthor(), "Proper format: " + "**" + bot.getPrefix() + "adjust USER AMOUNT**");
            return;
        }

        try {
            amount = Double.parseDouble(words[words.length - 1]);

            if (amount == Double.NaN) {
                Utilities.sendPrivateMessage(event.getAuthor(), "NOPE.");
                return;
            }
        } catch (NumberFormatException e) {
            Utilities.sendPrivateMessage(event.getAuthor(), "Proper format: " + "**" + bot.getPrefix() + "adjust USER AMOUNT**");
            return;
        }

        name = Utilities.combineArrayStopAtIndex(words, words.length - 1);
        id = Utilities.findUserId(guild, name);

        if (id == null) {
            Utilities.sendPrivateMessage(event.getAuthor(), "Couldn't find user '" + name + "'.");
            return;
        }

        bot.changeUserInfluence(guild.getMemberById(id), amount);

        Utilities.sendPrivateMessage(event.getAuthor(),
                "Your adjustment of "
                        + guild.getMemberById(id).getEffectiveName()
                        + " has been noted, giving them " + String.format("%.3f", amount) + " influence.");

        if (amount != 0) {
            Utilities.sendPrivateMessage(guild.getMemberById(id).getUser(),
                    guild.getMemberById(event.getAuthor().getId()).getEffectiveName() + " has adjusted your influence, changing it by " + String.format("%.3f", amount) + " in " +
                            guild.getName() + ".");
        }
    }

    @Override
    public String publicInfo() {
        return "**" + bot.getPrefix() + "adjust USER AMOUNT** - changes influence of someone else (don't @ them) in this server";
    }

    @Override
    public String privateInfo() {
        return "**" + bot.getPrefix() + "adjust USER AMOUNT** - changes influence of someone else (don't @ them) in your default server";
    }

}

