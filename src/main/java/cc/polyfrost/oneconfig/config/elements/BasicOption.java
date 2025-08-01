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

package cc.polyfrost.oneconfig.config.elements;

import cc.polyfrost.oneconfig.config.options.Option;
import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.ColorAnimation;
import cc.polyfrost.oneconfig.gui.animations.DummyAnimation;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.utils.DescriptionRenderer;
import cc.polyfrost.oneconfig.libs.universal.ChatColor;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import dev.diona.southside.util.render.ColorUtil;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class BasicOption {
    public final int size;
    protected final Field field;
    protected Object parent;
    public final String name;
    public final String description;
    public String category;
    public String subcategory;
    private final ColorAnimation nameColorAnimation = new ColorAnimation(new ColorPalette(Colors.WHITE_90, Colors.WHITE, Colors.WHITE_90));
    protected int nameColor = Colors.WHITE_90;
    private final Map<String, Supplier<Boolean>> dependencies = new HashMap<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private final List<Supplier<Boolean>> hideConditions = new ArrayList<>();
    private Animation descriptionAnimation = new DummyAnimation(0f);
    private float hoverTime = 0f;

    /**
     * Initialize option
     *
     * @param field       variable attached to option (null for category)
     * @param parent      the parent object of the field, used for getting and setting the variable
     * @param name        name of option
     * @param description The description
     * @param category    The category
     * @param subcategory The subcategory
     * @param size        size of option, 0 for single column, 1 for double.
     */
    public BasicOption(Field field, Object parent, String name, String description, String category, String subcategory, int size) {
        this.field = field;
        this.parent = parent;
        this.name = name;
        this.description = ColorUtil.stripColor(description);

        this.category = category;
        this.subcategory = subcategory;
        this.size = size;
        if (field != null) field.setAccessible(true);

        if (parent instanceof Option<?>) {
            ((Option<?>) parent).basicOption = this;
        }
    }

    /**
     * @param object Java object to set the variable to
     */
    protected void set(Object object) throws IllegalAccessException {
        if (field == null) return;
        if (object.equals(field.get(parent))) return;
        field.set(parent, object);
        this.triggerListeners();
    }

    public void triggerListeners() {
        for (Runnable listener : listeners) listener.run();
    }

    /**
     * @return value of variable as Java object
     */
    public Object get() throws IllegalAccessException {
        if (field == null) return null;
        return field.get(parent);
    }

    /**
     * @return height of option to align other options accordingly
     */
    public abstract int getHeight();

    /**
     * Function that gets called when drawing option
     *
     * @param vg NanoVG context
     * @param x  x position
     * @param y  y position
     */
    public abstract void draw(long vg, int x, int y, InputHandler inputHandler);

    /**
     * Function that gets called last drawing option,
     * should be used for things that draw above other options
     *
     * @param vg NanoVG context
     * @param x  x position
     * @param y  y position
     */
    public void drawLast(long vg, int x, int y, InputHandler inputHandler) {
        drawDescription(vg, x, y, inputHandler);
    }

    /**
     * Function that gets called when a key is typed
     *
     * @param key     char that has been typed
     * @param keyCode code of key
     */
    public void keyTyped(char key, int keyCode) {
    }

    public void drawDescription(long vg, int x, int y, InputHandler inputHandler) {
        if (description.trim().isEmpty() && dependencies.isEmpty()) return;
        boolean hovered = inputHandler.isAreaHovered(getNameX(x), y, NanoVGHelper.INSTANCE.getTextWidth(vg, name, 14f, Fonts.MEDIUM), 32f);
        nameColor = nameColorAnimation.getColor(hovered, false);
        if (hovered) hoverTime += GuiUtils.getDeltaTime();
        else hoverTime = 0;

        @Nullable String warningDescription = null;
        int others = 0;
        List<String> options = new ArrayList<>();
        if (!dependencies.isEmpty()) {
            for (Map.Entry<String, Supplier<Boolean>> dependency : dependencies.entrySet()) {
                String name = dependency.getKey();
                Supplier<Boolean> supplier = dependency.getValue();
                if (name.startsWith("unknown-")) {
                    others++;
                    continue;
                }

                if (!supplier.get()) {
                    options.add(name);
                }
            }
        }
        if (!options.isEmpty() || others != 0) {
            boolean knownOptions = options.isEmpty();
            StringBuilder builder = new StringBuilder("Option disabled by ");
            for (String mod : options) {
                builder.append("\"")
                        .append(mod)
                        .append("\", ");
            }
            builder = new StringBuilder(builder.substring(0, builder.length() - 2));
            if (others != 0) {
                if (knownOptions) builder.append(" and ");
                builder.append(others)
                        .append(" other option")
                        .append(others == 1 ? "" : "s");
            }
            builder.append(".");
            warningDescription = builder.toString();
        }

        if (!description.trim().isEmpty()) {
            DescriptionRenderer.drawDescription(vg, x, y, description, warningDescription, () -> descriptionAnimation, (a) -> descriptionAnimation = a, null, shouldDrawDescription(), (UResolution.getWindowWidth() / 2f < inputHandler.mouseX()) ? DescriptionRenderer.DescriptionPosition.RIGHT : DescriptionRenderer.DescriptionPosition.LEFT, inputHandler);
        }
    }

    /**
     * @return If this option should draw its description
     */
    protected boolean shouldDrawDescription() {
        return hoverTime > 350;
    }

    /**
     * Get the X of the name of the option, used to trigger the description
     *
     * @param x The x coordinate of the option
     * @return The x coordinate of the option's name
     */
    protected float getNameX(int x) {
        return x;
    }

    /**
     * @return If the option is enabled, based on the dependencies
     */
    public boolean isEnabled() {
        for (Supplier<Boolean> dependency : dependencies.values()) {
            if (!dependency.get()) return false;
        }
        return true;
    }

    public boolean isHidden() {
        for (Supplier<Boolean> condition : hideConditions) {
            if (condition.get()) return true;
        }
        return false;
    }

    /**
     * Add a condition to this option
     *
     * @param optionName The name of the option that adds this dependency
     * @param supplier The dependency
     */
    public void addDependency(String optionName, Supplier<Boolean> supplier) {
        this.dependencies.put(optionName, supplier);
    }

    /**
     * Add a condition to this option
     *
     * @param supplier The dependency
     * @deprecated Use {@link #addDependency(String, Supplier)} instead
     */
    @Deprecated
    public void addDependency(Supplier<Boolean> supplier) {
        this.dependencies.put("unknown-" + UUID.randomUUID(), supplier);
    }

    /**
     * Add a listener to this option
     *
     * @param runnable The listener
     */
    public void addListener(Runnable runnable) {
        this.listeners.add(runnable);
    }

    /**
     * Hide an option if a condition is met
     *
     * @param supplier The condition
     */
    public void addHideCondition(Supplier<Boolean> supplier) {
        this.hideConditions.add(supplier);
    }

    /**
     * @return The field
     */
    public Field getField() {
        return field;
    }

    /**
     * @return The parent of the field
     */
    public Object getParent() {
        return parent;
    }

    /**
     * @param parent The new parent object
     */
    public void setParent(Object parent) {
        this.parent = parent;
    }

    public void finishUpAndClose() {
    }
}
