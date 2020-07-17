package botmanager.frostbalance.history;

import botmanager.Utilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RegimeData {

    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("MMMM dd");

    Guild guild;

    String userId;
    long startDay;
    long endDay;
    TerminationCondition terminationCondition;

    String lastKnownUserName;

    /**
     * Create a complete regime.
     * @param guild
     * @param userId
     * @param startDay
     * @param endDay
     * @param terminationCondition
     */
    public RegimeData(Guild guild, String userId, long startDay, long endDay, TerminationCondition terminationCondition, String lastKnownUserName) {
        this.guild = guild;
        this.userId = userId;
        this.terminationCondition = terminationCondition;
        this.startDay = startDay;
        this.endDay = endDay;
        this.lastKnownUserName = lastKnownUserName;
    }

    /**
     * Create a complete regime.
     * @param guild
     * @param userId
     * @param startDay
     * @param endDay
     * @param terminationCondition
     */
    public RegimeData(Guild guild, String userId, long startDay, long endDay, TerminationCondition terminationCondition) {
        this.guild = guild;
        this.userId = userId;
        this.terminationCondition = terminationCondition;
        this.startDay = startDay;
        this.endDay = endDay;
        getRulerDisplayName();
    }

    /**
     * Create a regime that has not yet ended.
     * @param guild
     * @param userId
     * @param startDay
     */
    public RegimeData(Guild guild, String userId, long startDay) {
        this(guild, userId, startDay, 0, TerminationCondition.NONE);
    }

    /**
     * Create a basic regime with only the user and their guild.
     * @param guild
     * @param userId
     */
    public RegimeData(Guild guild, String userId) {
        this(guild, userId, 0, 0, TerminationCondition.UNKNOWN);
    }

    public String getRulerDisplayName() {
        Member member = guild.getMemberById(userId);
        if (member != null) {
            lastKnownUserName = member.getEffectiveName();
        } else if (lastKnownUserName == null) { //no name yet, no accessible member
            User user = guild.getJDA().getUserById(userId);
            if (user != null) { //user exists, use that user's account name
                lastKnownUserName = user.getName();
            }
        }
        if (lastKnownUserName == null) {
            return "???";
        }
        return lastKnownUserName;
    }

    public String forHumans(JDA jda) {
        return jda.getUserById(userId).getName() + ": " +
                (startDay != 0 ? LocalDate.ofEpochDay(startDay).format(FORMAT) : "???") + " - " +
                (endDay != 0 ? LocalDate.ofEpochDay(endDay).format(FORMAT) + " (" : "(") +
                terminationCondition.name + ")";
    }

    @Override
    public String toString() {
        return toCSV();
    }

    public String toCSV() {
        return String.join(",", userId, lastKnownUserName, String.valueOf(startDay), String.valueOf(endDay), terminationCondition.name());
    }

    public void end(TerminationCondition condition) {
        endDay = Utilities.todayAsLong();
        terminationCondition = condition;
    }

    public String getUserId() {
        return userId;
    }

    public TerminationCondition getTerminationCondition() {
        return terminationCondition;
    }
}
