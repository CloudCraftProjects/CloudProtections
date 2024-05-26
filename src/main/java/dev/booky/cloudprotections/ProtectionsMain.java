package dev.booky.cloudprotections;
// Created by booky10 in CloudCore (10:35 14.03.23)

import dev.booky.cloudcore.i18n.CloudTranslator;
import dev.booky.cloudprotections.commands.ProtectionsCommand;
import dev.booky.cloudprotections.listener.ProtectionListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public class ProtectionsMain extends JavaPlugin {

    private ProtectionsManager manager;
    private CloudTranslator i18n;

    @Override
    public void onLoad() {
        this.manager = new ProtectionsManager(this);
        new Metrics(this, 18100);

        this.i18n = new CloudTranslator(this.getClassLoader(),
                new NamespacedKey(this, "i18n"),
                Locale.GERMAN, Locale.ENGLISH);
        this.i18n.load();

        Bukkit.getServicesManager().register(ProtectionsManager.class, this.manager, this, ServicePriority.Normal);
    }

    @Override
    public void onEnable() {
        this.manager.reloadRegions();

        Bukkit.getPluginManager().registerEvents(new ProtectionListener(this.manager), this);

        ProtectionsCommand.create(this.manager);
    }

    @Override
    public void onDisable() {
        if (this.manager != null) {
            this.manager.saveRegions();
        }

        if (this.i18n != null) {
            this.i18n.unload();
        }
    }
}
