package botmanager.frostbalance.commands;

import botmanager.frostbalance.generic.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.generic.GenericMessageReceivedEventWrapper;
import botmanager.frostbalance.history.RegimeData;
import botmanager.generic.BotBase;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryCommand extends FrostbalanceHybridCommandBase {

    private static final int HISTORY_PAGE_SIZE = 6;

    public HistoryCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "history"
        }, false);
    }

    @Override
    public void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String message) {
        String[] words;
        String result = "";
        int page = 1;
        List<RegimeData> records;

        words = message.split(" ");

        if (eventWrapper.getGuild() == null) {
            result += "You need to have a default server to do that.";
            eventWrapper.sendResponse(result);
            return;
        }

        if (words.length >= 1 && !message.isEmpty()) {
            try {
                page = Integer.parseInt(words[0]);
            } catch (NumberFormatException e) {
                result += "Couldn't recognize the number '" + words[0] + "'.";

                eventWrapper.sendResponse(result);

                return;
            }
        }

        if (page < 0) {
            result += "This number is too low, it must be at least 1.";

            eventWrapper.sendResponse(result);

            return;
        }

        records = bot.readRecords(eventWrapper.getGuild());
        Collections.reverse(records);

        if (page > maxPages(records)) {

            if (page == 1) {
                result += "**This server has no history. This is likely an error, and you should inform staff.**";
            } else {
                result += "This number is too high, it must be at most " + maxPages(records) + ".";
            }

            eventWrapper.sendResponse(result);
            return;
        }

        result += displayRecords(records, page);

        eventWrapper.sendResponse(result);

    }

    @Override
    public String publicInfo() {
        return "**" + bot.getPrefix() + "history PAGE** - find out about previous regimes.";
    }

    private String displayRecords(List<RegimeData> records, int page) {

        List<RegimeData> sublist = records.subList((page - 1) * HISTORY_PAGE_SIZE, Math.min(page * HISTORY_PAGE_SIZE, records.size()));
        List<String> displayList = sublist.stream().map(regimeData -> regimeData.forHumans(bot.getJDA())).collect(Collectors.toList());

        return String.join("\n", displayList)
                + "\nPage " + page + "/" + maxPages(records);

    }

    private int maxPages(List<?> list) {
        return (int) Math.ceil(list.size() / (double) HISTORY_PAGE_SIZE);
    }

}
