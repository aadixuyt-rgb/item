package pl.zrubmoda.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Client-only mapping from original item id to visual item id and display transform. */
public final class AppearanceConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("zrub-moda.json");
    public static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();
    public static boolean polish = true;
    private AppearanceConfig() {}
    public static final class Entry {
        public String target = "minecraft:diamond";
        public String mode = "FLAT";
        public Entry() {}
        public Entry(String target, String mode) { this.target = target; this.mode = mode; }
    }
    private static final class Data { boolean polish = true; Map<String, Entry> entries = new LinkedHashMap<>(); }
    public static synchronized void load() {
        try {
            if (Files.exists(FILE)) try (Reader r = Files.newBufferedReader(FILE)) { Data d = GSON.fromJson(r, Data.class); if (d != null) { polish = d.polish; ENTRIES.clear(); if (d.entries != null) ENTRIES.putAll(d.entries); } }
        } catch (Exception e) { System.err.println("[Zrub Moda] Config load failed: " + e.getMessage()); }
    }
    public static synchronized void save() {
        try { Files.createDirectories(FILE.getParent()); Data d = new Data(); d.polish = polish; d.entries = new LinkedHashMap<>(ENTRIES); try (Writer w = Files.newBufferedWriter(FILE)) { GSON.toJson(d, w); } }
        catch (Exception e) { System.err.println("[Zrub Moda] Config save failed: " + e.getMessage()); }
    }
    public static Item targetFor(Item original) {
        Entry e = ENTRIES.get(Registries.ITEM.getId(original).toString()); if (e == null || e.target == null) return original;
        try { Item item = Registries.ITEM.get(Identifier.of(e.target)); return item == Items.AIR ? original : item; } catch (Exception ignored) { return original; }
    }
    public static Entry entryFor(Item original) { return ENTRIES.get(Registries.ITEM.getId(original).toString()); }
}