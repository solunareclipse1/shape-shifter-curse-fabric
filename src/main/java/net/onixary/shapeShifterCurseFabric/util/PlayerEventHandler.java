package net.onixary.shapeShifterCurseFabric.util;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoon;
import net.onixary.shapeShifterCurseFabric.data.PlayerNbtStorage;
import net.onixary.shapeShifterCurseFabric.networking.ModPacketsS2C;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.ability.FormAbilityManager;
import net.onixary.shapeShifterCurseFabric.player_form.ability.PlayerFormComponent;
import net.onixary.shapeShifterCurseFabric.player_form.ability.RegPlayerFormComponent;
import net.onixary.shapeShifterCurseFabric.player_form.instinct.InstinctManager;
import net.onixary.shapeShifterCurseFabric.status_effects.attachment.EffectManager;
import net.onixary.shapeShifterCurseFabric.status_effects.attachment.PlayerEffectAttachment;
import net.onixary.shapeShifterCurseFabric.team.MobTeamManager;
import net.onixary.shapeShifterCurseFabric.team.PlayerTeamHandler;

import static net.onixary.shapeShifterCurseFabric.player_form.instinct.InstinctTicker.loadInstinct;
import static net.onixary.shapeShifterCurseFabric.status_effects.RegTStatusEffect.removeVisualEffects;
import static net.onixary.shapeShifterCurseFabric.status_effects.attachment.EffectManager.*;

public class PlayerEventHandler {
    public static void register() {
        // join event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (handler.player.getWorld().isClient()) return;

            // check if first join with mod
            if(!PlayerNbtStorage.loadBooleanValue(handler.player.getServerWorld(), handler.player.getUuid().toString(), "first_join_with_mod")){
                ShapeShifterCurseFabric.LOGGER.info("First join with mod");
                // trigger advancement
                ShapeShifterCurseFabric.ON_FIRST_JOIN_WITH_MOD.trigger((ServerPlayerEntity) handler.player);
                // set first join with mod to false
                PlayerNbtStorage.saveBooleanValue(handler.player.getServerWorld(), handler.player.getUuid().toString(), "first_join_with_mod", true);
            }


            // load form
            FormAbilityManager.getServerWorld(server.getOverworld());
            ServerPlayerEntity player = handler.player;
            FormAbilityManager.loadForm(player);
            //PlayerFormComponent component = RegPlayerFormComponent.PLAYER_FORM.get(player);
            //FormAbilityManager.applyForm(player, component.getCurrentForm());

            // load attachment
            boolean hasAttachment = loadCurrentAttachment(server.getOverworld(), player);
            if(!hasAttachment) {
                resetAttachment(player);
            }
            else{
                ShapeShifterCurseFabric.LOGGER.info("Attachment loaded ");
            }
            ModPacketsS2C.sendSyncEffectAttachment(player, player.getAttached(EffectManager.EFFECT_ATTACHMENT));

            // load instinct
            InstinctManager.getServerWorld(server.getOverworld());
            loadInstinct(player);

            // load cursed moon data
            ShapeShifterCurseFabric.cursedMoonData.getInstance().load(server.getOverworld());
            if(FormAbilityManager.getForm(player) == PlayerForms.ORIGINAL_BEFORE_ENABLE){
                ShapeShifterCurseFabric.LOGGER.info("Cursed moon disabled");
                ShapeShifterCurseFabric.cursedMoonData.getInstance().disableCursedMoon(server.getOverworld());
            }
            else{
                ShapeShifterCurseFabric.LOGGER.info("Cursed moon enabled");
                ShapeShifterCurseFabric.cursedMoonData.getInstance().enableCursedMoon(server.getOverworld());
            }
            ShapeShifterCurseFabric.cursedMoonData.getInstance().save(server.getOverworld());

            // reset moon effect
            CursedMoon.resetMoonEffect();

            // Set doDaylightCycle to true forced
            server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, server);

            // update team
            PlayerTeamHandler.updatePlayerTeam(player);
        });
        // copy event
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            // 仅在服务端执行
            if (newPlayer.getWorld().isClient()) return;

            copyTransformativeEffect(oldPlayer, newPlayer);
            copyFormAndAbility(oldPlayer, newPlayer);
            PlayerTeamHandler.updatePlayerTeam(newPlayer);
        });

        //load event
        ServerWorldEvents.LOAD.register((server, world) -> {
            for (ServerPlayerEntity player : world.getPlayers()) {
                PlayerFormComponent component = RegPlayerFormComponent.PLAYER_FORM.get(player);
                FormAbilityManager.applyForm(player, component.getCurrentForm());

                MobTeamManager.registerTeam(world);
                PlayerTeamHandler.updatePlayerTeam(player);
                handleEntityTeam(world);
            }
        });
    }

    private static void copyTransformativeEffect(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer) {
        // transformative effect attachment
        PlayerEffectAttachment oldAttachment = oldPlayer.getAttached(EffectManager.EFFECT_ATTACHMENT);
        newPlayer.setAttached(EffectManager.EFFECT_ATTACHMENT, new PlayerEffectAttachment());
        PlayerEffectAttachment newAttachment = newPlayer.getAttached(EffectManager.EFFECT_ATTACHMENT);

        newAttachment.currentToForm = oldAttachment.currentToForm;
        newAttachment.remainingTicks = oldAttachment.remainingTicks;
        newAttachment.currentEffect = oldAttachment.currentEffect;

        // reapply potion effect
        if (newAttachment.currentEffect != null && currentRegEffect != null) {
            ShapeShifterCurseFabric.LOGGER.info("re-apply potion effect here");
            removeVisualEffects(newPlayer);
            newPlayer.addStatusEffect(new StatusEffectInstance(
                    currentRegEffect,
                    newAttachment.remainingTicks
            ));
        }
    }

    private static void copyFormAndAbility(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer) {
        PlayerFormComponent oldComponent = RegPlayerFormComponent.PLAYER_FORM.get(oldPlayer);
        PlayerFormComponent newComponent = RegPlayerFormComponent.PLAYER_FORM.get(newPlayer);
        newComponent.setCurrentForm(oldComponent.getCurrentForm());
        newComponent.setCurrentForm(oldComponent.getCurrentForm());
        FormAbilityManager.applyForm(newPlayer, newComponent.getCurrentForm());
    }

    private static void handleEntityTeam(ServerWorld world){
        Scoreboard scoreboard = world.getScoreboard();
        Team sorceryTeam = scoreboard.getTeam(MobTeamManager.SORCERY_TEAM_NAME);
        for (Entity entity : world.iterateEntities()) {
            // Sorcery Team
            if (entity.getType() == EntityType.EVOKER
            || entity.getType() == EntityType.WITCH
            || entity.getType() == EntityType.VINDICATOR
            || entity.getType() == EntityType.PILLAGER
            || entity.getType() == EntityType.RAVAGER)
            {
                if (sorceryTeam != null) {
                    scoreboard.addPlayerToTeam(entity.getEntityName(), sorceryTeam);
                }
            }
        }
    }
}
