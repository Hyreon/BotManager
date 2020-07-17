package botmanager.frostbalance;

import botmanager.frostbalance.history.RegimeData;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.List;

/**
 * A HotMap is identical to a HashMap; however, its getOrDefault method
 * will put a value there if there wasn't one before, rather than just returning
 * the default. This should reduce unintended behavior over indexed list maps.
 */
public class HotMap extends HashMap<Guild, List<RegimeData>> {

    @Override
    public List<RegimeData> getOrDefault(Object key, List<RegimeData> defaultValue) {
        List<RegimeData> result = super.getOrDefault(key, defaultValue);
        if (super.get(key) == null) {
            super.put((Guild) key, result);
        }
        return result;
    }
}
