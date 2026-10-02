package com.thecascadian.universalcompression.client.gui;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig.Entry;
import com.thecascadian.universalcompression.config.CompressionConfig;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen.Context;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen.Element;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.ListValueSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ValueSpec;
import org.jetbrains.annotations.Nullable;

public class UCConfigSectionScreen extends ConfigurationSectionScreen {
   private static final String NF_SECTION = "neoforge.configuration.uitext.section";
   private static final Set<String> ENABLE_KEYS = Set.of("enable_ns_exact", "enable_ns_contains", "enable_ns_starts", "enable_path_contains");

   public UCConfigSectionScreen(Screen parent, Type type, ModConfig config, Component title) {
      super(parent, type, config, title);
   }

   public UCConfigSectionScreen(Context parentCtx, Screen parent, Map<String, Object> valueSpecs, String key, Set<? extends Entry> entries, Component title) {
      super(parentCtx, parent, valueSpecs, key, entries, title);
   }

   @Nullable
   protected Element createBooleanValue(String key, ValueSpec spec, Supplier<Boolean> source, Consumer<Boolean> target) {
      return ENABLE_KEYS.contains(key) ? null : super.createBooleanValue(key, spec, source, target);
   }

   @Nullable
   protected <T> Element createList(String key, ListValueSpec spec, ConfigValue<List<T>> list) {
      BooleanValue toggle = getToggleForList(key);
      if (toggle == null) {
         return super.createList(key, spec, list);
      } else {
         String toggleKey = getToggleKeyFor(key);
         CycleButton<Boolean> toggleBtn = CycleButton.<Boolean>builder(v -> v ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF)
            .withValues(new Boolean[]{Boolean.TRUE, Boolean.FALSE})
            .displayOnlyValue()
            .withInitialValue((Boolean)toggle.getRaw())
            .create(0, 0, 80, 20, Component.empty(), (btn, newVal) -> {
               boolean old = (Boolean)toggle.getRaw();
               this.undoManager.add(v -> {
                  toggle.set(v);
                  this.onChanged(toggleKey);
               }, newVal, v -> {
                  toggle.set(v);
                  this.onChanged(toggleKey);
               }, old);
            });
         Element listElem = super.createList(key, spec, list);
         if (listElem == null) {
            return null;
         } else {
            AbstractWidget editBtn = listElem.widget();
            return new Element(
               this.getTranslationComponent(key), this.getTooltipComponent(key, null), new UCConfigSectionScreen.TwoButtonWidget(toggleBtn, editBtn), true
            );
         }
      }
   }

   protected Element createSection(String key, UnmodifiableConfig subconfig, UnmodifiableConfig subsection) {
      Component label = Component.translatable("neoforge.configuration.uitext.section", new Object[]{this.getTranslationComponent(key)});
      Component tooltip = this.getTooltipComponent(key, null);
      Button btn = Button.builder(
            label,
            button -> this.minecraft
                  .setScreen(
                     (Screen)this.sectionCache
                        .computeIfAbsent(
                           key,
                           k -> new UCConfigSectionScreen(
                                    this.context, this, subconfig.valueMap(), key, subsection.entrySet(), Component.translatable(this.getTranslationKey(key))
                                 )
                                 .rebuild()
                        )
                  )
         )
         .tooltip(Tooltip.create(tooltip))
         .width(150)
         .build();
      return new Element(label, tooltip, btn, false);
   }

   @Nullable
   private static BooleanValue getToggleForList(String listKey) {
      return switch (listKey) {
         case "blacklist_namespaces_exact" -> CompressionConfig.COMMON.enableNsExact;
         case "blacklist_namespaces_contains" -> CompressionConfig.COMMON.enableNsContains;
         case "blacklist_namespaces_starts" -> CompressionConfig.COMMON.enableNsStarts;
         case "blacklist_paths_contains" -> CompressionConfig.COMMON.enablePathContains;
         default -> null;
      };
   }

   private static String getToggleKeyFor(String listKey) {
      return switch (listKey) {
         case "blacklist_namespaces_exact" -> "enable_ns_exact";
         case "blacklist_namespaces_contains" -> "enable_ns_contains";
         case "blacklist_namespaces_starts" -> "enable_ns_starts";
         case "blacklist_paths_contains" -> "enable_path_contains";
         default -> listKey;
      };
   }

   private static final class TwoButtonWidget extends AbstractWidget {
      private static final int GAP = 4;
      private final CycleButton<Boolean> toggleBtn;
      private final AbstractWidget editBtn;

      TwoButtonWidget(CycleButton<Boolean> toggle, AbstractWidget edit) {
         super(0, 0, 150, 20, CommonComponents.EMPTY);
         this.toggleBtn = toggle;
         this.editBtn = edit;
         this.layout();
      }

      private void layout() {
         int toggleW = (this.width - 4) * 2 / 5;
         int editW = this.width - toggleW - 4;
         this.toggleBtn.setWidth(toggleW);
         this.editBtn.setWidth(editW);
         this.toggleBtn.setX(this.getX());
         this.toggleBtn.setY(this.getY());
         this.editBtn.setX(this.getX() + toggleW + 4);
         this.editBtn.setY(this.getY());
      }

      public void setX(int x) {
         super.setX(x);
         this.layout();
      }

      public void setY(int y) {
         super.setY(y);
         this.layout();
      }

      protected void renderWidget(GuiGraphics g, int mx, int my, float pt) {
         this.toggleBtn.render(g, mx, my, pt);
         this.editBtn.render(g, mx, my, pt);
      }

      public boolean mouseClicked(double mx, double my, int button) {
         return this.toggleBtn.mouseClicked(mx, my, button) || this.editBtn.mouseClicked(mx, my, button);
      }

      public void mouseMoved(double mx, double my) {
         this.toggleBtn.mouseMoved(mx, my);
         this.editBtn.mouseMoved(mx, my);
      }

      protected void updateWidgetNarration(NarrationElementOutput out) {
         this.toggleBtn.updateNarration(out);
      }
   }
}
