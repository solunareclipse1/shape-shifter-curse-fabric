package net.onixary.shapeShifterCurseFabric.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoon;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.item.RegCustomItem;
import net.onixary.shapeShifterCurseFabric.item.RegCustomPotions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDeathMixin {
    @Inject(
            method = "onDeath",
            at = @At(
                    value = "HEAD"
            )
    )
    private void onEntityDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;
        World world = entity.getWorld();

        // 仅在服务端执行，避免客户端重复触发
        if (world.isClient) return;
        Entity attacker = source.getAttacker();
        // 自定义实体的掉落逻辑
        if (attacker instanceof ServerPlayerEntity) {
            if(entity instanceof WitchEntity || entity instanceof EvokerEntity) {
                if (Math.random() < StaticParams.FAMILIAR_CURSE_POTION_DROP_PROBABILITY){
                    ItemStack customPotion = PotionUtil.setPotion(new ItemStack(Items.SPLASH_POTION), RegCustomPotions.FAMILIAR_FOX_FORM_POTION);
                    entity.getWorld().spawnEntity(
                            new ItemEntity(
                                    entity.getWorld(),
                                    entity.getX(),
                                    entity.getY(),
                                    entity.getZ(),
                                    customPotion
                            )
                    );
                }
            }
        }

        if(!(CursedMoon.isCursedMoon() && CursedMoon.isNight())){
            return;
        }

        if (attacker instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) attacker;
            if (entity instanceof MobEntity) {
                handleMobDeathDrop((MobEntity) entity, player);
            }
        }
    }

    @Unique
    private void handleMobDeathDrop(MobEntity mob, ServerPlayerEntity player) {
        // 概率掉落未加工的月之尘
        if (Math.random() < StaticParams.MOONDUST_DROP_PROBABILITY) {
            ItemStack stack = new ItemStack(RegCustomItem.UNTREATED_MOONDUST);
            mob.getWorld().spawnEntity(
                    new ItemEntity(
                            mob.getWorld(),
                            mob.getX(),
                            mob.getY(),
                            mob.getZ(),
                            stack
                    )
            );
        }
    }
}
