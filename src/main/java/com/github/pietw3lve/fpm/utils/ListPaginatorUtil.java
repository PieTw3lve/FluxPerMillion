package com.github.pietw3lve.fpm.utils;

import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A simple utility to create chat pages
 *
 * @param <E> The type of list to sort
 * @param <M> Messaging sending generic. See {@link MessagePlatform}
 * @author ReflxctionDev
 */
public class ListPaginatorUtil<E, M> {

    /**
     * The amount of lines each page can have. This does <i>NOT</i> include
     * the separators
     */
    private final int linesPerPage;

    /**
     * The messaging platform
     */
    private final MessagePlatform<M> platform;

    /**
     * A simple string converter
     */
    private final MessageConverter<E, M> converter;

    /**
     * The header that comes before sending the page lines
     */
    private Header header;

    /**
     * The footer that comes by the end of the page lines
     */
    private Footer footer;

    /**
     * A task invoked when the provided list is empty
     */
    private Consumer<CommandSender> ifEmpty;

    /**
     * A task invoked when the specified/requested page does not exist
     */
    private BiConsumer<CommandSender, Integer> invalidPage;

    /**
     * Creates a paginator which displays the specified amount of lines per page
     *
     * @param linesPerPage Lines that each page displays
     */
    public ListPaginatorUtil(int linesPerPage, MessagePlatform<M> platform, MessageConverter<E, M> converter) {
        this.linesPerPage = linesPerPage;
        this.platform = platform;
        this.converter = converter;
    }

    /**
     * Sets the header of each page, which appears before the page content is sent
     *
     * @param header New header to set
     * @return A reference to this object
     */
    public ListPaginatorUtil<E, M> setHeader(Header header) {
        this.header = header;
        return this;
    }

    /**
     * Sets the footer of each page, which appears at the end of the page content
     *
     * @param footer New footer to set
     * @return A reference to this object
     */
    public ListPaginatorUtil<E, M> setFooter(Footer footer) {
        this.footer = footer;
        return this;
    }

    /**
     * Adds a callback method which is invoked when the passed list in {@link #sendPage(List, CommandSender, int)}
     * is empty.
     *
     * @param task Task to execute
     * @return A reference to this object
     */
    public ListPaginatorUtil<E, M> ifEmpty(Consumer<CommandSender> task) {
        this.ifEmpty = task;
        return this;
    }

    /**
     * Adds a callback method when the page index passed in {@link #sendPage(List, CommandSender, int)} is invalid (i.e less than 0
     * or greater than the pages size)
     *
     * @param task Task to execute
     * @return A reference to this object
     */
    public ListPaginatorUtil<E, M> ifPageIsInvalid(BiConsumer<CommandSender, Integer> task) {
        this.invalidPage = task;
        return this;
    }

    /**
     * Returns the amount of pages this list contains. It divides by the page, and if there are any remaining elements
     * (i.e remainders) they get their own page (which adds by 1).
     *
     * @param list List to get the size for
     * @return The pages count this list can hold
     */
    public int getPageSize(List<E> list) {
        return list.size() / linesPerPage + (list.size() % linesPerPage == 0 ? 0 : 1);
    }

    /**
     * Sends a page content to the specified {@link CommandSender}. Content is converted to a string message
     * using the passed {@link MessageConverter} which is specified in the constructor
     *
     * @param list       List to send to the receiver
     * @param target     Target to send the list to
     * @param pageNumber Page index to send for
     */
    public void sendPage(List<E> list, CommandSender target, int pageNumber) {
        if (list.isEmpty()) {
            if (ifEmpty != null)
                ifEmpty.accept(target);
            return;
        }
        int size = getPageSize(list);
        if (pageNumber > size) {
            if (invalidPage != null) {
                invalidPage.accept(target, pageNumber);
                return;
            } else {
                throw new IllegalArgumentException(String.format("Page number %s is greater than the pages size %s", String.valueOf(pageNumber), String.valueOf(size)));
            }
        }
        if (pageNumber <= 0) {
            if (invalidPage != null) {
                invalidPage.accept(target, pageNumber);
                return;
            } else {
                throw new IllegalArgumentException(String.format("Page number %s invalid", String.valueOf(pageNumber)));
            }
        }
        int listIndex = pageNumber - 1;
        int l = Math.min(pageNumber * linesPerPage, list.size());
        if (header != null)
            header.sendHeader(target, pageNumber, size);
        for (int i = listIndex * linesPerPage; i < l; i++) {
            E element = list.get(i);
            M message = converter.convert(element);
            platform.send(target, message);
        }
        if (footer != null)
            footer.sendFooter(target, pageNumber, size);
    }

    /**
     * A simple functional interface which acts like a medium to send messages. This can be used when standard
     * messaging system (i.e {@link CommandSender#sendMessage(String)}) is not desired, or when it's desired to use
     * chat components rather than raw strings
     *
     * @param <M> Message generic
     */
    @FunctionalInterface
    public interface MessagePlatform<M> {

        /**
         * The standard Bukkit messaging method
         */
        MessagePlatform<String> NORMAL = CommandSender::sendMessage;

        /**
         * Sends the message to target
         *
         * @param target  Sender to send the message to
         * @param message Message to send
         */
        void send(CommandSender target, M message);

    }

    /**
     * A simple functional interface to change a field to be a valid messaging value. This will allow convenient converting
     * to messages if a specific implementation is required
     *
     * @param <E> Element type
     * @param <M> Message object generic. See {@link MessagePlatform}
     */
    @FunctionalInterface
    public interface MessageConverter<E, M> {

        /**
         * Converts the object to be a string which can be sent to chat
         *
         * @param e Object to convert to a string
         * @return The string value
         */
        M convert(E e);

    }

    /**
     * Represents a header which is specified in {@link ListPaginator#setHeader(Header)}.
     * <p>
     * Headers appear before sending the page content
     */
    @FunctionalInterface
    public interface Header {

        /**
         * Sends the header to the specified player
         *
         * @param target    Sender to send the header to
         * @param pageIndex A representation of the current page index
         * @param pageCount A representation of the total page count
         */
        void sendHeader(CommandSender target, int pageIndex, int pageCount);

    }

    /**
     * Represents a footer which is specified in {@link ListPaginator#setFooter(Footer)}
     * <p>
     * Footers appear after sending the page content
     */
    @FunctionalInterface
    public interface Footer {

        void sendFooter(CommandSender target, int pageIndex, int pageCount);

    }
}
