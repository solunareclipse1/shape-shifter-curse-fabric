package net.onixary.shapeShifterCurseFabric.data;


import io.wispforest.owo.config.annotation.Config;

@Config(name = "config-ssc", wrapperName = "ConfigSSC")
public class ConfigModelSSC {
    // player setting
    public boolean keepOriginalSkin = false;
    // transformative mob setting
    // bat mob
    public float transformativeBatSpawnChance = 0.2f;
    // axolotl mob
    public float transformativeAxolotlSpawnChance = 0.4f;
    // ocelot mob
    public float transformativeOcelotSpawnChance = 0.4f;
}
