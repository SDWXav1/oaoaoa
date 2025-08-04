spackage com.example.killrewardpunish;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class KillRewardPunish extends JavaPlugin implements Listener {

    private final Map<UUID, Integer> deathPenalties = new HashMap<>();
    private final Map<UUID, Integer> killBonuses = new HashMap<>();
    private final Random random = new Random();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("KillRewardPunish enabled!");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        UUID uuid = dead.getUniqueId();
        int stack = deathPenalties.getOrDefault(uuid, 0);
        deathPenalties.put(uuid, stack + 1);

        int effect = random.nextInt(3);
        switch (effect) {
            case 0:
                dead.sendMessage(ChatColor.RED + "You feel slower...");
                dead.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 999999, stack));
                break;
            case 1:
                dead.sendMessage(ChatColor.RED + "You feel weaker...");
                dead.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 999999, stack));
                break;
            case 2:
                dead.sendMessage(ChatColor.DARK_RED + "You are unlucky... beware of mobs!");
                // Simulated unlucky flag — mobs would spawn via a task or be targeted more.
                break;
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getEntity().getKiller() == null) return;

        Player killer = event.getEntity().getKiller();
        UUID uuid = killer.getUniqueId();

        int bonus = killBonuses.getOrDefault(uuid, 0);
        killBonuses.put(uuid, bonus + 1);

        // Cure 1 death effect if any
        if (deathPenalties.containsKey(uuid)) {
            int newPenalty = deathPenalties.get(uuid) - 1;
            if (newPenalty <= 0) {
                deathPenalties.remove(uuid);
                killer.sendMessage(ChatColor.GREEN + "You've cured your punishment!");
                killer.removePotionEffect(PotionEffectType.SLOWNESS);
                killer.removePotionEffect(PotionEffectType.WEAKNESS);
            } else {
                deathPenalties.put(uuid, newPenalty);
                killer.sendMessage(ChatColor.YELLOW + "One punishment cured. Remaining: " + newPenalty);
            }
        }

        // Give random buff
        int reward = new Random().nextInt(3);
        switch (reward) {
            case 0:
                killer.sendMessage(ChatColor.GREEN + "You feel powerful!");
                killer.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, bonus));
                break;
            case 1:
                killer.sendMessage(ChatColor.AQUA + "You feel healthier!");
                killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, bonus));
                break;
            case 2:
                killer.sendMessage(ChatColor.LIGHT_PURPLE + "You feel faster at everything!");
                killer.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, bonus));
                break;
        }
    }
}
