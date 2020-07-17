package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.frostbalance.history.TerminationCondition;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CoupCommand extends FrostbalanceCommandBase {

    final String[] KEYWORDS = {
            bot.getPrefix() + "coup"
    };

    public CoupCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "coup"
        });
    }

    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {

        String id = event.getAuthor().getId();
        String result, privateResult;
        Member currentOwner;
        boolean found = false, success;

        currentOwner = bot.getOwner(event.getGuild());

        Member member = event.getGuild().getMemberById(id);

        if (!bot.hasBeenForciblyRemoved(member)) {
            result = "You have been recently removed by administrative action. Wait until someone else is leader.";
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        if (currentOwner == null) {
            result = "**" + event.getMember().getEffectiveName() + "** is the first player to declare themselves leader, " +
                    "and is now leader!";
            privateResult = null;
            success = true;
        } else {

            if (currentOwner.equals(member)) {
                result = "You realize that you're no match for yourself, and call it off.";
                Utilities.sendGuildMessage(event.getChannel(), result);
                return;
            }

            double influence = bot.getUserInfluence(member);
            double ownerInfluence = bot.getUserInfluence(currentOwner);

            if (influence > ownerInfluence) {
                bot.changeUserInfluence(member, -ownerInfluence);
                bot.changeUserInfluence(currentOwner, -ownerInfluence);
                result = "**" + event.getMember().getEffectiveName() + "** has successfully supplanted **" +
                        currentOwner.getAsMention() + "** as leader, reducing both users' influence and becoming" +
                        " the new leader!";
                privateResult = "*This maneuver has cost you " + String.format("%.3f", ownerInfluence) + " influence. " +
                        currentOwner.getEffectiveName() + " has lost **ALL** of their influence.*";
                success = true;
            } else {
                bot.changeUserInfluence(member, -influence);
                bot.changeUserInfluence(currentOwner, -influence);
                result = "**" + event.getMember().getEffectiveName() + "** has attempted a coup on **" +
                        currentOwner.getAsMention() + "**, which has backfired. Both players have lost influence" +
                        " and the leader has not changed.";
                privateResult = "*This maneuver has cost you **ALL** of your influence. " +
                        currentOwner.getEffectiveName() + " has lost " + String.format("%.3f", influence) + " of their influence.*";
                success = false;
            }

        }

        if (success) {
            bot.endRegime(event.getGuild(), TerminationCondition.COUP);
            bot.startRegime(event.getGuild(), member.getUser());
        }

        Utilities.sendGuildMessage(event.getChannel(), result);

        if (privateResult != null) {
            Utilities.sendPrivateMessage(event.getAuthor(), privateResult);
        }

    }

    @Override
    public String publicInfo() {
        return ""
                + "**" + bot.getPrefix() + "coup** - become server owner; this will drain both your influence and the influence " +
                "of the current owner until one (or both) of you run out. For ties, the existing owner is still owner.";
    }

    @Override
    public String privateInfo() {
        return null;
    }

}
