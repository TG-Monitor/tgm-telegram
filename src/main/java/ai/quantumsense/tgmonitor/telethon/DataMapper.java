package ai.quantumsense.tgmonitor.telethon;

import ai.quantumsense.tgmonitor.backend.pojo.TelegramMessage;

public interface DataMapper {
    /**
     * Map string output of monitor processes for a newly received message to
     * a TelegramMessage object.
     * @param str String produced by monitor process for a new message.
     * @return TelegramMessage object with all fields initialised.
     */
    TelegramMessage mapTelegramMessage(String str);
}
