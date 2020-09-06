package me.gamingti.phantomdeprecation;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.plugin.java.JavaPlugin;

public final class PhantomDeprecation extends JavaPlugin implements Listener {

	// How many damage points items should be repaired by
	int fixStep = 108;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {

	}

	// On click see if we get the item
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player)) return;
		Player player = (Player) e.getWhoClicked();

		int eventSlot = e.getRawSlot();

		return;

		/*if (e.getView().getType() == InventoryType.ANVIL) {
			if (eventSlot != 2) return; // If not the output slot

			AnvilInventory anvil = (AnvilInventory) e.getInventory();

			if (!anvilOutputCondition(anvil)) return;

			ItemStack elytra = anvil.getItem(0);
			ItemMeta elytraMeta = elytra.getItemMeta();
			Repairable elytraMetaR = (Repairable) elytraMeta;
			Damageable elytraMetaD = (Damageable) elytraMeta;

			ItemStack leather = anvil.getItem(1);
			int leatherUsing = leather.getAmount();
			int maximumLeatherToUse = (int) Math.ceil(elytraMetaD.getDamage() / fixStep) + 1;
			if (leatherUsing > maximumLeatherToUse) leatherUsing = maximumLeatherToUse;



		}*/

	}

	@EventHandler
	public void onPrepareAnvil(PrepareAnvilEvent e) {
		AnvilInventory anvil = (AnvilInventory) e.getInventory();

		if (!anvilOutputCondition(anvil)) return;

		ItemStack elytra = anvil.getItem(0);
		ItemMeta elytraMeta = elytra.getItemMeta();
		Repairable elytraMetaR = (Repairable) elytraMeta;
		Damageable elytraMetaD = (Damageable) elytraMeta;

		ItemStack leather = anvil.getItem(1);
		int leatherUsing = leather.getAmount();
		int maximumLeatherToUse = (int) Math.ceil(elytraMetaD.getDamage() / fixStep) + 1;
		if (leatherUsing > maximumLeatherToUse) leatherUsing = maximumLeatherToUse;

		ItemStack newElytra = getRepairedElytra(elytra, leatherUsing, anvil.getRenameText());

		// Display XP cost
		int xpCost = elytraMetaR.getRepairCost() + leatherUsing;
		if (anvil.getRenameText() != elytraMeta.getDisplayName() && !anvil.getRenameText().isEmpty()) xpCost += 1;
		anvil.setRepairCost(xpCost);

		e.setResult(newElytra);
		//((Player)e.getViewers().get(0)).updateInventory();

		// TODO: Fix all leather being used if overpaying
	}

	// Return the repaired elytra ItemStack
	private ItemStack getRepairedElytra(ItemStack elytra, int leatherAmount, String renameText) {
		ItemMeta elytraMeta = elytra.getItemMeta();
		Repairable elytraMetaR = (Repairable) elytraMeta;
		Damageable elytraMetaD = (Damageable) elytraMeta;

		int newDamage = elytraMetaD.getDamage() - (leatherAmount * fixStep);
		if (newDamage < 0) newDamage = 0;

		ItemStack repairedElytra = new ItemStack(elytra);

		// Set new damage
		elytraMetaD.setDamage(newDamage);
		repairedElytra.setItemMeta((ItemMeta)elytraMetaD);

		// Set new repair cost
		elytraMetaR.setRepairCost((elytraMetaR.getRepairCost() * 2) + 1);
		repairedElytra.setItemMeta((ItemMeta)elytraMetaR);

		// If changed name, change it
		if (renameText != repairedElytra.getItemMeta().getDisplayName() && !renameText.isEmpty()) {
			ItemMeta elytraDisplayName = repairedElytra.getItemMeta();
			elytraDisplayName.setDisplayName(renameText);
			repairedElytra.setItemMeta(elytraDisplayName);
		}

		return repairedElytra;
	}

	// Should the anvil output a new elytra?
	private boolean anvilOutputCondition(AnvilInventory anvil) {
		// Is any slot empty?
		if (anvil.getItem(0) == null || anvil.getItem(1) == null) return false;

		// Is the first slot an elytra?
		if (anvil.getItem(0).getType() != Material.ELYTRA) return false;

		// Is the second slot leather?
		if (anvil.getItem(1).getType() != Material.LEATHER) return false;

		// Is the elytra fully healed?
		if (((Damageable)anvil.getItem(0).getItemMeta()).getDamage() == 0) return false;

		// TODO: Fix visual glitch when PrepareAnvilEvent is fired and output remains identical to previous result

		return true;
	}

}
