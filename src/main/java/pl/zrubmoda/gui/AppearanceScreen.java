package pl.zrubmoda.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import pl.zrubmoda.config.AppearanceConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AppearanceScreen extends Screen {
    private TextFieldWidget search;
    private final List<Item> items = new ArrayList<>();
    private int scroll;
    private Item selectedTarget;
    private Item sourceItem;
    private String language = "PL";
    private ButtonWidget languageButton;
    private ButtonWidget modeButton;
    private ButtonWidget clearButton;
    private static final int ROWS = 7;

    public AppearanceScreen() {
        super(Text.literal("Zrub Moda"));
        items.addAll(Registries.ITEM.stream().filter(i -> !i.getDefaultStack().isEmpty()).toList());
    }

    @Override protected void init() {
        int left = width / 2 - 155, top = height / 2 - 105;
        if (client != null && client.player != null && !client.player.getMainHandStack().isEmpty()) {
            sourceItem = client.player.getMainHandStack().getItem();
            AppearanceConfig.Entry entry = AppearanceConfig.entryFor(sourceItem);
            if (entry != null) {
                try { selectedTarget = Registries.ITEM.get(net.minecraft.util.Identifier.of(entry.target)); }
                catch (Exception ignored) { selectedTarget = null; }
            }
        }
        search = new TextFieldWidget(textRenderer, left + 58, top, 195, 20, Text.literal("Search"));
        search.setChangedListener(s -> scroll = 0);
        addDrawableChild(search);
        languageButton = ButtonWidget.builder(languageText(), b -> {
            language = language.equals("PL") ? "EN" : "PL";
            b.setMessage(languageText());
            if (modeButton != null) modeButton.setMessage(modeText());
            if (clearButton != null) clearButton.setMessage(Text.literal(language.equals("PL") ? "Wyczyść" : "Clear"));
        }).dimensions(left + 258, top, 52, 20).build();
        addDrawableChild(languageButton);
        modeButton = ButtonWidget.builder(modeText(), b -> toggleMode()).dimensions(left, top + 32, 85, 20).build();
        addDrawableChild(modeButton);
        clearButton = ButtonWidget.builder(Text.literal(language.equals("PL") ? "Wyczyść" : "Clear"), b -> clearMapping())
            .dimensions(left, top + 58, 85, 20).build();
        addDrawableChild(clearButton);
        addDrawableChild(ButtonWidget.builder(Text.literal("‹"), b -> scroll = Math.max(0, scroll - 1))
            .dimensions(left + 92, top + 168, 25, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("›"), b -> {
            int max = Math.max(0, (filtered().size() - 1) / ROWS);
            scroll = Math.min(max, scroll + 1);
        }).dimensions(left + 280, top + 168, 25, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("×"), b -> close()).dimensions(left + 285, top + 194, 25, 20).build());
    }

    private Text languageText() {
        return Text.literal(language.equals("PL") ? "Język: Polski" : "Language: English");
    }

    private Text modeText() {
        AppearanceConfig.Entry e = sourceItem == null ? null : AppearanceConfig.entryFor(sourceItem);
        boolean sword = e != null && "SWORD".equals(e.mode);
        String mode = sword ? (language.equals("PL") ? "Jak miecz" : "Sword-like")
                            : (language.equals("PL") ? "Płasko" : "Flat");
        return Text.literal((language.equals("PL") ? "Tryb: " : "Mode: ") + mode);
    }

    private void toggleMode() {
        if (sourceItem == null) return;
        AppearanceConfig.Entry e = AppearanceConfig.entryFor(sourceItem);
        if (e == null) {
            if (selectedTarget == null) return;
            e = new AppearanceConfig.Entry(Registries.ITEM.getId(selectedTarget).toString(), "FLAT");
        }
        e.mode = "SWORD".equals(e.mode) ? "FLAT" : "SWORD";
        AppearanceConfig.ENTRIES.put(Registries.ITEM.getId(sourceItem).toString(), e);
        AppearanceConfig.save();
        if (modeButton != null) modeButton.setMessage(modeText());
    }

    private void clearMapping() {
        if (sourceItem == null) return;
        AppearanceConfig.ENTRIES.remove(Registries.ITEM.getId(sourceItem).toString());
        AppearanceConfig.save();
        if (modeButton != null) modeButton.setMessage(modeText());
    }

    private List<Item> filtered() {
        String q = search == null ? "" : search.getText().toLowerCase(Locale.ROOT);
        return items.stream().filter(i -> {
            String id = Registries.ITEM.getId(i).toString().toLowerCase(Locale.ROOT);
            String name = i.getName().getString().toLowerCase(Locale.ROOT);
            return id.contains(q) || name.contains(q);
        }).toList();
    }

    @Override public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        int left = width / 2 - 155, top = height / 2 - 105;
        ctx.fill(left - 5, top - 8, left + 315, top + 222, 0xE8202028);
        ctx.drawText(textRenderer, title, left, top - 24, 0xFFFFFF, true);
        super.render(ctx, mouseX, mouseY, delta);
        List<Item> filtered = filtered();
        int maxScroll = Math.max(0, (filtered.size() - 1) / ROWS);
        scroll = Math.min(scroll, maxScroll);
        int start = scroll * ROWS;
        int visible = Math.min(ROWS, Math.max(0, filtered.size() - start));
        for (int n = 0; n < visible; n++) {
            Item item = filtered.get(start + n);
            int y = top + 31 + n * 19;
            boolean chosen = item == selectedTarget;
            ctx.fill(left + 92, y, left + 308, y + 18, chosen ? 0xFF555D80 : 0xFF303038);
            ctx.drawItem(new ItemStack(item), left + 94, y + 1);
            String label = item.getName().getString();
            if (label.length() > 26) label = label.substring(0, 25) + "…";
            ctx.drawText(textRenderer, label, left + 114, y + 5, 0xFFFFFF, false);
            if (chosen) ctx.drawText(textRenderer, "✓", left + 294, y + 5, 0x55FF55, false);
        }
        if (filtered.isEmpty()) ctx.drawText(textRenderer, language.equals("PL") ? "Brak wyników" : "No results", left + 120, top + 80, 0xAAAAAA, false);
        if (sourceItem == null) {
            ctx.drawText(textRenderer, language.equals("PL") ? "Najpierw weź przedmiot do głównej ręki" : "Hold an item in your main hand first", left, top + 87, 0xFFFFAA, false);
        } else {
            String source = sourceItem.getName().getString();
            ctx.drawText(textRenderer, (language.equals("PL") ? "Zmieniasz wygląd: " : "Appearance for: ") + source, left, top + 87, 0xCCCCCC, false);
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = width / 2 - 155, top = height / 2 - 105;
        if (mouseX >= left + 92 && mouseX <= left + 310 && mouseY >= top + 31 && mouseY < top + 31 + ROWS * 19) {
            int row = (int)((mouseY - (top + 31)) / 19);
            List<Item> filtered = filtered();
            int index = scroll * ROWS + row;
            if (index >= 0 && index < filtered.size()) {
                selectedTarget = filtered.get(index);
                if (sourceItem != null) {
                    String sourceId = Registries.ITEM.getId(sourceItem).toString();
                    AppearanceConfig.Entry previous = AppearanceConfig.entryFor(sourceItem);
                    String mode = previous == null ? "FLAT" : previous.mode;
                    AppearanceConfig.ENTRIES.put(sourceId, new AppearanceConfig.Entry(Registries.ITEM.getId(selectedTarget).toString(), mode));
                    AppearanceConfig.save();
                }
                if (modeButton != null) modeButton.setMessage(modeText());
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean shouldPause() { return false; }
}
