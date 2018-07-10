package ai.qantumsense.tgmonitor.telethon.datamapping;

import ai.qantumsense.tgmonitor.telethon.DataMapper;
import ai.quantumsense.tgmonitor.backend.pojo.TelegramMessage;
import com.google.gson.Gson;

public class JsonGsonDataMapper implements DataMapper {

    private Gson gson = new Gson();

    /**
     * Map monitor process output to TelegramMessage.
     *
     * Assumes the process output to be a single line of JSON.
     *
     * This implementation relies on equality of JSON property names and the
     * field names in TelegramMessage. So, all JSON properties that correspond
     * to a field in TelegramMessage must be named exactly the same as the
     * corresponding field in TelegramMessage.
     *
     * You have to be aware of this when you edit the Telethon Python scripts!
     *
     * JSON properties that can't be mapped to a field in TelegramMessage
     * are ignored.
     *
     * @param str String produced by monitor process for a new message.
     * @return TelegramMessage object with all fields initialised.
     */
    @Override
    public TelegramMessage mapTelegramMessage(String str) {
        return gson.fromJson(str, TelegramMessage.class);
    }
}
