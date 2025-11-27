/*
 * DiscordSRV - https://github.com/DiscordSRV/DiscordSRV
 *
 * Copyright (C) 2016 - 2024 Austin "Scarsz" Shapiro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */

package github.scarsz.discordsrv.objects.threads;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.util.DiscordUtil;
import github.scarsz.discordsrv.util.PlaceholderUtil;
import github.scarsz.discordsrv.util.TimeUtil;
import lombok.Getter;
import net.dv8tion.jda.api.entities.GuildChannel;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ChannelUpdater extends Thread {

    @Getter private final Set<UpdaterChannel> updaterChannels = new HashSet<>();

    public ChannelUpdater() {
        setName("DiscordSRV - Channel Updater");
    }

    @Override
    public void run() {
        return;
    }

    public static class UpdaterChannel {

        public static final int MAX_CHANNEL_NAME = 100;
        @Getter private final String channelId;
        @Getter private final String format;
        @Getter private final int interval;
        @Getter @Nullable private final String shutdownFormat;
        private int minutesUntilRefresh;

        public UpdaterChannel(GuildChannel channel, String format, int interval, @Nullable String shutdownFormat) {
            this.channelId = channel.getId();
            this.format = format;
            this.shutdownFormat = shutdownFormat;

            // Minimum value for the interval is 10 so we'll make sure it's above that
            if (interval < 10) {
                DiscordSRV.warning("Update interval in minutes for channel \"" + channel.getName() + "\" was below the minimum value of 10. Using 10 as the interval.");
                this.interval = 10;
            } else this.interval = interval;

            this.minutesUntilRefresh = this.interval;

        }

        public void update() {
            final GuildChannel discordChannel = DiscordUtil.getJda().getGuildChannelById(this.channelId);
            if (discordChannel == null) {
                DiscordSRV.error(String.format("Failed to find channel \"%s\". Does it exist?", this.channelId));
                return;
            }

            String newName = PlaceholderUtil.replaceChannelUpdaterPlaceholders(this.format);
            parseChannelName(discordChannel, newName, false);
        }

        public void updateToShutdownFormat() {
            if (this.shutdownFormat == null) return;

            final GuildChannel discordChannel = StringUtils.isNotBlank(this.channelId) && StringUtils.isNumeric(this.channelId)
                    ? DiscordUtil.getJda().getGuildChannelById(this.channelId)
                    : null;
            if (discordChannel == null) {
                DiscordSRV.error(String.format("Failed to find channel \"%s\". Does it exist?", this.channelId));
                return;
            }

            String newName = this.shutdownFormat
                    .replaceAll("%time%|%date%", TimeUtil.timeStamp())
                    .replace("%serverversion%", Bukkit.getBukkitVersion())
                    .replace("%totalplayers%", Integer.toString(DiscordSRV.getTotalPlayerCount()))
                    .replace("%timestamp%", Long.toString(System.currentTimeMillis() / 1000));

            parseChannelName(discordChannel, newName, true);
        }

        public void performTick() {
            this.minutesUntilRefresh--;

            if (this.minutesUntilRefresh <= 0) {
                this.update();
                this.minutesUntilRefresh = this.interval;
            }
        }

        private void parseChannelName(GuildChannel discordChannel, String newName, boolean blockThread) {
            return;
        }
    }

}
