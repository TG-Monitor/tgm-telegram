package ai.quantumsense.tgmonitor.telegram;

import ai.quantumsense.tgmonitor.backend.pojo.TelegramMessage;

import javax.annotation.Nullable;

public interface Filter {

    /**
     * Filter and parse a line of output for a received message from the
     * read_messages.py script.
     *
     * Filtering: check if the message is from one of the set peers in Peers.
     * Return null if this is not the case.
     *
     * Parsing: map the serialised representation of the message to a
     * TelegramMessage object.
     *
     * @param line Line of output of read_messages.py for a new message
     *
     * @return TelegramMessage object if the message is from one of the defined
     * peers, and null otherwise.
     */
    @Nullable TelegramMessage filterAndParse(String line);
}
