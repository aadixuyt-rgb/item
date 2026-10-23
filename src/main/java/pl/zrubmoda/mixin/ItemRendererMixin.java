package pl.zrubmoda.mixin;

import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.render.model.BakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.zrubmoda.config.AppearanceConfig;

/** Resolves the visual model from the mapped item while leaving the real stack untouched. */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void zrubModa$visualModel(ItemStack stack, World world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        if (stack == null || stack.isEmpty()) return;
        Item original = stack.getItem(); Item target = AppearanceConfig.targetFor(original);
        if (target == original) return;
        try {
            ItemStack visual = new ItemStack(target);
            ItemRenderer self = (ItemRenderer)(Object)this;
            cir.setReturnValue(self.getModel(visual, world, entity, seed));
        } catch (Throwable ignored) { /* missing/broken models fall back to vanilla */ }
    }
}