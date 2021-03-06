package botmanager.maidiscordbot;

import botmanager.maidiscordbot.commands.*;
import botmanager.generic.BotBase;
import botmanager.Utilities;
import java.io.File;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import botmanager.generic.ICommand;
import botmanager.maidiscordbot.generic.MaiDiscordBotCommandBase;

//idea: encrypter(s) built in?
/**
 *
 * @author MC_2018 <mc2018.git@gmail.com>
 */
public class MaiDiscordBot extends BotBase {

    TimerTask timerTask;
    Timer timer = new Timer();
	
    private HashMap<Guild, Boolean> harvesting = new HashMap<>();
    private static final int PLANT_GROWTH_MAX = 500000;
    public Set<Member> planters = new HashSet<>();
    
    public MaiDiscordBot(String botToken, String name) {
        super(botToken, name);
        getJDA().getPresence().setActivity(Activity.watching(" you lose money :)"));
        setPrefix("~");
        generatePlantTimer();
        setCommands(new ICommand[] {
            new MoneyCommand(this),
            new HelpCommand(this),
            new BalanceCommand(this),
            new GiveCommand(this),
            new BalanceTopCommand(this),
            new DailyRewardCommand(this),
            new GambleCommand(this),
            new CoinflipCommand(this),
            new JackpotCommand(this),
            new DeadCommand(this),
            new PMRepeaterCommand(this),
            new AlltimeBaltopCommand(this),
            new PlantCommand(this),
            new HarvestCommand(this)
        });

        generateTimer();

        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

        exec.schedule(new Runnable() {
            public void run() {
                generatePlanterCache();
            }
        }, 1, TimeUnit.SECONDS);

    }

    public void generateTimer() {
        timerTask = new TimerTask() {

            @Override
            public void run() {

                growPlants();

            }

        };

        timer.schedule(timerTask, 60000, 60000);
    }

    public void generatePlantTimer() {
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        timerTask = new TimerTask() {

            @Override
            public void run() {
                growPlants();
            }
        };

        timer.schedule(timerTask, 60000, 60000);
        exec.schedule(new Runnable() {
            public void run() {
                generatePlanterCache();
            }
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        for (ICommand command : getCommands()) {
            command.run(event);
        }
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        for (ICommand command : getCommands()) {
            command.run(event);
        }
    }
    
    public String getUserCSVAtIndex(Guild guild, User user, int index) {
        File file = new File("data/" + getName() + "/" + guild.getId() + "/" + user.getId() + ".csv");

        if (!file.exists()) {
            return "";
        }

        return Utilities.getCSVValueAtIndex(Utilities.read(file), index);
    }

    public void setUserCSVAtIndex(Guild guild, User user, int index, String newValue) {
        File file = new File("data/" + getName() + "/" + guild.getId() + "/" + user.getId() + ".csv");
        String data = Utilities.read(file);
        String[] originalValues = data.split(",");
        String[] newValues;

        if (originalValues.length > index) {
            newValues = data.split(",");
        } else {
            newValues = new String[index + 1];
            System.arraycopy(originalValues, 0, newValues, 0, originalValues.length);

            for (int i = originalValues.length; i < newValues.length; i++) {
                newValues[i] = "";
            }
        }
        
        newValues[index] = newValue;
        Utilities.write(file, Utilities.buildCSV(newValues));
    }

    public int getUserBalance(Guild guild, User user) {
        try {
            return Integer.parseInt(getUserCSVAtIndex(guild, user, 0));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public int getUserBalance(Member member) {
        return getUserBalance(member.getGuild(), member.getUser());
    }

    public void setUserBalance(Guild guild, User user, int amount) {
        setUserCSVAtIndex(guild, user, 0, String.valueOf(amount));
        updateUserBaltop(guild, user, amount);
    }

    public void setUserBalance(Member member, int amount) {
        setUserBalance(member.getGuild(), member.getUser(), amount);
    }

    public void addUserBalance(Guild guild, User user, int amount) {
        setUserBalance(guild, user, getUserBalance(guild, user) + amount);
    }
    
    public void addUserBalance(Member member, int amount) {
        addUserBalance(member.getGuild(), member.getUser(), amount);
    }
    
    public int getUserJackpot(Guild guild, User user) {
        try {
            return Integer.parseInt(getUserCSVAtIndex(guild, user, 1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getUserJackpot(Member member) {
        return getUserJackpot(member.getGuild(), member.getUser());
    }

    public void setUserJackpot(Guild guild, User user, int amount) {
        setUserCSVAtIndex(guild, user, 1, String.valueOf(amount));
    }
    
    public void setUserJackpot(Member member, int amount) {
        setUserJackpot(member.getGuild(), member.getUser(), amount);
    }

    public void updateJackpot(Guild guild, int jackpotCap, int jackpotBalance) {
        Utilities.write(new File("data/" + getName() + "/" + guild.getId() + "/jackpot.csv"), jackpotCap + "," + jackpotBalance);
    }

    public File[] getGuildFolders() {
        File[] dataFiles = new File("data/" + getName()).listFiles();

        List<File> guildFolders = new ArrayList();
        File[] array;

        for (File dataFile : dataFiles) {
            if (dataFile.isDirectory()) {

                String folderName = dataFile.getName();

                try {
                    Long.parseLong(folderName);
                    guildFolders.add(dataFile);
                } catch (NumberFormatException e) {
                }
            }
        }

        array = new File[guildFolders.size()];
        guildFolders.toArray(array);
        return array;
    }

    private void generatePlanterCache() {

        File[] guildFolders = getGuildFolders();

        for (File guildFolder : guildFolders) {

            String guildId = guildFolder.getName();

            File[] userFiles = guildFolder.listFiles();
            for (File userFile : userFiles) {
                String userId = userFile.getName().replace(".csv", "");
                System.out.println(userId);

                try {
                    Long.parseLong(userId);
                    Member member = memberFromIds(guildId, userId);
                    if (member != null) planters.add(member);
                    else System.out.println("null member");
                } catch (NumberFormatException e) {
                }
            }

        }

        for (Member member : planters) {
            System.out.println("member:" + member.getEffectiveName());
        }
    }

    private Member memberFromIds(String guildId, String userId) {
        JDA jda = getJDA();
        Guild guild = jda.getGuildById(Long.parseLong(guildId));
        User user = jda.getUserById(userId);
        return guild.getMember(user);
    }
	
    public int getUserPlant(Guild guild, User user) {
        try {
            return Integer.parseInt(getUserCSVAtIndex(guild, user, 4));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getUserPlant(Member member) {
        return getUserPlant(member.getGuild(), member.getUser());
    }

    public void setUserPlant(Guild guild, User user, int amount) {
        setUserCSVAtIndex(guild, user, 4, String.valueOf(amount));
    }

    public void setUserPlant(Member member, int amount) {
        setUserPlant(member.getGuild(), member.getUser(), amount);
    }

    public void updatePlant(Guild guild, int plantBalance) {
        Utilities.write(new File("data/" + getName() + "/" + guild.getId() + "/plant.csv"), String.valueOf(plantBalance));
    }

    public int getTotalPlant(Guild guild) {
        try {
            String info = Utilities.read(new File("data/" + getName() + "/" + guild.getId() + "/plant.csv"));
            return Integer.parseInt(Utilities.getCSVValueAtIndex(info, 0));
        } catch (NumberFormatException e) {
            updatePlant(guild, 0);
            return 0;
        }
    }

    public void growPlants() {

        HashMap<Guild, Integer> totals = new HashMap<>();
		
        for (Member planter : planters) {
            if (isHarvesting(planter.getGuild())) {
                continue;
            }
            int planterPlantAmount = (int) Math.ceil(getUserPlant(planter) * 1.01);
            if (planterPlantAmount > PLANT_GROWTH_MAX && getUserPlant(planter) <= PLANT_GROWTH_MAX) {
                planterPlantAmount = PLANT_GROWTH_MAX;
            }
            setUserPlant(planter, planterPlantAmount);
            totals.put(planter.getGuild(), totals.getOrDefault(planter, 0) + planterPlantAmount);
        }

        for (Guild guild : totals.keySet()) {
            updatePlant(guild, totals.get(guild));
        }
    }

    public void resetPlanters(Guild guild) {
        for (Member planter : planters) {
            if (planter.getGuild().equals(guild)) {
                setUserPlant(planter, 0);
            }
        }
        planters.removeAll(guild.getMembers());
    }

    public boolean isHarvesting(Guild guild) {
        return harvesting.getOrDefault(guild, false);
    }

    public void setHarvesting(Guild guild, boolean harvesting) {
        this.harvesting.put(guild, harvesting);
    }

    public int getUserDaily(Guild guild, User user) {
        try {
            return Integer.parseInt(getUserCSVAtIndex(guild, user, 2));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getUserDaily(Member member) {
        return getUserDaily(member.getGuild(), member.getUser());
    }

    public void setUserDaily(Guild guild, User user, int date) {
        setUserCSVAtIndex(guild, user, 2, String.valueOf(date));
    }
    
    public void setUserDaily(Member member, int date) {
        setUserDaily(member.getGuild(), member.getUser(), date);
    }
    
    public int getUserBaltop(Guild guild, User user) {
        try {
            return Integer.parseInt(getUserCSVAtIndex(guild, user, 3));
        } catch (NumberFormatException e) {
            int userBalance = getUserBalance(guild, user);
            setUserBaltop(guild, user, userBalance);
            return userBalance;
        }
    }

    public int getUserBaltop(Member member) {
        return getUserBaltop(member.getGuild(), member.getUser());
    }

    private void setUserBaltop(Guild guild, User user, int amount) {
        setUserCSVAtIndex(guild, user, 3, String.valueOf(amount));
    }

    private void setUserBaltop(Member member, int amount) {
        setUserBaltop(member.getGuild(), member.getUser(), amount);
    }
    
    public void updateUserBaltop(Guild guild, User user, int amount) {
        if (getUserBaltop(guild, user) < amount) {
            setUserBaltop(guild, user, amount);
        }
    }
    
    /*public void updateUserBaltop(Member member, int amount) {
        if (getUserBaltop(member) < amount) {
            setUserBaltop(member, amount);
        }
    }*/
    
    @Override
    public MaiDiscordBotCommandBase[] getCommands() {
        ICommand[] commands = super.getCommands();
        MaiDiscordBotCommandBase[] newCommands = new MaiDiscordBotCommandBase[commands.length];
        
        for (int i = 0; i < commands.length; i++) {
            newCommands[i] = (MaiDiscordBotCommandBase) commands[i];
        }
        
        return newCommands;
    }
}
