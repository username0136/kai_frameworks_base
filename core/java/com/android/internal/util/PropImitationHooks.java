/*
 * Copyright (C) 2022 Paranoid Android
 *           (C) 2023 StatiXOS
 *           (C) 2023 ArrowOS
 *           (C) 2023 The LibreMobileOS Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.util;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

/**
 * @hide
 */
public class PropImitationHooks {

    private static final String TAG = "PropImitationHooks";
    private static final boolean DEBUG = SystemProperties.getBoolean("debug.pihooks.log", false);

    private static final Set<String> SPOOF_PACKAGES = Set.of(
            "com.bank1",
            "com.bank2"
    );

    private static final Map<String, String> sCertifiedProps = Map.ofEntries(
            Map.entry("TYPE", "user"),
            Map.entry("TAGS", "release-keys"),
            Map.entry("ID", "ZP11.260417.009"),
            Map.entry("BRAND", "google"),
            Map.entry("DEVICE", "bluejay"),
            Map.entry("FINGERPRINT",
                    "google/bluejay_beta/bluejay:CANARY/ZP11.260417.009/15372612:user/release-keys"),
            Map.entry("MANUFACTURER", "Google"),
            Map.entry("MODEL", "Pixel 6a"),
            Map.entry("PRODUCT", "bluejay_beta"),
            Map.entry("VERSION.RELEASE", "17"),
            Map.entry("VERSION.SECURITY_PATCH", "2026-05-05"),
            Map.entry("VERSION.DEVICE_INITIAL_SDK_INT", "21"),
            Map.entry("VERSION.SDK_INT", "32")
    );

    public static void setProps(Context context) {
        final String packageName = context.getPackageName();

        if (TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "Null package name");
            return;
        }

        if (!SPOOF_PACKAGES.contains(packageName)) {
            return;
        }

        dlog("Spoofing build props for package: " + packageName);
        sCertifiedProps.forEach(PropImitationHooks::setPropValue);
    }

    private static void setPropValue(String key, String value) {
        try {
            dlog("Setting prop " + key + " to " + value);
            Class clazz = Build.class;
            if (key.startsWith("VERSION.")) {
                clazz = Build.VERSION.class;
                key = key.substring(8);
            }
            Field field = clazz.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, field.getType().equals(Integer.TYPE) ? Integer.parseInt(value) : value);
            field.setAccessible(false);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set prop " + key, e);
        }
    }

    public static void dlog(String msg) {
        if (DEBUG) Log.d(TAG, msg);
    }
}
