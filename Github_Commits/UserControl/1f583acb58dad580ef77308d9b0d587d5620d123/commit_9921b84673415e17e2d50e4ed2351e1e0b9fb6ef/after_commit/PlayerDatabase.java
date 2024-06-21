package no.runsafe.UserControl.database;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.database.IRow;
import no.runsafe.framework.api.database.ISchemaUpdate;
import no.runsafe.framework.api.database.Repository;
import no.runsafe.framework.api.database.SchemaUpdate;
import no.runsafe.framework.api.event.IServerReady;
import no.runsafe.framework.api.hook.IPlayerDataProvider;
import no.runsafe.framework.api.hook.IPlayerSessionDataProvider;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.log.IDebug;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.timer.TimedCache;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public class PlayerDatabase extends Repository
	implements IPlayerDataProvider, IPlayerSessionDataProvider, IServerReady
{
	public PlayerDatabase(IDebug console, IScheduler scheduler, IConsole output, PlayerUsernameLog playerUsernameLog)
	{
		this.console = console;
		this.output = output;
		this.dataCache = new TimedCache<>(scheduler);
		this.playerUsernameLog = playerUsernameLog;
	}

	@Nonnull
	@Override
	public String getTableName()
	{
		return "player_db";
	}

	@Nonnull
	@Override
	public ISchemaUpdate getSchemaUpdateQueries()
	{
		ISchemaUpdate update = new SchemaUpdate();

		update.addQueries(
			"CREATE TABLE player_db (" +
				"`name` varchar(255) NOT NULL," +
				"`joined` datetime NOT NULL," +
				"`login` datetime NOT NULL," +
				"`logout` datetime NULL," +
				"`banned` datetime NULL," +
				"`ban_reason` varchar(255) NULL," +
				"`ban_by` varchar(255) NULL," +
				"`ip` int unsigned NULL," +
				"PRIMARY KEY(`name`)" +
			")"
		);

		update.addQueries("ALTER TABLE player_db ADD COLUMN temp_ban datetime NULL");
		update.addQueries("ALTER TABLE player_db ADD COLUMN uuid VARCHAR(255) NULL");

		update.addQueries(
			// Add console
			"INSERT INTO `" + getTableName() + "` " +
				"(`name`, `joined`, `login`, `logout`, `banned`, `ban_reason`, `ban_by`, `ip`, `temp_ban`, `uuid`) " +
				"VALUES ('console', '1970-01-01', '1970-01-01', '1970-01-01', NULL, NULL, NULL, NULL, NULL, '" + playerUsernameLog.consoleUUID +"');",
			// Add column for banningPlayer UUID
			"ALTER TABLE player_db ADD COLUMN ban_by_uuid VARCHAR(36) NULL;",
			// Convert banning console to UUID
			"UPDATE IGNORE `player_db` SET `ban_by_uuid` = '" + playerUsernameLog.consoleUUID + "' " +
				"WHERE `ban_by` = 'console' OR (`banned` IS NOT NULL AND `ban_by` IS NULL);",
			// Convert banningPlayer usernames to UUIDs
			"UPDATE IGNORE `player_db` SET `ban_by_uuid` = " +
				"(SELECT `uuid` FROM `player_username_log` WHERE `name`=`player_db`.`ban_by` LIMIT 1) " +
				"WHERE `ban_by` IS NOT NULL;",

			"ALTER TABLE player_db RENAME TO player_db_old;",
			// Create new table based on player uuids instead of usernames.
			"CREATE TABLE `" + getTableName() + "` (" +
				"`uuid` varchar(36) NOT NULL," +
				"`name` varchar(36) NOT NULL," +
				"`joined` datetime NOT NULL," +
				"`login` datetime NOT NULL," +
				"`logout` datetime NULL," +
				"`banned` datetime NULL," +
				"`temp_ban` VARCHAR(255) NULL," +
				"`ban_reason` varchar(255) NULL," +
				"`ban_by` varchar(36) NULL," +
				"`ip` int unsigned NULL," +
				"PRIMARY KEY(`uuid`)" +
			");",
			// Migrate to new table ignoring duplicates.
			"INSERT IGNORE INTO `" + getTableName() + "` " +
				"(`uuid`, `name`, `joined`, `login`, `logout`, `banned`, `temp_ban`, `ban_reason`, `ban_by`, `ip`) " +
				"SELECT `uuid`, `name`, `joined`, `login`, `logout`, `banned`, `temp_ban`, `ban_reason`, `ban_by_uuid`, `ip` " +
				"FROM `player_db_old` WHERE `uuid` IS NOT NULL;"
		);

		return update;
	}

	@Override
	public void OnServerReady()
	{
		// Clean up invalid state after a server failure
		database.update("UPDATE player_db SET `logout`=NOW() WHERE logout < login");
	}

	public void logPlayerInfo(IPlayer player)
	{
		console.debugFine("Updating player_db with login time");
		database.update(
			"INSERT INTO player_db (`uuid`,`name`,`joined`,`login`,`ip`) VALUES (?,?,NOW(),NOW(),INET_ATON(?))" +
				"ON DUPLICATE KEY UPDATE `uuid`=VALUES(`uuid`), `name`=VALUES(`name`), `login`=VALUES(`login`), `ip`=VALUES(`ip`)",
			player, player.getName(), player.getIP()
		);
		dataCache.Invalidate(player);
		playerUsernameLog.purgeLookupCache();
	}

	public void logPlayerBan(IPlayer player, IPlayer banningPlayer, String reason)
	{
		database.update(
			"UPDATE player_db SET `banned`=NOW(), ban_reason=?, ban_by=? WHERE `uuid`=?",
			reason, banningPlayer == null ? playerUsernameLog.consoleUUID.toString() : banningPlayer.getUniqueId().toString(), player
		);
		dataCache.Invalidate(player);
	}

	public void setPlayerTemporaryBan(IPlayer player, Instant temporary)
	{
		database.update("UPDATE player_db SET temp_ban=? WHERE `uuid`=?", temporary, player);
		dataCache.Invalidate(player);
	}

	public void logPlayerUnban(IPlayer player)
	{
		database.update(
			"UPDATE player_db SET `banned`=NULL, ban_reason=NULL, ban_by=NULL, temp_ban=NULL WHERE `uuid`=?",
			player
		);
		dataCache.Invalidate(player);
	}

	public void logPlayerLogout(IPlayer player)
	{
		database.update(
			"UPDATE player_db SET `logout`=NOW() WHERE `uuid`=?",
			player
		);
		dataCache.Invalidate(player);
	}

	public PlayerData getData(IPlayer player)
	{
		PlayerData data = dataCache.Cache(player);
		if (data != null)
			return data;

		IRow raw = database.queryRow("SELECT * FROM player_db WHERE `uuid`=?", player.getUniqueId().toString());
		if (raw.isEmpty())
			output.logInformation("New player %s with UUID %s discovered!", player.getName(), player.getUniqueId().toString());
		else if (!player.getUniqueId().toString().equalsIgnoreCase(raw.String("uuid")))
			output.logInformation("Player %s with UUID %s changed their username!", player.getName(), raw.String("uuid"));
		data = new PlayerData();
		data.setBanned(raw.Instant("banned"));
		if (raw.String("ban_by") != null)
			data.setBanningPlayer(UUID.fromString(raw.String("ban_by")));
		else
			data.setBanningPlayer(null);
		data.setBanReason(raw.String("ban_reason"));
		data.setJoined(raw.Instant("joined"));
		data.setLogin(raw.Instant("login"));
		data.setLogout(raw.Instant("logout"));
		data.setUnban(raw.Instant("temp_ban"));

		return dataCache.Cache(player, data);
	}

	@Override
	public HashMap<String, String> GetPlayerData(IPlayer player)
	{
		PlayerData data = getData(player);
		HashMap<String, String> result = new LinkedHashMap<>();
		if (data.getBanned() != null)
		{
			result.put("usercontrol.ban.status", "true");
			result.put("usercontrol.ban.timestamp", formatDate(data.getBanned()));
			result.put("usercontrol.ban.reason", data.getBanReason());
			if (data.getUnban() != null)
				result.put("usercontrol.ban.temporary", formatDate(data.getUnban()));
			result.put("usercontrol.ban.by", playerUsernameLog.getLatestUsername(data.getBanningPlayerUUID()));
		}
		else
			result.put("usercontrol.ban.status", "false");
		result.put("usercontrol.joined", formatDate(data.getJoined()));
		result.put("usercontrol.login", formatDate(data.getLogin()));
		result.put("usercontrol.logout", formatDate(data.getLogout()));
		result.put("usercontrol.pastNames", StringUtils.join(playerUsernameLog.getUsedUsernames(player.getUniqueId()), ", "));
		if (data.getLogout() != null && data.getLogout().isAfter(data.getLogin()))
			result.put("usercontrol.seen", DurationFormatUtils.formatDurationWords(
				Duration.between(data.getLogout(), Instant.now()).toMillis(), true, true)
			);

		return result;
	}

	private String formatDate(Instant date)
	{
		if (date == null)
			return "Invalid Date";

		return date.atZone(ZoneId.systemDefault()).toString().replace("T", " ").substring(0,16);
	}

	@Override
	public DateTime GetPlayerLogout(IPlayer player)
	{
		if (player == null)
			return null;

		PlayerData data = getData(player);
		if (data == null || data.getLogout() == null)
			return null;

		return new DateTime(data.getLogout().getEpochSecond());
	}

	@Override
	public String GetPlayerBanReason(IPlayer player)
	{
		return getData(player).getBanReason();
	}

	@Override
	public boolean IsFirstSession(IPlayer player)
	{
		return GetPlayerLogout(player) == null;
	}

	private final IConsole output;
	private final IDebug console;
	private final PlayerUsernameLog playerUsernameLog;
	private final TimedCache<IPlayer, PlayerData> dataCache;
}
