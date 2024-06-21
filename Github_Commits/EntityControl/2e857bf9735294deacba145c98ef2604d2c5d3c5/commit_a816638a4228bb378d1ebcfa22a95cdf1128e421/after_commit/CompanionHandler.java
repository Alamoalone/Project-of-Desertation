package no.runsafe.entitycontrol.pets;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.entity.IEntity;
import no.runsafe.framework.api.entity.ILivingEntity;
import no.runsafe.framework.api.event.entity.IEntityDamageEvent;
import no.runsafe.framework.api.event.player.IPlayerChangedWorldEvent;
import no.runsafe.framework.api.event.player.IPlayerDeathEvent;
import no.runsafe.framework.api.event.player.IPlayerInteractEntityEvent;
import no.runsafe.framework.api.event.player.IPlayerQuitEvent;
import no.runsafe.framework.api.event.player.IPlayerRightClick;
import no.runsafe.framework.api.event.plugin.IPluginDisabled;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.event.entity.RunsafeEntityDamageEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerChangedWorldEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerDeathEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEntityEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerQuitEvent;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import no.runsafe.framework.minecraft.Sound;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CompanionHandler
	implements IPluginDisabled, IPlayerRightClick, IPlayerChangedWorldEvent,
	IPlayerQuitEvent, IPlayerInteractEntityEvent, IEntityDamageEvent,
	IPlayerDeathEvent
{
	/**
	 * Constructor for CompanionHandler.
	 * @param console The console.
	 * @param server The server.
	 */
	public CompanionHandler(IConsole console, IServer server, IScheduler scheduler)
	{
		this.console = console;
		CompanionHandler.server = server;
		this.scheduler = scheduler;
	}

	/**
	 * Attempt to spawn a companion.
	 * @param type Companion type to spawn.
	 * @param follower Player the companion will follow.
	 */
	public void spawnCompanion(CompanionType type, @Nonnull IPlayer follower)
	{
		try
		{
			ILivingEntity pet = type.spawnCompanion(follower);

			UUID playerUUID = follower.getUniqueId();

			if (!summonedPets.containsKey(playerUUID))
				summonedPets.put(playerUUID, new ArrayList<SummonedPet>(1));

			summonedPets.get(playerUUID).add(new SummonedPet(type, pet));
		}
		catch (Exception e)
		{
			console.logException(e);
		}
	}

	/**
	 * Detect when a player tries to summon/remove a companion.
	 * @param player Companion owner.
	 * @param usingItem Item the player is holding.
	 * @param targetBlock Targeted block. Influences nothing.
	 * @return True when player isn't holding a companion egg.
	 */
	@Override
	public boolean OnPlayerRightClick(IPlayer player, RunsafeMeta usingItem, IBlock targetBlock)
	{
		//Check if player is holding a monster egg
		if (usingItem == null || !usingItem.is(Item.Miscellaneous.MonsterEgg.Any))
			return true;

		//Check if monster egg has lore set
		List<String> lore = usingItem.getLore();
		if (lore == null || lore.isEmpty())
			return true;

		String petString = null;
		for (String loreString : lore)
		{
			if (!loreString.startsWith("§7Pet: "))
				continue;

			petString = loreString;
			break;
		}

		if (petString == null)
			return false;

		//Get the companion's type
		CompanionType type = null;
		for (CompanionType companionType : CompanionType.values())
		{
			if (!petString.endsWith(companionType.getTitle()))
				continue;

			type = companionType;
			break;
		}

		//Summon or remove companion
		if (type == null)
			return false;
		SummonedPet summonedPet = getPlayerSummoned(player, type);

		if (summonedPet == null)
			spawnCompanion(type, player);
		else
			removeSummonedPet(player, summonedPet);

		return false;
	}

	/**
	 * Gets companion pet the player has summoned if they have summoned one.
	 * @param player Companion pet owner.
	 * @param type Companion pet type.
	 * @return Companion pet. Returns null if not summoned yet.
	 */
	public SummonedPet getPlayerSummoned(IPlayer player, CompanionType type)
	{
		UUID playerUUID = player.getUniqueId();
		if (!summonedPets.containsKey(playerUUID))
			return null;

		for (SummonedPet summonedPet : summonedPets.get(playerUUID))
			if (summonedPet.getType() == type)
				return summonedPet;

		return null;
	}

	/**
	 * Find whether or not an entity is summoned.
	 * @param entityId Entity to find if summoned.
	 * @return True if entity is summoned, otherwise false.
	 */
	public static boolean entityIsSummoned(int entityId)
	{
		if (summonedPets.isEmpty())
			return false;

		for (Map.Entry<UUID, List<SummonedPet>> node : summonedPets.entrySet())
			for (SummonedPet pet : node.getValue())
				if (entityId == pet.getEntityID())
					return true;

		return false;
	}

	/**
	 * Removes a player's pet.
	 * @param player Pet owner.
	 * @param pet Object do delete.
	 */
	public void removeSummonedPet(IPlayer player, SummonedPet pet)
	{
		pet.getPet().remove();
		summonedPets.get(player.getUniqueId()).remove(pet);
	}

	/**
	 * Removes all of a player's summoned companion pets.
	 * @param player Pet owner.
	 */
	public void removeSummonedPets(IPlayer player)
	{
		if (summonedPets.get(player.getUniqueId()) == null)
			return;

		for (SummonedPet pet : summonedPets.get(player.getUniqueId()))
			pet.getPet().remove();
		summonedPets.remove(player.getUniqueId());
	}

	/**
	 * Triggered when player changes world.
	 * Kill player's companion pets.
	 * @param event Event that happens when a player changes worlds.
	 */
	@Override
	public void OnPlayerChangedWorld(RunsafePlayerChangedWorldEvent event)
	{
		removeSummonedPets(event.getPlayer());
	}

	/**
	 * Kill player's companion pets when they log out.
	 * @param event Player log out event.
	 */
	@Override
	public void OnPlayerQuit(RunsafePlayerQuitEvent event)
	{
		removeSummonedPets(event.getPlayer());
	}

	@Override
	public void OnPlayerDeathEvent(RunsafePlayerDeathEvent event)
	{
		removeSummonedPets(event.getEntity());
	}

	@Override
	public void OnPlayerInteractEntityEvent(RunsafePlayerInteractEntityEvent event)
	{
		IEntity runsafePet = event.getRightClicked();
		if (interactTimer.containsKey(runsafePet))
		{
			event.cancel();
			return;
		}

		// Check if right clicked entity is a companion pet.
		if (!entityIsSummoned(runsafePet.getEntityId()))
			return;
		ILivingEntity pet = (ILivingEntity) runsafePet;

		// Play the companion's idle sound.
		Sound interactSound = pet.getIdleSound();
		if (interactSound != null)
		{
			interactSound.Play(runsafePet.getLocation(), 1.0F, 1.5F);
			interactTimer.put(runsafePet, scheduler.startSyncTask(() ->
			{
				if (interactTimer.containsKey(runsafePet))
					interactTimer.remove(runsafePet);
			}, 2));
		}

		event.cancel();
	}

	@Override
	public void OnEntityDamage(RunsafeEntityDamageEvent event)
	{
		if (entityIsSummoned(event.getEntity().getEntityId()))
			event.cancel();
	}

	@Override
	public void OnPluginDisabled()
	{
		for (Map.Entry<UUID, List<SummonedPet>> node : summonedPets.entrySet())
			for (SummonedPet pet : summonedPets.get(node.getKey()))
				pet.getPet().remove();
		summonedPets = null;
	}

	private final IScheduler scheduler;
	private final IConsole console;
	public static IServer server;
	private static final ConcurrentHashMap<IEntity, Integer> interactTimer = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<UUID, List<SummonedPet>> summonedPets = new ConcurrentHashMap<UUID, List<SummonedPet>>(0);
	//public static ConcurrentHashMap<String, List<CompanionType>> summonedPets = new ConcurrentHashMap<String, List<CompanionType>>(0);
	//public static ConcurrentHashMap<String, List<Integer>> summonedPetIds = new ConcurrentHashMap<String, List<Integer>>(0);
}
