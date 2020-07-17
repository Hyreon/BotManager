package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

/**
 *
 * @author MC_2018 <mc2018.git@gmail.com>
 */
public class SupportCommand extends FrostbalanceCommandBase {

    private static final double PRIVATE_RATE = 0.5;

    public SupportCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "support"
        });
    }
    
    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {
        String[] words;
        String id;
        String name;
        double balance, amount;

        balance = bot.getUserInfluence(event.getMember());
        
        words = message.split(" ");
        
        if (words.length < 2) {
            Utilities.sendGuildMessage(event.getChannel(), "Proper format: " + "**" + bot.getPrefix() + "support USER AMOUNT**");
            return;
        }
        
        try {
            amount = Double.parseDouble(words[words.length - 1]);
            
            if (balance < amount) {
                Utilities.sendGuildMessage(event.getChannel(), "You can't offer that much support. You will instead offer all of your support.");
                amount = balance;
            } else if (amount <= 0) {
                Utilities.sendGuildMessage(event.getChannel(), "You have to give *some* support if you're running this command.");
                return;
            } else if (amount == Double.NaN) {
                Utilities.sendGuildMessage(event.getChannel(), "NOPE.");
                return;
            }
        } catch (NumberFormatException e) {
            Utilities.sendGuildMessage(event.getChannel(), "Proper format: " + "**" + bot.getPrefix() + "support USER AMOUNT**");
            return;
        }
        
        name = Utilities.combineArrayStopAtIndex(words, words.length - 1);
        id = Utilities.findUserId(event.getGuild(), name);
        
        if (id == null) {
            Utilities.sendGuildMessage(event.getChannel(), "Couldn't find user '" + name + "'.");
            return;
        }
        bot.changeUserInfluence(event.getMember(), -amount);
        bot.changeUserInfluence(event.getGuild().getMemberById(id), amount);

        event.getMessage().delete();
        
        Utilities.sendGuildMessage(event.getChannel(),
                event.getMember().getEffectiveName() + " has supported "
                + event.getGuild().getMemberById(id).getEffectiveName()
                + ", giving them some influence.");

        if (amount != 0) {
            Utilities.sendPrivateMessage(event.getGuild().getMemberById(id).getUser(),
                    event.getMember().getEffectiveName() + " has supported you, giving you " + String.format("%.3f", amount) + " influence in " +
                            event.getGuild().getName() + ".");
        }
    }

    @Override
    public void runPrivate(PrivateMessageReceivedEvent event, String message) {
        String[] words;
        String id;
        String name;
        String result;
        double balance, amount;

        Guild guild = bot.getUserDefaultGuild(event.getAuthor());

        if (guild == null) {
            result = "You need to set a default guild to transfer influence.";
            Utilities.sendPrivateMessage(event.getAuthor(), result);
            return;
        }

        balance = bot.getUserInfluence(bot.getUserDefaultGuild(event.getAuthor()), event.getAuthor());

        words = message.split(" ");

        if (words.length < 2) {
            Utilities.sendPrivateMessage(event.getAuthor(), "Proper format: " + "**" + bot.getPrefix() + "support USER AMOUNT**");
            return;
        }

        try {
            amount = Double.parseDouble(words[words.length - 1]);

            if (balance < amount) {
                Utilities.sendPrivateMessage(event.getAuthor(), "You can't offer that much support. You will instead offer all of your support.");
                amount = balance;
            } else if (amount <= 0) {
                Utilities.sendPrivateMessage(event.getAuthor(), "You have to give *some* support if you're running this command.");
                return;
            } else if (amount == Double.NaN) {
                Utilities.sendPrivateMessage(event.getAuthor(), "NOPE.");
                return;
            }
        } catch (NumberFormatException e) {
            Utilities.sendPrivateMessage(event.getAuthor(), "Proper format: " + "**" + bot.getPrefix() + "support USER AMOUNT**");
            return;
        }

        name = Utilities.combineArrayStopAtIndex(words, words.length - 1);
        id = Utilities.findUserId(guild, name);

        if (id == null) {
            Utilities.sendPrivateMessage(event.getAuthor(), "Couldn't find user '" + name + "'.");
            return;
        }

        bot.changeUserInfluence(guild, event.getAuthor(), -amount);
        bot.changeUserInfluence(guild.getMemberById(id), amount * PRIVATE_RATE);

        Utilities.sendPrivateMessage(event.getAuthor(),
                "Your private support of "
                        + guild.getMemberById(id).getEffectiveName()
                        + " has been noted, giving them half of that influence. (" +
                        String.format("%.3f", amount * PRIVATE_RATE) + ", -" +
                        String.format("%.3f", amount) + ")");

        if (amount != 0) {
            Utilities.sendPrivateMessage(guild.getMemberById(id).getUser(),
                    guild.getMemberById(event.getAuthor().getId()).getEffectiveName() + " has supported you, giving you " + String.format("%.3f", amount * PRIVATE_RATE) + " influence in " +
                            guild.getName() + ".");
        }
    }

    @Override
    public String publicInfo() {
        return "**" + bot.getPrefix() + "support USER AMOUNT** - gives your influence to someone else (don't @ them)";
    }

    @Override
    public String privateInfo() {
        return "**" + bot.getPrefix() + "support USER AMOUNT** - gives your influence to someone else (don't @ them); 50% of what you send is lost";
    }

}
