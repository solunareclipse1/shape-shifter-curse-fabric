package net.onixary.shapeShifterCurseFabric.status_effects;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.axolotl.TransformativeAxolotlEntity;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.bat.TransformativeBatEntity;
import net.onixary.shapeShifterCurseFabric.form_giving_custom_entity.ocelot.TransformativeOcelotEntity;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.ability.FormAbilityManager;
import net.onixary.shapeShifterCurseFabric.status_effects.attachment.EffectManager;

import java.util.Objects;

import static net.onixary.shapeShifterCurseFabric.status_effects.RegTStatusEffect.*;
import static net.onixary.shapeShifterCurseFabric.status_effects.attachment.EffectManager.EFFECT_ATTACHMENT;

public class TStatusApplier {
    private TStatusApplier() {}

    public static float T_BAT_STATUS_CHANCE = 0.5f;
    public static float T_AXOLOTL_STATUS_CHANCE = 0.7f;
    public static float T_OCELOT_STATUS_CHANCE = 0.5f;

    public static void applyStatusFromTMob(MobEntity fromMob, PlayerEntity player) {
        if (fromMob instanceof TransformativeBatEntity) {
            applyStatusByChance(T_BAT_STATUS_CHANCE, player, TO_BAT_0_EFFECT);
        }
        else if(fromMob instanceof TransformativeAxolotlEntity) {
            applyStatusByChance(T_AXOLOTL_STATUS_CHANCE, player, TO_AXOLOTL_0_EFFECT);
        }
        else if(fromMob instanceof TransformativeOcelotEntity){
            applyStatusByChance(T_OCELOT_STATUS_CHANCE, player, TO_OCELOT_0_EFFECT);
        }
    }

    private static void applyStatusByChance(float chance, PlayerEntity player, BaseTransformativeStatusEffect regStatusEffect) {
        PlayerForms curToForm = Objects.requireNonNull(player.getAttached(EFFECT_ATTACHMENT)).currentToForm;
        ShapeShifterCurseFabric.LOGGER.info("current dest form: " + curToForm + " when applyStatusByChance");
        if(player.getAttached(EFFECT_ATTACHMENT) == null){
            ShapeShifterCurseFabric.LOGGER.info("attach is null when applyStatusByChance");
        }
        // 只有不同种类的效果才会互相覆盖
        if (Math.random() < chance && curToForm != regStatusEffect.getToForm() && FormAbilityManager.getForm(player) == PlayerForms.ORIGINAL_SHIFTER) {
            ShapeShifterCurseFabric.LOGGER.info("TStatusApplier applyStatusByChance");
            EffectManager.overrideEffect(player, regStatusEffect);
        }
    }
}
