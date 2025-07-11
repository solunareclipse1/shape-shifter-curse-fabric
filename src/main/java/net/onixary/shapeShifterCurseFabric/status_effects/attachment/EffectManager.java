package net.onixary.shapeShifterCurseFabric.status_effects.attachment;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.onixary.shapeShifterCurseFabric.data.StaticParams;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2C;
import net.onixary.shapeShifterCurseFabric.status_effects.BaseTransformativeStatusEffect;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.*;
import static net.onixary.shapeShifterCurseFabric.status_effects.RegTStatusEffect.removeVisualEffects;
import static net.onixary.shapeShifterCurseFabric.data.PlayerNbtStorage.loadAttachment;
import static net.onixary.shapeShifterCurseFabric.data.PlayerNbtStorage.saveAttachment;

public class EffectManager {
    // 注册玩家附身
    public static final AttachmentType<PlayerEffectAttachment> EFFECT_ATTACHMENT =
            AttachmentRegistry.create(new Identifier(MOD_ID, "effect_data"));

    //static String testUUID = "testUUID-3d9ab571-1ea5-360b-bc9d-77cd0b2f72a9";
    public static BaseTransformativeStatusEffect currentRegEffect;

    // 覆盖新的效果
    public static void overrideEffect(PlayerEntity player, BaseTransformativeStatusEffect regEffect) {
        LOGGER.info("get attach here");
        PlayerEffectAttachment attachment = player.getAttached(EFFECT_ATTACHMENT);
        LOGGER.info("remove old effect here");
        // 移除旧效果
        if (attachment.currentEffect != null && attachment.currentEffect.getToForm() != regEffect.getToForm()) {
            attachment.currentEffect.onEffectCanceled(player);
        }
        LOGGER.info("apply new effect here");
        // 应用新效果
        currentRegEffect = attachment.currentRegEffect = regEffect;
        attachment.currentToForm = regEffect.getToForm();
        attachment.remainingTicks = StaticParams.T_EFFECT_DEFAULT_DURATION;
        attachment.currentEffect = regEffect;
        LOGGER.info("apply potion effect here");
        // 添加原版药水效果（用于渲染）
        // 呈现必须使用注册过的类，因此将其手动传入
        player.addStatusEffect(new StatusEffectInstance(regEffect, StaticParams.T_EFFECT_DEFAULT_DURATION));
        // 触发自定义成就
        ON_GET_TRANSFORM_EFFECT.trigger((ServerPlayerEntity) player);

        // 判断是否为服务端玩家并发送同步包
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ModPacketsS2C.sendSyncEffectAttachment(serverPlayer, attachment);
        }
    }

    public static void loadEffect(PlayerEntity player, PlayerEffectAttachment loadedAttachment) {
        if (loadedAttachment.currentRegEffect != null) {
            player.addStatusEffect(new StatusEffectInstance(loadedAttachment.currentRegEffect, loadedAttachment.remainingTicks));
        }

        // 判断是否为服务端玩家并发送同步包
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ModPacketsS2C.sendSyncEffectAttachment(serverPlayer, loadedAttachment);
            //LOGGER.info("sended sync effect attachment, currentToForm: " + loadedAttachment.currentToForm);
        }
    }

    // 强制应用当前效果
    public static void applyEffect(PlayerEntity player) {
        PlayerEffectAttachment attachment = player.getAttached(EFFECT_ATTACHMENT);
        LOGGER.info(attachment == null? "attachment is null" : "attachment is not null");
        if (attachment != null && attachment.currentEffect != null) {
            currentRegEffect = attachment.currentRegEffect = null;
            attachment.currentEffect.onEffectApplied(player);
            attachment.currentToForm = null;
            attachment.remainingTicks = 0;
            attachment.currentEffect = null;
        }
    }

    // 强制结束当前效果
    public static void cancelEffect(PlayerEntity player) {
        PlayerEffectAttachment attachment = player.getAttached(EFFECT_ATTACHMENT);
        LOGGER.info(attachment == null? "attachment is null" : "attachment is not null");
        if (attachment != null && attachment.currentEffect != null) {
            currentRegEffect = attachment.currentRegEffect = null;
            attachment.currentEffect.onEffectCanceled(player);
            attachment.currentToForm = null;
            attachment.remainingTicks = 0;
            attachment.currentEffect = null;
        }
    }

    public static PlayerEffectAttachment getCurrentEffectAttachment(PlayerEntity player) {
        return player.getAttached(EFFECT_ATTACHMENT);
    }

    public static boolean saveCurrentAttachment(ServerWorld world, PlayerEntity player) {
        PlayerEffectAttachment attachment = player.getAttached(EFFECT_ATTACHMENT);
        if(attachment != null) {
            //saveAttachment(String.valueOf((player.getUuid())), attachment);
            saveAttachment(world, player.getUuid().toString(), attachment);
            //LOGGER.info("save attachment success, currentToForm: " + attachment.currentToForm);
            // 判断是否为服务端玩家并发送同步包
            if (player instanceof ServerPlayerEntity serverPlayer) {
                ModPacketsS2C.sendSyncEffectAttachment(serverPlayer, attachment);
                //LOGGER.info("sended sync effect attachment, currentToForm: " + attachment.currentToForm);
            }
            return true;
        }
        else{
            LOGGER.info("save attachment failed");
            return false;
        }
    }

    public static boolean loadCurrentAttachment(ServerWorld world, PlayerEntity player) {
        PlayerEffectAttachment attachment = loadAttachment(world, player.getUuid().toString());
        player.setAttached(EffectManager.EFFECT_ATTACHMENT, attachment);
        if(attachment == null){
            LOGGER.info("no attachment found in file");
            return false;
        }
        else if(attachment.currentToForm != null){
            LOGGER.info("load attachment success, currentToForm: " + attachment.currentToForm);
            loadEffect(player, attachment);
            return true;
        }
        else{
            LOGGER.info("loaded attachment is empty, reset attachment");
            return false;
        }
    }

    public static void resetAttachment(PlayerEntity player) {
        player.setAttached(EffectManager.EFFECT_ATTACHMENT, new PlayerEffectAttachment());
        removeVisualEffects(player);
    }
}
