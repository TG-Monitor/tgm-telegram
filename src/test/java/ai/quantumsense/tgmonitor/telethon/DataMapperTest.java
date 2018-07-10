package ai.quantumsense.tgmonitor.telethon;

import ai.qantumsense.tgmonitor.telethon.DataMapper;
import ai.qantumsense.tgmonitor.telethon.datamapping.JsonGsonDataMapper;
import ai.quantumsense.tgmonitor.backend.pojo.TelegramMessage;
import org.junit.Test;

public class DataMapperTest {
    @Test
    public void testJsonToTelegramMessage() {
        String line = "{\"id\": 1305312, \"date\": 1531003695, \"text\": \"We're good team. I pick the days I have to work late, so I can empty my schedule at the gym nights \\ud83d\\ude06\", \"replyMessageId\": 1305307, \"sender\": {\"id\": 449519549, \"isBot\": false, \"isContact\": false, \"firstName\": \"\\ud83e\\udd8b \\ud83c\\uddf0 \\ud83c\\uddea \\ud83c\\uddee \\ud83e\\udd8b\", \"lastName\": null, \"username\": null, \"phone\": null}, \"peer\": {\"id\": 1069879618, \"title\": \"(the) English Club\", \"username\": \"the_englishclub\", \"participantsCount\": null, \"megagroup\": true}, \"audio\": false, \"document\": false, \"gif\": false, \"photo\": false, \"sticker\": false, \"video\": false, \"voice\": false}";
        DataMapper mapper = new JsonGsonDataMapper();
        TelegramMessage msg = mapper.mapTelegramMessage(line);
        System.out.println(msg);
    }
}
