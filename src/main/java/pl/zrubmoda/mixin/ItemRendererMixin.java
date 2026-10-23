package pl.zrubmoda.mixin;

import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.WrapperBakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.zrubmoda.config.AppearanceConfig;

/**
 * Replaces only the resolved client-side model. The actual inventory stack is never edited.
 * A guard prevents a target item's own mapping from recursively changing the replacement model.
 */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Unique private static final ThreadLocal<Boolean> ZRUB_MODA_RESOLVING = ThreadLocal.withInitial(() -> false);

    @Shadow
    public abstract BakedModel getModel(ItemStack stack, World world, LivingEntity entity, int seed);

    @Inject(method = "getModel(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)Lnet/minecraft/client/render/model/BakedModel;", at = @At("HEAD"), cancellable = true)
    private void zrubModa$visualModel(ItemStack stack, World world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        if (ZRUB_MODA_RESOLVING.get() || stack == null || stack.isEmpty()) return;
        Item original = stack.getItem();
        AppearanceConfig.Entry entry = AppearanceConfig.entryFor(original);
        Item target = AppearanceConfig.targetFor(original);
        if (target == original || entry == null) return;

        try {
            ZRUB_MODA_RESOLVING.set(true);
            BakedModel replacement = getModel(new ItemStack(target), world, entity, seed);
            if (replacement == null) return;

            // FLAT uses the source item's display transforms while keeping the target model/texture.
            // SWORD uses the target model's own transforms (e.g. a handheld item model).
            if (!"SWORD".equals(entry.mode)) {
                BakedModel sourceModel = getModel(stack, world, entity, seed);
                if (sourceModel != null) replacement = new ZrubModaTransformModel(replacement, sourceModel.getTransformation());
            }
            cir.setReturnValue(replacement);
        } catch (RuntimeException ignored) {
            // Fall back to vanilla rendering if a resource-pack model is unavailable or invalid.
        } finally {
            ZRUB_MODA_RESOLVING.set(false);
        }
    }

    @Unique
    private static final class ZrubModaTransformModel extends WrapperBakedModel {
        private final ModelTransformation transformation;
        private ZrubModaTransformModel(BakedModel wrapped, ModelTransformation transformation) {
            super(wrapped);
            this.transformation = transformation;
        }
        @Override public ModelTransformation getTransformation() { return transformation; }
    }
}
