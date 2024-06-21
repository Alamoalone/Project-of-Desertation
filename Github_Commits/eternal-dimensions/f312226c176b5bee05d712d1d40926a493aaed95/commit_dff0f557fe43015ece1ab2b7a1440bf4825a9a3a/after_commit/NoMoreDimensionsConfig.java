package com.vorono4ka.config;

import com.vorono4ka.NoMoreDimensions;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = NoMoreDimensions.MOD_ID)
public class NoMoreDimensionsConfig implements ConfigData {
    private static class Constant {
        @ConfigEntry.Gui.Excluded
        private static final String COMMON_CATEGORY = "default";
    }

    @ConfigEntry.Gui.CollapsibleObject
    public Common common = new Common();

    public static class Common {
        @ConfigEntry.Category(Constant.COMMON_CATEGORY)
        public final String listDelimiter = ",";
        @ConfigEntry.Category(Constant.COMMON_CATEGORY)
        public String blockedDimensions;

        public String[] getBlockedDimensions() {
            return blockedDimensions != null ? blockedDimensions.split(listDelimiter) : new String[0];
        }
    }
}
