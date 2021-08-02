package com.senderman.linkbannerbot;

import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;

public class LinkCheckingUtils {

    public static final String tgChatMessageLinkRegex = "(?:https?://)?t.me/c/\\d+/\\d+";
    public static final String anyLinkRegex = ".*(?:https?://)?(?:www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-z]{2,4}\\b[-a-zA-Z0-9@:%_+.~#?&/=]*.*";
    public static final String tgChatLinkRegex = "(?:https?://)?t.me/[-a-zAZ0-9_]";

    public static boolean hasBadLink(String text) {
        return text
                .replaceAll("(https?://)?(t.me|telegram.org)", "")
                .matches(anyLinkRegex);
    }

    public static boolean hasBadChatLink(String text, long chatId, @Nullable String chatUsername) {
        var t1 = text.replaceAll(".*(" + tgChatMessageLinkRegex + ").*", "$1").replaceAll("(?:https?://)?t.me/c/", "");
        boolean check1 = ("-100" + t1).startsWith(String.valueOf(chatId));
        if (check1)
            return false;
        var chatLink = text.replaceAll("(?:https?://)?t.me/", "");
        if (chatUsername != null && chatUsername.equals(chatLink)) {
            return false;
        }
        return !isUser(chatLink);
    }

    public static boolean isUser(String mention) {
        try {
            return Jsoup.connect("https://t.me/" + mention)
                    .get()
                    .selectFirst("a.tgme_action_button_new")
                    .text()
                    .equals("Send Message");
        } catch (Exception e) {
            return true;
        }
    }
}
