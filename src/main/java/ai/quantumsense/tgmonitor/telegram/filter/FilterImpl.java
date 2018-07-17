package ai.quantumsense.tgmonitor.telegram.filter;

import ai.quantumsense.tgmonitor.entities.Peers;
import ai.quantumsense.tgmonitor.servicelocator.ServiceLocator;
import ai.quantumsense.tgmonitor.telegram.Filter;
import ai.quantumsense.tgmonitor.backend.pojo.TelegramMessage;
import com.google.gson.Gson;

import javax.annotation.Nullable;

/**
 * Filter implementation assuming lines in the following format:
 *
 * <peer-username><space><json-object>
 */
public class FilterImpl implements Filter {

    private Gson gson = new Gson();
    private ServiceLocator<Peers> peersLocator;

    public FilterImpl(ServiceLocator<Peers> peersLocator) {
        this.peersLocator = peersLocator;
    }

    @Override
    public @Nullable TelegramMessage filterAndParse(String line) {
        String json = filter(line);
        if (json != null)
            return parse(json);
        else
            return null;
    }

    /**
     * Map a JSON object to a TelegramMessage object.
     *
     * This implementation works only if the JSON properties have the same names
     * and structure as in the TelegramMessage object.
     *
     * This is important to keep in mind when editing the read_messages.py
     * script of the TelegramMessage class.
     *
     * Excess JSON properties that can't be mapped to a field in TelegramMessage
     * are ignored.
     */
    private TelegramMessage parse(String json) {
        return gson.fromJson(json, TelegramMessage.class);
    }

    /**
     * Check if the provided line of output corresponds to a message in one of
     * the set peers in the Peers entity. If yes, return the JSON representation
     * of the message as a string. If no, return null.
     *
     * Expects the format of a line to be (see read_messages.py script):
     *
     * <peer-username><space><json-object>
     */
    private @Nullable String filter(String line) {
        String[] a = line.split(" ", 2);
        for (String p : peersLocator.getService().getPeers()) {
            if (a[0].toLowerCase().equals(p.toLowerCase())) return a[1];
        }
        return null;
    }
}
