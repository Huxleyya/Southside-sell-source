///*
// * This file is part of OneConfig.
// * OneConfig - Next Generation Config Library for Minecraft: Java Edition
// * Copyright (C) 2021~2023 Polyfrost.
// *   <https://polyfrost.cc> <https://github.com/Polyfrost/>
// *
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
// *
// *   OneConfig is licensed under the terms of version 3 of the GNU Lesser
// * General Public License as published by the Free Software Foundation, AND
// * under the Additional Terms Applicable to OneConfig, as published by Polyfrost,
// * either version 1.0 of the Additional Terms, or (at your option) any later
// * version.
// *
// *   This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// * Lesser General Public License for more details.
// *
// *   You should have received a copy of the GNU Lesser General Public
// * License.  If not, see <https://www.gnu.org/licenses/>. You should
// * have also received a copy of the Additional Terms Applicable
// * to OneConfig, as published by Polyfrost. If not, see
// * <https://polyfrost.cc/legal/oneconfig/additional-terms>
// */
//
//package cc.polyfrost.oneconfig.internal.mixin;
//
//import cc.polyfrost.oneconfig.renderer.TextRenderer;
//import org.spongepowered.asm.mixin.Dynamic;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Pseudo;
//import org.spongepowered.asm.mixin.injection.Constant;
//import org.spongepowered.asm.mixin.injection.ModifyConstant;
//
//@Pseudo
//@Mixin(targets = "net.wyvest.redaction.features.NameHighlight")
//public class RedactionNameHighlightMixin {
//
//    @Dynamic("REDACTION")
//    @ModifyConstant(method = "getColorCode", remap = false, constant = @Constant(stringValue = "§w"))
//    private static String removeColorCode(String original) {
//        if (TextRenderer.isDrawingTextBorder()) {
//            return "";
//        } else {
//            return original;
//        }
//    }
//}