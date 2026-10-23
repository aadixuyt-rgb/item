package pl.zrubmoda.mixin;

import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.zrubmoda.config.AppearanceConfig;

/**
 * Resolves a replacement baked model without mutating the stack passed by the game.
 */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow
    public abstract BakedModel getModel(ItemStack stack, World world, LivingEntity entity, int seed);

    @Inject(method = "getModel(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)Lnet/minecraft/client/render/model/BakedModel;", at = @At("HEAD"), cancellable = true)
    private void zrubModa$visualModel(ItemStack stack, World world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        if (stack == null || stack.isEmpty()) return;
        Item original = stack.getItem();
        Item target = AppearanceConfig.targetFor(original);
        if (target == original) return;

        try {
            ItemStack visualStack = new ItemStack(target);
            cir.setReturnValue(getModel(visualStack, world, entity, seed));
        } catch (RuntimeException ignored) {
            // Keep vanilla rendering if the replacement cannot be resolved.
        }
    }
}
