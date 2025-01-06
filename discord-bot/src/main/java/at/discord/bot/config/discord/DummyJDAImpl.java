package at.discord.bot.config.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.entities.sticker.StickerPack;
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake;
import net.dv8tion.jda.api.entities.sticker.StickerUnion;
import net.dv8tion.jda.api.events.GatewayPingEvent;
import net.dv8tion.jda.api.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildUnavailableEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.InterfacedEventManager;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.managers.DirectAudioController;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.requests.restaction.CommandEditAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.requests.restaction.GuildAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView;
import okhttp3.OkHttpClient;

import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class DummyJDAImpl implements JDA {
    /**
     * Gets the current {@link Status Status} of the JDA instance.
     *
     * @return Current JDA status.
     */
    @Override
    public Status getStatus() {
        return Status.INITIALIZED;
    }

    /**
     * The {@link GatewayIntent GatewayIntents} for this JDA session.
     *
     * @return {@link EnumSet} of active gateway intents
     */
    @Override
    public EnumSet<GatewayIntent> getGatewayIntents() {
        return EnumSet.of(GatewayIntent.GUILD_MEMBERS);
    }

    /**
     * The {@link CacheFlag cache flags} that have been enabled for this JDA session.
     *
     * @return Copy of the EnumSet of cache flags for this session
     */
    @Override
    public EnumSet<CacheFlag> getCacheFlags() {
        return  EnumSet.allOf(CacheFlag.class);
    }

    /**
     * Attempts to remove the user with the provided id from the cache.
     * <br>If you attempt to remove the {@link #getSelfUser() SelfUser} this will simply return {@code false}.
     *
     * <p>This should be used by an implementation of {@link MemberCachePolicy MemberCachePolicy}
     * as an upstream request to remove a member.
     *
     * @param userId The target user id
     * @return True, if the cache was changed
     */
    @Override
    public boolean unloadUser(long userId) {
        return true;
    }

    /**
     * The time in milliseconds that discord took to respond to our last heartbeat
     * <br>This roughly represents the WebSocket ping of this session
     *
     * <p><b>{@link RestAction RestAction} request times do not
     * correlate to this value!</b>
     *
     * <p>The {@link GatewayPingEvent GatewayPingEvent} indicates an update to this value.
     *
     * @return time in milliseconds between heartbeat and the heartbeat ack response
     * @see #getRestPing() Getting RestAction ping
     */
    @Override
    public long getGatewayPing() {
        return 0;
    }

    /**
     * This method will block until JDA has reached the specified connection status.
     *
     * <p><b>Login Cycle</b><br>
     * <ol>
     *  <li>{@link Status#INITIALIZING INITIALIZING}</li>
     *  <li>{@link Status#INITIALIZED INITIALIZED}</li>
     *  <li>{@link Status#LOGGING_IN LOGGING_IN}</li>
     *  <li>{@link Status#CONNECTING_TO_WEBSOCKET CONNECTING_TO_WEBSOCKET}</li>
     *  <li>{@link Status#IDENTIFYING_SESSION IDENTIFYING_SESSION}</li>
     *  <li>{@link Status#AWAITING_LOGIN_CONFIRMATION AWAITING_LOGIN_CONFIRMATION}</li>
     *  <li>{@link Status#LOADING_SUBSYSTEMS LOADING_SUBSYSTEMS}</li>
     *  <li>{@link Status#CONNECTED CONNECTED}</li>
     * </ol>
     *
     * @param status The init status to wait for, once JDA has reached the specified
     *               stage of the startup cycle this method will return.
     * @param failOn Optional failure states that will force a premature return
     * @return The current JDA instance, for chaining convenience
     * @throws InterruptedException     If this thread is interrupted while waiting
     * @throws IllegalArgumentException If the provided status is null or not an init status ({@link Status#isInit()})
     * @throws IllegalStateException    If JDA is shutdown during this wait period
     */
    @Override
    public JDA awaitStatus(Status status, Status... failOn) throws InterruptedException {
        return this;
    }

    /**
     * Blocks the current thread until {@link #getStatus()} returns {@link Status#SHUTDOWN}.
     * <br>This can be useful in certain situations like disabling class loading.
     *
     * <p>Note that shutdown time depends on the length of the rate-limit queue.
     * You can use {@link #shutdownNow()} to cancel all pending requests and immediately shutdown.
     *
     * <p><b>Example</b>
     * <pre>{@code
     * jda.shutdown();
     * // Allow at most 10 seconds for remaining requests to finish
     * if (!jda.awaitShutdown(10, TimeUnit.SECONDS)) {
     *     jda.shutdownNow(); // Cancel all remaining requests
     *     jda.awaitShutdown(); // Wait until shutdown is complete (indefinitely)
     * }
     * }</pre>
     *
     * <p><b>This will not implicitly call {@code shutdown()}, you are responsible to ensure that the shutdown process has started.</b>
     *
     * @param duration The maximum time to wait, or 0 to wait indefinitely
     * @param unit     The time unit for the duration
     * @return False, if the timeout has elapsed before the shutdown has completed, true otherwise.
     * @throws IllegalArgumentException If the provided unit is null
     * @throws InterruptedException     If the current thread is interrupted while waiting
     */
    @Override
    public boolean awaitShutdown(long duration, TimeUnit unit) throws InterruptedException {
        return false;
    }

    /**
     * Cancels all currently scheduled {@link RestAction} requests.
     * <br>When a {@link RestAction} is cancelled, a {@link CancellationException} will be provided
     * to the failure callback. This means {@link RestAction#queue(Consumer, Consumer)} will invoke the second callback
     * and {@link RestAction#complete()} will throw an exception.
     *
     * <p><b>This is only recommended as an extreme last measure to avoid backpressure.</b>
     * If you want to stop requests on shutdown you should use {@link #shutdownNow()} instead of this method.
     *
     * @return how many requests were cancelled
     * @see RestAction#setCheck(BooleanSupplier)
     */
    @Override
    public int cancelRequests() {
        return 0;
    }

    /**
     * {@link ScheduledExecutorService} used to handle rate-limits for {@link RestAction}
     * executions. This is also used in other parts of JDA related to http requests.
     *
     * @return The {@link ScheduledExecutorService} used for http request handling
     * @since 4.0.0
     */
    @Override
    public ScheduledExecutorService getRateLimitPool() {
        return null;
    }

    /**
     * {@link ScheduledExecutorService} used to send WebSocket messages to discord.
     * <br>This involves initial setup of guilds as well as keeping the connection alive.
     *
     * @return The {@link ScheduledExecutorService} used for WebSocket transmissions
     * @since 4.0.0
     */
    @Override
    public ScheduledExecutorService getGatewayPool() {
        return null;
    }

    /**
     * {@link ExecutorService} used to handle {@link RestAction} callbacks
     * and completions. This is also used for handling {@link Message.Attachment} downloads
     * when needed.
     * <br>By default this uses the {@link ForkJoinPool#commonPool() CommonPool} of the runtime.
     *
     * @return The {@link ExecutorService} used for callbacks
     * @since 4.0.0
     */
    @Override
    public ExecutorService getCallbackPool() {
        return null;
    }

    /**
     * The {@link OkHttpClient} used for handling http requests from {@link RestAction RestActions}.
     *
     * @return The http client
     * @since 4.0.0
     */
    @Override
    public OkHttpClient getHttpClient() {
        return null;
    }

    /**
     * Direct access to audio (dis-)connect requests.
     * <br>This should not be used when normal audio operation is desired.
     *
     * <p>The correct way to open and close an audio connection is through the {@link Guild Guild's}
     * {@link AudioManager}.
     *
     * @return The {@link DirectAudioController} for this JDA instance
     * @throws IllegalStateException If {@link GatewayIntent#GUILD_VOICE_STATES} is disabled
     * @since 4.0.0
     */
    @Override
    public DirectAudioController getDirectAudioController() {
        return null;
    }

    /**
     * Changes the internal EventManager.
     *
     * <p>The default EventManager is {@link InterfacedEventManager InterfacedEventListener}.
     * <br>There is also an {@link AnnotatedEventManager AnnotatedEventManager} available.
     *
     * @param manager The new EventManager to use
     */
    @Override
    public void setEventManager(IEventManager manager) {

    }

    /**
     * Adds all provided listeners to the event-listeners that will be used to handle events.
     * This uses the {@link InterfacedEventManager InterfacedEventListener} by default.
     * To switch to the {@link AnnotatedEventManager AnnotatedEventManager}, use {@link #setEventManager(IEventManager)}.
     *
     * <p>Note: when using the {@link InterfacedEventManager InterfacedEventListener} (default),
     * given listener <b>must</b> be instance of {@link EventListener EventListener}!
     *
     * @param listeners The listener(s) which will react to events.
     * @throws IllegalArgumentException If either listeners or one of it's objects is {@code null}.
     */
    @Override
    public void addEventListener(Object... listeners) {

    }

    /**
     * Removes all provided listeners from the event-listeners and no longer uses them to handle events.
     *
     * @param listeners The listener(s) to be removed.
     * @throws IllegalArgumentException If either listeners or one of it's objects is {@code null}.
     */
    @Override
    public void removeEventListener(Object... listeners) {

    }

    /**
     * Immutable List of Objects that have been registered as EventListeners.
     *
     * @return List of currently registered Objects acting as EventListeners.
     */
    @Override
    public List<Object> getRegisteredListeners() {
        return null;
    }

    /**
     * Retrieves the list of global commands.
     * <br>This list does not include guild commands! Use {@link Guild#retrieveCommands()} for guild commands.
     *
     * @param withLocalizations {@code true} if the localization data (such as name and description) should be included
     * @return {@link RestAction} - Type: {@link List} of {@link Command}
     */
    @Override
    public RestAction<List<Command>> retrieveCommands(boolean withLocalizations) {
        return null;
    }

    /**
     * Retrieves the existing {@link Command} instance by id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param id The command id
     * @return {@link RestAction} - Type: {@link Command}
     * @throws IllegalArgumentException If the provided id is not a valid snowflake
     */
    @Override
    public RestAction<Command> retrieveCommandById(String id) {
        return null;
    }

    /**
     * Creates or updates a global command.
     * <br>If a command with the same name exists, it will be replaced.
     * This operation is idempotent.
     * Commands will persist between restarts of your bot, you only have to create a command once.
     *
     * <p>To specify a complete list of all commands you can use {@link #updateCommands()} instead.
     *
     * <p>You need the OAuth2 scope {@code "applications.commands"} in order to add commands to a guild.
     *
     * @param command The {@link CommandData} for the command
     * @return {@link RestAction} - Type: {@link Command}
     * <br>The RestAction used to create or update the command
     * @throws IllegalArgumentException If null is provided
     * @see SlashCommands#slash(String, String) Commands.slash(...)
     * @see SlashCommands#message(String) Commands.message(...)
     * @see SlashCommands#user(String) Commands.user(...)
     * @see Guild#upsertCommand(CommandData) Guild.upsertCommand(...)
     */
    @Override
    public RestAction<Command> upsertCommand(CommandData command) {
        return null;
    }

    /**
     * Configures the complete list of global commands.
     * <br>This will replace the existing command list for this bot. You should only use this once on startup!
     *
     * <p>This operation is idempotent.
     * Commands will persist between restarts of your bot, you only have to create a command once.
     *
     * <p>You need the OAuth2 scope {@code "applications.commands"} in order to add commands to a guild.
     *
     * <p><b>Examples</b>
     *
     * <p>Set list to 2 commands:
     * <pre>{@code
     * jda.updateCommands()
     *   .addCommands(Commands.slash("ping", "Gives the current ping"))
     *   .addCommands(Commands.slash("ban", "Ban the target user")
     *     .setGuildOnly(true)
     *     .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
     *     .addOption(OptionType.USER, "user", "The user to ban", true))
     *   .queue();
     * }</pre>
     *
     * <p>Delete all commands:
     * <pre>{@code
     * jda.updateCommands().queue();
     * }</pre>
     *
     * @return {@link CommandListUpdateAction}
     * @see Guild#updateCommands()
     */
    @Override
    public CommandListUpdateAction updateCommands() {
        return null;
    }

    /**
     * Edit an existing global command by id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param id The id of the command to edit
     * @return {@link CommandEditAction} used to edit the command
     * @throws IllegalArgumentException If the provided id is not a valid snowflake
     */
    @Override
    public CommandEditAction editCommandById(String id) {
        return null;
    }

    /**
     * Delete the global command for this id.
     *
     * <p>If there is no command with the provided ID,
     * this RestAction fails with {@link ErrorResponse#UNKNOWN_COMMAND ErrorResponse.UNKNOWN_COMMAND}
     *
     * @param commandId The id of the command that should be deleted
     * @return {@link RestAction}
     * @throws IllegalArgumentException If the provided id is not a valid snowflake
     */
    @Override
    public RestAction<Void> deleteCommandById(String commandId) {
        return null;
    }

    /**
     * Retrieves the currently configured {@link RoleConnectionMetadata} records for this application.
     *
     * @return {@link RestAction} - Type: {@link List} of {@link RoleConnectionMetadata}
     * @see <a href="https://discord.com/developers/docs/tutorials/configuring-app-metadata-for-linked-roles" target="_blank">Configuring App Metadata for Linked Roles</a>
     */
    @Override
    public RestAction<List<RoleConnectionMetadata>> retrieveRoleConnectionMetadata() {
        return null;
    }

    /**
     * Updates the currently configured {@link RoleConnectionMetadata} records for this application.
     *
     * <p>Returns the updated connection metadata records on success.
     *
     * @param records The new records to set
     * @return {@link RestAction} - Type: {@link List} of {@link RoleConnectionMetadata}
     * @throws IllegalArgumentException If null is provided or more than {@value RoleConnectionMetadata#MAX_RECORDS} records are configured.
     * @see <a href="https://discord.com/developers/docs/tutorials/configuring-app-metadata-for-linked-roles" target="_blank">Configuring App Metadata for Linked Roles</a>
     */
    @Override
    public RestAction<List<RoleConnectionMetadata>> updateRoleConnectionMetadata(Collection<? extends RoleConnectionMetadata> records) {
        return null;
    }

    /**
     * Constructs a new {@link Guild Guild} with the specified name
     * <br>Use the returned {@link GuildAction GuildAction} to provide
     * further details and settings for the resulting Guild!
     *
     * <p>This RestAction does not provide the resulting Guild!
     * It will be in a following {@link GuildJoinEvent GuildJoinEvent}.
     *
     * @param name The name of the resulting guild
     * @return {@link GuildAction GuildAction}
     * <br>Allows for setting various details for the resulting Guild
     * @throws IllegalStateException    If the currently logged in account is in 10 or more guilds
     * @throws IllegalArgumentException If the provided name is empty, {@code null} or not between 2-100 characters
     */
    @Override
    public GuildAction createGuild(String name) {
        return null;
    }

    /**
     * Constructs a new {@link Guild Guild} from the specified template code.
     *
     * <p>This RestAction does not provide the resulting Guild!
     * It will be in a following {@link GuildJoinEvent GuildJoinEvent}.
     *
     * <p>Possible {@link ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link ErrorResponse#UNKNOWN_GUILD_TEMPLATE Unknown Guild Template}
     *     <br>The template doesn't exist.</li>
     * </ul>
     *
     * @param code The template code to use to create a guild
     * @param name The name of the resulting guild
     * @param icon The {@link Icon Icon} to use, or null to use no icon
     * @return {@link RestAction RestAction}
     * @throws IllegalStateException    If the currently logged in account is in 10 or more guilds
     * @throws IllegalArgumentException If the provided name is empty, {@code null} or not between 2-100 characters
     */
    @Override
    public RestAction<Void> createGuildFromTemplate(String code, String name, Icon icon) {
        return null;
    }

    /**
     * {@link CacheView CacheView} of
     * all cached {@link AudioManager AudioManagers} created for this JDA instance.
     * <br>AudioManagers are created when first retrieved via {@link Guild#getAudioManager() Guild.getAudioManager()}.
     * <u>Using this will perform better than calling {@code Guild.getAudioManager()} iteratively as that would cause many useless audio managers to be created!</u>
     *
     * <p>AudioManagers are cross-session persistent!
     *
     * @return {@link CacheView CacheView}
     */
    @Override
    public CacheView<AudioManager> getAudioManagerCache() {
        return null;
    }

    /**
     * {@link SnowflakeCacheView SnowflakeCacheView} of
     * all <b>cached</b> {@link User Users} visible to this JDA session.
     *
     * @return {@link SnowflakeCacheView SnowflakeCacheView}
     */
    @Override
    public SnowflakeCacheView<User> getUserCache() {
        return null;
    }

    /**
     * Gets all {@link Guild Guilds} that contain all given users as their members.
     *
     * @param users The users which all the returned {@link Guild Guilds} must contain.
     * @return Immutable list of all {@link Guild Guild} instances which have all {@link User Users} in them.
     * @see Guild#isMember(UserSnowflake)
     */
    @Override
    public List<Guild> getMutualGuilds(User... users) {
        return null;
    }

    /**
     * Gets all {@link Guild Guilds} that contain all given users as their members.
     *
     * @param users The users which all the returned {@link Guild Guilds} must contain.
     * @return Immutable list of all {@link Guild Guild} instances which have all {@link User Users} in them.
     */
    @Override
    public List<Guild> getMutualGuilds(Collection<User> users) {
        return null;
    }

    /**
     * Attempts to retrieve a {@link User User} object based on the provided id.
     *
     * <p>If {@link #getUserById(long)} is cached, this will directly return the user in a completed {@link RestAction} without making a request.
     * When both {@link GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} and {@link GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intents
     * are disabled this will always make a request even if the user is cached.
     * You can use {@link CacheRestAction#useCache(boolean) action.useCache(false)} to force an update.
     *
     * <p>The returned {@link RestAction RestAction} can encounter the following Discord errors:
     * <ul>
     *     <li>{@link ErrorResponse#UNKNOWN_USER ErrorResponse.UNKNOWN_USER}
     *     <br>Occurs when the provided id does not refer to a {@link User User}
     *     known by Discord. Typically occurs when developers provide an incomplete id (cut short).</li>
     * </ul>
     *
     * @param id The id of the requested {@link User}.
     * @return {@link CacheRestAction} - Type: {@link User}
     * <br>On request, gets the User with id matching provided id from Discord.
     */
    @Override
    public CacheRestAction<User> retrieveUserById(long id) {
        return null;
    }

    /**
     * {@link SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link Guild Guilds} visible to this JDA session.
     *
     * @return {@link SnowflakeCacheView SnowflakeCacheView}
     */
    @Override
    public SnowflakeCacheView<Guild> getGuildCache() {
        return null;
    }

    /**
     * Set of {@link Guild} IDs for guilds that were marked unavailable by the gateway.
     * <br>When a guild becomes unavailable a {@link GuildUnavailableEvent GuildUnavailableEvent}
     * is emitted and a {@link GuildAvailableEvent GuildAvailableEvent} is emitted
     * when it becomes available again. During the time a guild is unavailable it its not reachable through
     * cache such as {@link #getGuildById(long)}.
     *
     * @return Possibly-empty set of guild IDs for unavailable guilds
     */
    @Override
    public Set<String> getUnavailableGuilds() {
        return null;
    }

    /**
     * Whether the guild is unavailable. If this returns true, the guild id should be in {@link #getUnavailableGuilds()}.
     *
     * @param guildId The guild id
     * @return True, if this guild is unavailable
     */
    @Override
    public boolean isUnavailable(long guildId) {
        return false;
    }

    /**
     * Unified {@link SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link Role Roles} visible to this JDA session.
     *
     * @return Unified {@link SnowflakeCacheView SnowflakeCacheView}
     * @see CacheView#allSnowflakes(Supplier) CacheView.allSnowflakes(...)
     */
    @Override
    public SnowflakeCacheView<Role> getRoleCache() {
        return null;
    }

    /**
     * {@link SnowflakeCacheView} of
     * all cached {@link ScheduledEvent ScheduledEvents} visible to this JDA session.
     *
     * <p>This requires {@link CacheFlag#SCHEDULED_EVENTS} to be enabled.
     *
     * @return {@link SnowflakeCacheView}
     */
    @Override
    public SnowflakeCacheView<ScheduledEvent> getScheduledEventCache() {
        return null;
    }

    /**
     * {@link SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link PrivateChannel PrivateChannels} visible to this JDA session.
     *
     * @return {@link SnowflakeCacheView SnowflakeCacheView}
     */
    @Override
    public SnowflakeCacheView<PrivateChannel> getPrivateChannelCache() {
        return null;
    }

    /**
     * Opens a {@link PrivateChannel} with the provided user by id.
     * <br>This will fail with {@link ErrorResponse#UNKNOWN_USER UNKNOWN_USER}
     * if the user does not exist.
     *
     * <p>If the channel is cached, this will directly return the channel in a completed {@link RestAction} without making a request.
     * You can use {@link CacheRestAction#useCache(boolean) action.useCache(false)} to force an update.
     *
     * <p><b>Example</b><br>
     * <pre>{@code
     * public void sendMessage(JDA jda, long userId, String content) {
     *     jda.openPrivateChannelById(userId)
     *        .flatMap(channel -> channel.sendMessage(content))
     *        .queue();
     * }
     * }</pre>
     *
     * @param userId The id of the target user
     * @return {@link CacheRestAction} - Type: {@link PrivateChannel}
     * @throws UnsupportedOperationException If the target user is the currently logged in account
     * @see User#openPrivateChannel()
     */
    @Override
    public CacheRestAction<PrivateChannel> openPrivateChannelById(long userId) {
        return null;
    }

    /**
     * Unified {@link SnowflakeCacheView SnowflakeCacheView} of
     * all cached {@link RichCustomEmoji Custom Emojis} visible to this JDA session.
     *
     * @return Unified {@link SnowflakeCacheView SnowflakeCacheView}
     * @see CacheView#allSnowflakes(Supplier) CacheView.allSnowflakes(...)
     */
    @Override
    public SnowflakeCacheView<RichCustomEmoji> getEmojiCache() {
        return null;
    }

    /**
     * Attempts to retrieve a {@link Sticker} object based on the provided snowflake reference.
     * <br>This works for both {@link StandardSticker} and {@link GuildSticker}, and you can resolve them using the provided {@link StickerUnion}.
     *
     * <p>If the sticker is not one of the supported {@link Sticker.Type Types}, the request fails with {@link IllegalArgumentException}.
     *
     * <p>The returned {@link RestAction RestAction} can encounter the following Discord errors:
     * <ul>
     *     <li>{@link ErrorResponse#UNKNOWN_STICKER UNKNOWN_STICKER}
     *     <br>Occurs when the provided id does not refer to a sticker known by Discord.</li>
     * </ul>
     *
     * @param sticker The reference of the requested {@link Sticker}.
     *                <br>Can be {@link RichSticker}, {@link StickerItem}, or {@link Sticker#fromId(long)}.
     * @return {@link RestAction RestAction} - Type: {@link StickerUnion}
     * <br>On request, gets the sticker with id matching provided id from Discord.
     * @throws IllegalArgumentException If the provided sticker is null
     */
    @Override
    public RestAction<StickerUnion> retrieveSticker(StickerSnowflake sticker) {
        return null;
    }

    /**
     * Retrieves a list of all the public {@link StickerPack StickerPacks} used for nitro.
     *
     * @return {@link RestAction RestAction} - Type: List of {@link StickerPack}
     */
    @Override
    public RestAction<List<StickerPack>> retrieveNitroStickerPacks() {
        return null;
    }

    /**
     * The EventManager used by this JDA instance.
     *
     * @return The {@link IEventManager}
     */
    @Override
    public IEventManager getEventManager() {
        return null;
    }

    /**
     * Returns the currently logged in account represented by {@link SelfUser SelfUser}.
     * <br>Account settings <b>cannot</b> be modified using this object. If you wish to modify account settings please
     * use the AccountManager which is accessible by {@link SelfUser#getManager()}.
     *
     * @return The currently logged in account.
     */
    @Override
    public SelfUser getSelfUser() {
        return null;
    }

    /**
     * The {@link Presence Presence} controller for the current session.
     * <br>Used to set {@link Activity} and {@link OnlineStatus} information.
     *
     * @return The never-null {@link Presence Presence} for this session.
     */
    @Override
    public Presence getPresence() {
        return null;
    }

    /**
     * The shard information used when creating this instance of JDA.
     * <br>Represents the information provided to {@link JDABuilder#useSharding(int, int)}.
     *
     * @return The shard information for this shard
     */
    @Override
    public ShardInfo getShardInfo() {
        return null;
    }

    /**
     * The login token that is currently being used for Discord authentication.
     *
     * @return Never-null, 18 character length string containing the auth token.
     */
    @Override
    public String getToken() {
        return null;
    }

    /**
     * This value is the total amount of JSON responses that discord has sent.
     * <br>This value resets every time the websocket has to perform a full reconnect (not resume).
     *
     * @return Never-negative long containing total response amount.
     */
    @Override
    public long getResponseTotal() {
        return 0;
    }

    /**
     * This value is the maximum amount of time, in seconds, that JDA will wait between reconnect attempts.
     * <br>Can be set using {@link JDABuilder#setMaxReconnectDelay(int) JDABuilder.setMaxReconnectDelay(int)}.
     *
     * @return The maximum amount of time JDA will wait between reconnect attempts in seconds.
     */
    @Override
    public int getMaxReconnectDelay() {
        return 0;
    }

    /**
     * Sets whether or not JDA should try to automatically reconnect if a connection-error is encountered.
     * <br>This will use an incremental reconnect (timeouts are increased each time an attempt fails).
     *
     * <p>Default is <b>true</b>.
     *
     * @param reconnect If true - enables autoReconnect
     */
    @Override
    public void setAutoReconnect(boolean reconnect) {

    }

    /**
     * Whether the Requester should retry when
     * a {@link SocketTimeoutException SocketTimeoutException} occurs.
     *
     * @param retryOnTimeout True, if the Request should retry once on a socket timeout
     */
    @Override
    public void setRequestTimeoutRetry(boolean retryOnTimeout) {

    }

    /**
     * USed to determine whether or not autoReconnect is enabled for JDA.
     *
     * @return True if JDA will attempt to automatically reconnect when a connection-error is encountered.
     */
    @Override
    public boolean isAutoReconnect() {
        return false;
    }

    /**
     * Used to determine if JDA will process MESSAGE_DELETE_BULK messages received from Discord as a single
     * {@link MessageBulkDeleteEvent MessageBulkDeleteEvent} or split
     * the deleted messages up and fire multiple {@link MessageDeleteEvent MessageDeleteEvents},
     * one for each deleted message.
     *
     * <p>By default, JDA will separate the bulk delete event into individual delete events, but this isn't as efficient as
     * handling a single event would be. It is recommended that BulkDelete Splitting be disabled and that the developer
     * should instead handle the {@link MessageBulkDeleteEvent MessageBulkDeleteEvent}
     *
     * @return Whether or not JDA currently handles the BULK_MESSAGE_DELETE event by splitting it into individual MessageDeleteEvents or not.
     */
    @Override
    public boolean isBulkDeleteSplittingEnabled() {
        return false;
    }

    /**
     * Shuts down this JDA instance, closing all its connections.
     * After this command is issued the JDA Instance can not be used anymore.
     * Already enqueued {@link RestAction RestActions} are still going to be executed.
     *
     * <p>If you want this instance to shutdown without executing, use {@link #shutdownNow() shutdownNow()}
     *
     * <p>This will interrupt the default JDA event thread, due to the gateway connection being interrupted.
     *
     * @see #shutdownNow()
     */
    @Override
    public void shutdown() {

    }

    /**
     * Shuts down this JDA instance instantly, closing all its connections.
     * After this command is issued the JDA Instance can not be used anymore.
     * This will also cancel all queued {@link RestAction RestActions}.
     *
     * <p>If you want this instance to shutdown without cancelling enqueued RestActions use {@link #shutdown() shutdown()}
     *
     * <p>This will interrupt the default JDA event thread, due to the gateway connection being interrupted.
     *
     * @see #shutdown()
     */
    @Override
    public void shutdownNow() {

    }

    /**
     * Retrieves the {@link ApplicationInfo ApplicationInfo} for
     * the application that owns the logged in Bot-Account.
     * <br>This contains information about the owner of the currently logged in bot account!
     *
     * @return {@link RestAction RestAction} - Type: {@link ApplicationInfo ApplicationInfo}
     * <br>The {@link ApplicationInfo ApplicationInfo} of the bot's application.
     */
    @Override
    public RestAction<ApplicationInfo> retrieveApplicationInfo() {
        return null;
    }

    /**
     * Configures the required scopes applied to the {@link #getInviteUrl(Permission...)} and similar methods.
     * <br>To use slash commands you must add {@code "applications.commands"} to these scopes. The scope {@code "bot"} is always applied.
     *
     * @param scopes The scopes to use with {@link #getInviteUrl(Permission...)} and the likes
     * @return The current JDA instance
     * @throws IllegalArgumentException If null is provided
     */
    @Override
    public JDA setRequiredScopes(Collection<String> scopes) {
        return null;
    }

    /**
     * Creates an authorization invite url for the currently logged in Bot-Account.
     * <br>Example Format:
     * {@code https://discord.com/oauth2/authorize?scope=bot&client_id=288202953599221761&permissions=8}
     *
     * <p><b>Hint:</b> To enable a pre-selected Guild of choice append the parameter {@code &guild_id=YOUR_GUILD_ID}
     *
     * @param permissions The permissions to use in your invite, these can be changed by the link user.
     *                    <br>If no permissions are provided the {@code permissions} parameter is omitted
     * @return A valid OAuth2 invite url for the currently logged in Bot-Account
     */
    @Override
    public String getInviteUrl(Permission... permissions) {
        return null;
    }

    /**
     * Creates an authorization invite url for the currently logged in Bot-Account.
     * <br>Example Format:
     * {@code https://discord.com/oauth2/authorize?scope=bot&client_id=288202953599221761&permissions=8}
     *
     * <p><b>Hint:</b> To enable a pre-selected Guild of choice append the parameter {@code &guild_id=YOUR_GUILD_ID}
     *
     * @param permissions The permissions to use in your invite, these can be changed by the link user.
     *                    <br>If no permissions are provided the {@code permissions} parameter is omitted
     * @return A valid OAuth2 invite url for the currently logged in Bot-Account
     */
    @Override
    public String getInviteUrl(Collection<Permission> permissions) {
        return null;
    }

    /**
     * Returns the {@link ShardManager ShardManager} that manages this JDA instances or null if this instance is not managed
     * by any {@link ShardManager ShardManager}.
     *
     * @return The corresponding ShardManager or {@code null} if there is no such manager
     */
    @Override
    public ShardManager getShardManager() {
        return null;
    }

    /**
     * Retrieves a {@link Webhook Webhook} by its id.
     * <br>If the webhook does not belong to any known guild of this JDA session, it will be {@link Webhook#isPartial() partial}.
     *
     * <p>Possible {@link ErrorResponse ErrorResponses} caused by
     * the returned {@link RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>We do not have the required permissions</li>
     *
     *     <li>{@link ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>A webhook with this id does not exist</li>
     * </ul>
     *
     * @param webhookId The webhook id
     * @return {@link RestAction RestAction} - Type: {@link Webhook Webhook}
     * <br>The webhook object.
     * @throws IllegalArgumentException If the {@code webhookId} is null or empty
     * @see Guild#retrieveWebhooks()
     * @see TextChannel#retrieveWebhooks()
     */
    @Override
    public RestAction<Webhook> retrieveWebhookById(String webhookId) {
        return null;
    }

    /**
     * Sorted {@link SnowflakeCacheView SnowflakeCacheView} of {@link StageChannel}.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return {@link SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @Override
    public SnowflakeCacheView<StageChannel> getStageChannelCache() {
        return null;
    }

    /**
     * {@link SnowflakeCacheView SnowflakeCacheView} of {@link ThreadChannel}.
     *
     * <p>These threads can also represent posts in {@link ForumChannel ForumChannels}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return {@link SnowflakeCacheView SnowflakeCacheView}
     */
    @Override
    public SnowflakeCacheView<ThreadChannel> getThreadChannelCache() {
        return null;
    }

    /**
     * Sorted {@link SnowflakeCacheView SnowflakeCacheView} of {@link Category}.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return {@link SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @Override
    public SnowflakeCacheView<Category> getCategoryCache() {
        return null;
    }

    /**
     * Sorted {@link SnowflakeCacheView SnowflakeCacheView} of {@link TextChannel}.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return {@link SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @Override
    public SnowflakeCacheView<TextChannel> getTextChannelCache() {
        return null;
    }

    /**
     * Sorted {@link SnowflakeCacheView SnowflakeCacheView} of {@link NewsChannel}.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return {@link SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @Override
    public SnowflakeCacheView<NewsChannel> getNewsChannelCache() {
        return null;
    }

    /**
     * Sorted {@link SnowflakeCacheView SnowflakeCacheView} of {@link VoiceChannel}.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return {@link SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @Override
    public SnowflakeCacheView<VoiceChannel> getVoiceChannelCache() {
        return null;
    }

    /**
     * {@link SnowflakeCacheView SnowflakeCacheView} of {@link ForumChannel}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JDA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JDA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JDA} instance, you can use {@link JDA#awaitReady()}.
     *
     * @return {@link SnowflakeCacheView SnowflakeCacheView}
     */
    @Override
    public SnowflakeCacheView<ForumChannel> getForumChannelCache() {
        return null;
    }
}