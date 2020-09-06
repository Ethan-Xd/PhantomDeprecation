package me.gamingti.phantomdeprecation

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.Repairable
import org.bukkit.plugin.java.JavaPlugin
import kotlin.math.ceil

class PhantomDeprecation : JavaPlugin(), Listener {
    // How many damage points items should be repaired by
    var fixStep = 108
    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
    }

    @EventHandler
    fun onPrepareAnvil(e: PrepareAnvilEvent) {
        val anvil = e.inventory as AnvilInventory
        if (!anvilOutputCondition(anvil)) return
        val elytra = anvil.getItem(0)
        val elytraMeta = elytra.itemMeta
        val elytraMetaR = elytraMeta as Repairable
        val elytraMetaD = elytraMeta as Damageable
        val leather = anvil.getItem(1)
        var leatherUsing = leather.amount
        val maximumLeatherToUse = ceil(elytraMetaD.damage / fixStep.toDouble()).toInt() + 1
        if (leatherUsing > maximumLeatherToUse) leatherUsing = maximumLeatherToUse
        val newElytra = getRepairedElytra(elytra, leatherUsing, anvil.renameText)

        // Display XP cost
        var xpCost = elytraMetaR.repairCost + leatherUsing
        if (anvil.renameText !== elytraMeta.displayName && !anvil.renameText.isEmpty()) xpCost += 1
        anvil.repairCost = xpCost
        e.result = newElytra
        //((Player)e.getViewers().get(0)).updateInventory();

        // TODO: Fix all leather being used when repairing elytra if stack has overpay (#2)
    }

    // Return the repaired elytra ItemStack
    private fun getRepairedElytra(elytra: ItemStack, leatherAmount: Int, renameText: String): ItemStack {
        val elytraMeta = elytra.itemMeta
        val elytraMetaR = elytraMeta as Repairable
        val elytraMetaD = elytraMeta as Damageable
        var newDamage = elytraMetaD.damage - leatherAmount * fixStep
        if (newDamage < 0) newDamage = 0
        val repairedElytra = ItemStack(elytra)

        // Set new damage
        elytraMetaD.damage = newDamage
        repairedElytra.itemMeta = elytraMetaD as ItemMeta

        // Set new repair cost
        elytraMetaR.repairCost = elytraMetaR.repairCost * 2 + 1
        repairedElytra.itemMeta = elytraMetaR as ItemMeta

        // If changed name, change it
        if (renameText !== repairedElytra.itemMeta.displayName && !renameText.isEmpty()) {
            val elytraDisplayName = repairedElytra.itemMeta
            elytraDisplayName.displayName = renameText
            repairedElytra.itemMeta = elytraDisplayName
        }
        return repairedElytra
    }

    // Should the anvil output a new elytra?
    private fun anvilOutputCondition(anvil: AnvilInventory): Boolean {
        // Is any slot empty?
        if (anvil.getItem(0) == null || anvil.getItem(1) == null) return false

        // Is the first slot an elytra?
        if (anvil.getItem(0).type != Material.ELYTRA) return false

        // Is the second slot leather?
        if (anvil.getItem(1).type != Material.LEATHER) return false

        // Is the elytra fully healed?
        return (anvil.getItem(0).itemMeta as Damageable).damage != 0

        // TODO: Fix visual glitch when PrepareAnvilEvent is fired and output remains identical to previous result (#1)
        // Should be possible within this function, if not then put in onPrepareAnvil
    }
}