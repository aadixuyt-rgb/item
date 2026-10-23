package pl.zrubmoda.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import pl.zrubmoda.config.AppearanceConfig;
import java.util.ArrayList;
import java.util.List;

public final class AppearanceScreen extends Screen {
    private TextFieldWidget search;
    private final List<Item> items = new ArrayList<>();
    private int scroll = 0;
    private Item selected;
    private String language = "PL";
    private ButtonWidget modeButton;
    private ButtonWidget languageButton;
    private static final int ROWS = 7;
    public AppearanceScreen() { super(Text.literal("Zrub Moda")); items.addAll(Registries.ITEM.stream().filter(i -> !i.getDefaultStack().isEmpty()).toList()); }
    @Override protected void init() {
        int left = width / 2 - 155, top = height / 2 - 105;
        search = new TextFieldWidget(textRenderer, left + 58, top, 195, 20, Text.literal("Search")); search.setChangedListener(s -> scroll = 0); addDrawableChild(search);
        languageButton = ButtonWidget.builder(Text.literal("Język: Polski"), b -> { language = language.equals("PL") ? "EN" : "PL"; b.setMessage(Text.literal(language.equals("PL") ? "Język: Polski" : "Language: English")); }).dimensions(left + 258, top, 52, 20).build(); addDrawableChild(languageButton);
        modeButton = ButtonWidget.builder(modeText(), b -> toggleMode()).dimensions(left, top + 32, 85, 20).build(); addDrawableChild(modeButton);
        addDrawableChild(ButtonWidget.builder(Text.literal("‹"), b -> scroll = Math.max(0, scroll - 1)).dimensions(left + 92, top + 168, 25, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("›"), b -> scroll++).dimensions(left + 280, top + 168, 25, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(language.equals("PL") ? "Wyczyść" : "Clear"), b -> { if (selected != null) { AppearanceConfig.ENTRIES.remove(Registries.ITEM.getId(selected).toString()); AppearanceConfig.save(); modeButton.setMessage(modeText()); } }).dimensions(left, top + 58, 85, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("×"), b -> close()).dimensions(left + 285, top + 194, 25, 20).build());
    }
    private Text modeText() {
        AppearanceConfig.Entry e = selected == null ? null : AppearanceConfig.entryFor(selected);
        boolean sword = e != null && "SWORD".equals(e.mode);
        return Text.literal((language.equals("PL") ? "Tryb: " : "Mode: ") + (sword ? (language.equals("PL") ? "Jak miecz" : "Sword-like") : (language.equals("PL") ? "Płasko" : "Flat")));
    }
    private void toggleMode() {
        if (selected == null) return;
        AppearanceConfig.Entry e = AppearanceConfig.entryFor(selected);
        if (e == null) e = new AppearanceConfig.Entry("minecraft:diamond", "FLAT");
        e.mode = "SWORD".equals(e.mode) ? "FLAT" : "SWORD";
        AppearanceConfig.ENTRIES.put(Registries.ITEM.getId(selected).toString(), e); AppearanceConfig.save(); modeButton.setMessage(modeText());
    }
    private List<Item> filtered() {
        String q = search == null ? "" : search.getText().toLowerCase(java.util.Locale.ROOT);
        return items.stream().filter(i -> { String id = Registries.ITEM.getId(i).toString(); String name = i.getName().getString().toLowerCase(java.util.Locale.ROOT); return id.contains(q) || name.contains(q); }).toList();
    }
    @Override public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        int left = width / 2 - 155, top = height / 2 - 105;
        ctx.fill(left - 5, top - 8, left + 315, top + 222, 0xE8202028);
        ctx.drawText(textRenderer, title, left, top - 24, 0xFFFFFF, true);
        super.render(ctx, mouseX, mouseY, delta);
        List<Item> filtered = filtered(); int start = Math.min(scroll * ROWS, Math.max(0, filtered.size() - 1));
        int visible = Math.min(ROWS, Math.max(0, filtered.size() - start));
        for (int n = 0; n < visible; n++) {
            Item item = filtered.get(start + n); int y = top + 31 + n * 19; boolean chosen = item == selected;
            ctx.fill(left + 92, y, left + 308, y + 18, chosen ? 0xFF555D80 : 0xFF303038);
            ctx.drawItem(new ItemStack(item), left + 94, y + 1);
            String label = item.getName().getString(); if (label.length() > 26) label = label.substring(0, 25) + "…";
            ctx.drawText(textRenderer, label, left + 114, y + 5, 0xFFFFFF, false);
            if (item == selected) ctx.drawText(textRenderer, "✓", left + 294, y + 5, 0x55FF55, false);
        }
        if (filtered.isEmpty()) ctx.drawText(textRenderer, language.equals("PL") ? "Brak wyników" : "No results", left + 120, top + 80, 0xAAAAAA, false);
    }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = width / 2 - 155, top = height / 2 - 105;
        if (mouseX >= left + 92 && mouseX <= left + 310 && mouseY >= top + 31 && mouseY < top + 31 + ROWS * 19) {
            int row = (int)((mouseY - (top + 31)) / 19); List<Item> filtered = filtered(); int index = scroll * ROWS + row;
            if (index >= 0 && index < filtered.size()) {
                selected = filtered.get(index);
                if (client != null && client.player != null) {
                    ItemStack held = client.player.getMainHandStack();
                    if (!held.isEmpty()) { AppearanceConfig.ENTRIES.put(Registries.ITEM.getId(held.getItem()).toString(), new AppearanceConfig.Entry(Registries.ITEM.getId(selected).toString(), "FLAT")); AppearanceConfig.save(); }
                }
                if (modeButton != null) modeButton.setMessage(modeText()); return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override public boolean shouldPause() { return false; }
}