package ai.quantumsense.tgmonitor.test.telegram;

import ai.quantumsense.tgmonitor.telegram.Filter;
import ai.quantumsense.tgmonitor.telegram.filter.FilterImpl;
import ai.quantumsense.tgmonitor.backend.pojo.TelegramMessage;

public class FilterTest extends AbsTelegramTest {

    private static String line = "the_englishclub {\"id\": 1305312, \"date\": 1531003695, \"text\": \"We're good team. I pick the days I have to work late, so I can empty my schedule at the gym nights \\ud83d\\ude06\", \"replyMessageId\": 1305307, \"sender\": {\"id\": 449519549, \"isBot\": false, \"isContact\": false, \"firstName\": \"\\ud83e\\udd8b \\ud83c\\uddf0 \\ud83c\\uddea \\ud83c\\uddee \\ud83e\\udd8b\", \"lastName\": null, \"username\": null, \"phone\": null}, \"peer\": {\"id\": 1069879618, \"title\": \"(the) English Club\", \"username\": \"the_englishclub\", \"participantsCount\": null, \"megagroup\": true}, \"audio\": false, \"document\": false, \"gif\": false, \"photo\": false, \"sticker\": false, \"video\": false, \"voice\": false}";
    private static Filter filter = new FilterImpl(peersLocator);

    private enum Type {
        FILTER_HIT,
        FILTER_MISS
    }

    public static void main(String[] args) {
        Type type = Type.FILTER_HIT;
        switch (type) {
            case FILTER_HIT:
                filterHit();
                break;
            case FILTER_MISS:
                filterMiss();
                break;
        }
    }

    private static void filterHit() {
        peers.addPeer("the_englishclub");
        System.out.println(filter.filterAndParse(line));
    }

    private static void filterMiss() {
        System.out.println(filter.filterAndParse(line));
    }

}
