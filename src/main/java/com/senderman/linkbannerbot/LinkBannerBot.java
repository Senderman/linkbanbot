package com.senderman.linkbannerbot;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.BotModule;
import com.annimon.tgbotsmodule.Runner;
import com.annimon.tgbotsmodule.beans.Config;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LinkBannerBot implements BotModule {

    public static void main(String[] args) {
        Runner.run(List.of(new LinkBannerBot()));
    }

    @Override
    public @NotNull BotHandler botHandler(@NotNull Config config) {
        return new com.senderman.linkbannerbot.BotHandler();
    }
}
