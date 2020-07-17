package botmanager.frostbalance.commands;

import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ImplicitInfluence extends FrostbalanceCommandBase {

    SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yyyy hh:mm");
    ArrayList<Member> minuteMembers;
    String cachedDate;

    public ImplicitInfluence(BotBase bot) {
        super(bot);
        minuteMembers = new ArrayList<>();
        cachedDate = sdf.format(new Date());
    }

    @Override
    public void run(Event genericEvent) {
        GuildMessageReceivedEvent event;
        Guild guild;
        Member member;
        String date = sdf.format(new Date());

        if (!(genericEvent instanceof GuildMessageReceivedEvent)) {
            return;
        }

        event = (GuildMessageReceivedEvent) genericEvent;
        guild = event.getGuild();
        member = event.getMember();

        if (!this.cachedDate.equals(date)) {
            minuteMembers = new ArrayList<>();
            this.cachedDate = date;
        }

        for (Member minuteMember : minuteMembers) {
            if (guild.getId().equals(minuteMember.getGuild().getId()) &&
                    member.getId().equals(minuteMember.getId())) {
                return;
            }
        }

        bot.gainDailyInfluence(event.getMember(), 0.05);
        minuteMembers.add(member);
    }

    @Override
    public String publicInfo() {
        return "type in chat to gain influence gradually (0.05 per minute with a message); this is capped to 1.00 and does not stack with `.daily`.";
    }

    @Override
    public String privateInfo() {
        return null;
    }

}
