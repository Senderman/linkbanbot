package com.senderman.linkbannerbot;

import com.annimon.tgbotsmodule.api.methods.Methods;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

public class BotHandler extends com.annimon.tgbotsmodule.BotHandler {

    private final String username;
    private final String token;
    private final long mainChatId;

    public BotHandler() {
        this.username = System.getenv("username");
        this.token = System.getenv("token");
        this.mainChatId = Long.parseLong(System.getenv("chat_id"));
    }

    @Override
    protected BotApiMethod<?> onUpdate(@NotNull Update update) {

        if (!update.hasMessage())
            return null;

        var message = update.getMessage();
        if (!message.getChatId().equals(mainChatId)) {
            Methods.leaveChat(message.getChatId()).callAsync(this);
            return null;
        }

        if (!message.hasText())
            return null;

        if (!message.hasEntities())
            return null;

        var chatUsername = message.getChat().getUserName();
        var urlEntities = message.getEntities()
                .stream()
                .filter(e -> e.getType().equals("url") || e.getType().equals("text_link"))
                .toList();
        long goodUrls = urlEntities
                .stream()
                .map(e -> Objects.requireNonNullElseGet(e.getUrl(), e::getText))
                .filter(e -> !LinkCheckingUtils.hasBadLink(e))
                .filter(e -> !LinkCheckingUtils.hasBadChatLink(e, message.getChatId(), chatUsername))
                .count();
        if (goodUrls < urlEntities.size()) {
            ban(message);
            return null;
        }

        boolean hasUserMentions = message.getEntities()
                .stream()
                .filter(e -> e.getType().equals("mention"))
                .map(MessageEntity::getText)
                .map(e -> e.replaceAll("@", ""))
                .filter(e -> chatUsername == null || !chatUsername.equals(e))
                .anyMatch(e -> !LinkCheckingUtils.isUser(e));
        if (hasUserMentions) {
            ban(message);
            return null;
        }

        return null;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    private void ban(Message message) {
        /*Methods.sendMessage()
                .setChatId(message.getChatId())
                .setReplyToMessageId(message.getMessageId())
                .setText("Бан!!!")
                .callAsync(this);*/
        Methods.Administration.kickChatMember()
                .setChatId(message.getChatId())
                .setUserId(message.getFrom().getId())
                .callAsync(this);
        Methods.deleteMessage()
                .setChatId(message.getChatId())
                .setMessageId(message.getMessageId())
                .callAsync(this);
        Methods.sendMessage()
                .setChatId(message.getChatId())
                .setText("Пользователь забанен за ссылку: " + message.getFrom().getFirstName())
                .callAsync(this);
    }
}
