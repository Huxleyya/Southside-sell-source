/*
 * This file is part of OneConfig.
 * OneConfig - Next Generation Config Library for Minecraft: Java Edition
 * Copyright (C) 2021~2023 Polyfrost.
 *   <https://polyfrost.cc> <https://github.com/Polyfrost/>
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *   OneConfig is licensed under the terms of version 3 of the GNU Lesser
 * General Public License as published by the Free Software Foundation, AND
 * under the Additional Terms Applicable to OneConfig, as published by Polyfrost,
 * either version 1.0 of the Additional Terms, or (at your option) any later
 * version.
 *
 *   This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 * License.  If not, see <https://www.gnu.org/licenses/>. You should
 * have also received a copy of the Additional Terms Applicable
 * to OneConfig, as published by Polyfrost. If not, see
 * <https://polyfrost.cc/legal/oneconfig/additional-terms>
 */

package cc.polyfrost.oneconfig.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.core.ConfigUtils;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.config.elements.OptionPage;
import cc.polyfrost.oneconfig.config.options.Option;
import cc.polyfrost.oneconfig.config.options.WrappedValue;
import cc.polyfrost.oneconfig.config.options.impl.HUD;
import cc.polyfrost.oneconfig.gui.elements.config.*;
import cc.polyfrost.oneconfig.internal.gui.HudGui;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import cc.polyfrost.oneconfig.platform.Platform;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HUDUtils {
//    public static void addHudOptions(OptionPage page, HUD option, Config config) {
//        Hud hud = option.getValue();
//        if (hud == null) return;
//        hud.position.setHud(hud);
//        hud.setConfig(config);
//        HudCore.huds.put(option, hud);
//        String category = option.category();
//        String subcategory = option.subcategory();
//        ArrayList<BasicOption> options = new ArrayList<>();
//        try {
//            ArrayList<Field> fieldArrayList = ConfigUtils.getClassFields(hud.getClass());
//            HashMap<String, Field> fields = new HashMap<>();
//            for (Field f : fieldArrayList) fields.put(f.getName(), f);
//            options.add(new ConfigHeader(
//                    null, hud, option.getLabel(), category, subcategory, 2
//            ));
//            options.add(ConfigUtils.getOption2(hud.enabled, category, subcategory));
////            options.add(new ConfigSwitch(fields.get("enabled"), hud, "Enabled", "If the HUD is enabled", category, subcategory, 1));
//
//            options.add(new ConfigButton(fields.get("resetPosition"), hud, "Position", "Reset HUD to default position", category, subcategory, 1, "Reset"));
//            options.add(new ConfigSwitch(fields.get("locked"), hud, "Locked", "If the position is locked", category, subcategory, 2));
//            options.add(new ConfigSlider(fields.get("scale"), hud, "Scale", "The scale of the HUD", category, subcategory, 0.3f, 10f, 0, false));
//            ConfigDropdown dropdown = new ConfigDropdown(fields.get("positionAlignment"), hud, "Position Alignment", "The alignment of the HUD", category, subcategory, 2, new String[]{"Auto", "Left", "Center", "Right"});
//            dropdown.addListener(() -> hud.setScale(hud.scale, Platform.getGuiPlatform().getCurrentScreen() instanceof HudGui));
//            options.add(dropdown);
//            options.addAll(ConfigUtils.getClassOptions(hud));
//            if (hud instanceof BasicHud) {
//                options.add(new ConfigCheckbox(fields.get("background"), hud, "Background", "If the background of the HUD is enabled.", category, subcategory, 1));
//                options.add(new ConfigCheckbox(fields.get("rounded"), hud, "Rounded corners", "If the background has rounded corners.", category, subcategory, 1));
//                options.get(options.size() - 1).addDependency("Background or Border", () -> ((BasicHud) hud).background || ((BasicHud) hud).border);
//                options.add(new ConfigCheckbox(fields.get("border"), hud, "Outline/border", "If the hud has an outline.", category, subcategory, 1));
//                options.add(new ConfigColorElement(fields.get("bgColor"), hud, "Background color:", "The color of the background.", category, subcategory, 1, true));
//                options.get(options.size() - 1).addDependency("Background", () -> ((BasicHud) hud).background);
//                options.add(new ConfigColorElement(fields.get("borderColor"), hud, "Border color:", "The color of the border.", category, subcategory, 1, true));
//                options.get(options.size() - 1).addDependency("Border", () -> ((BasicHud) hud).border);
//                options.add(new ConfigSlider(fields.get("cornerRadius"), hud, "Corner radius:", "The corner radius of the background.", category, subcategory, 0, 10, 0, false));
//                options.get(options.size() - 1).addDependency("Rounded", () -> ((BasicHud) hud).rounded);
//                options.add(new ConfigSlider(fields.get("borderSize"), hud, "Border thickness:", "The thickness of the outline.", category, subcategory, 0, 10, 0, false));
//                options.get(options.size() - 1).addDependency("Border", () -> ((BasicHud) hud).border);
//                Field paddingX = fields.get("paddingX");
//                Field paddingY = fields.get("paddingY");
//                try {
//                    boolean changed = false;
//                    if (((float) ConfigUtils.getField(paddingX, hud)) > 10) {
//                        paddingX.set(hud, 5f);
//                        changed = true;
//                    }
//                    if (((float) ConfigUtils.getField(paddingY, hud)) > 10) {
//                        paddingY.set(hud, 5f);
//                        changed = true;
//                    }
//                    if (changed) config.save();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                options.add(new ConfigSlider(paddingX, hud, "X-Padding", "The horizontal padding of the HUD.", category, subcategory, 0, 10, 0, false));
//                options.add(new ConfigSlider(paddingY, hud, "Y-Padding", "The vertical padding of the HUD.", category, subcategory, 0, 10, 0, false));
//                options.get(options.size() - 2).addDependency("Background or Border", () -> ((BasicHud) hud).background || ((BasicHud) hud).border);
//                options.get(options.size() - 1).addDependency("Background or Border", () -> ((BasicHud) hud).background || ((BasicHud) hud).border);
//            }
//            for (BasicOption option : options) {
//                if (option.name.equals("Enabled")) continue;
//                option.addDependency(hudAnnotation.name(), hud::isEnabled);
//            }
//        } catch (Exception ignored) {
//        }
//        HudCore.hudOptions.addAll(options);
//        ConfigUtils.getSubCategory(page, option.category(), option.subcategory()).options.addAll(options);
//    }
}
