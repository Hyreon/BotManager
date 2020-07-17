package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.frostbalance.history.TerminationCondition;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;

public class TransferCommand extends FrostbalanceCommandBase {

    public TransferCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "transfer"
        });
    }

    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {

        String targetId;
        String result;
        Member authorAsMember;
        Member currentOwner;

        try {
            String info = Utilities.read(new File("data/" + bot.getName() + "/" + event.getGuild().getId() + "/owner.csv"));
            currentOwner = event.getGuild().getMember(event.getJDA().getUserById(Utilities.getCSVValueAtIndex(info, 0)));
        } catch (NullPointerException | IllegalArgumentException e) {
            currentOwner = null;
        }

        authorAsMember = event.getMember();

        if (!authorAsMember.equals(currentOwner)) {
            result = "You have to be the owner to transfer ownership of the server peaceably.";

            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        if (message.isEmpty()) {
            result = publicInfo();
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        targetId = Utilities.findUserId(event.getGuild(), message);

        if (targetId == null) {
            result = "Couldn't find user '" + message + "'.";
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        if (event.getGuild().getMemberById(targetId).getRoles().contains(bot.getSystemRole(event.getGuild()))) {
            if (targetId.equals(event.getJDA().getSelfUser().getId())) {
                result = "A very generous offer, but I can't accept.";
            } else {
                result = "Staff members are prohibited from getting server ownership through transfer.";
            }
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        User newOwner = event.getJDA().getUserById(targetId);

        bot.endRegime(event.getGuild(), TerminationCondition.TRANSFER);
        bot.startRegime(event.getGuild(), newOwner);

        result = "**" + currentOwner.getEffectiveName() + "** has transferred ownership to " +
                newOwner.getAsMention() + " for this server.";
        Utilities.sendGuildMessage(event.getChannel(), result);

    }

    @Override
    public String publicInfo() {
        return ""
                + "**" + bot.getPrefix() + "transfer USER** - makes someone else server owner.";
    }

    @Override
    public String privateInfo() {
        return null;
    }

}
