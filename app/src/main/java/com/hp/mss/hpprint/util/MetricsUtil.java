/*
 * Hewlett-Packard Company
 * All rights reserved.
 *
 * This file, its contents, concepts, methods, behavior, and operation
 * (collectively the "Software") are protected by trade secret, patent,
 * and copyright laws. The use of the Software is governed by a license
 * agreement. Disclosure of the Software to third parties, in any form,
 * in whole or in part, is expressly prohibited except as authorized by
 * the license agreement.
 */

package com.hp.mss.hpprint.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Base64;

public class MetricsUtil {

    private static final String PRINT_METRICS_PRODUCTION_SERVER = "https://print-metrics-w1.twosmiles.com/api/v1/mobile_app_metrics";
    private static final String PRINT_METRICS_TEST_SERVER = "http://print-metrics-test.twosmiles.com/api/v1/mobile_app_metrics";
    //    private static final String PRINT_METRICS_LOCAL_SERVER = "http://10.0.2.2:4567/api/v1/mobile_app_metrics";
    private static final String PRINT_METRICS_USER_NAME = "hpmobileprint";
    private static final String PRINT_METRICS_PASSWORD = "print1t";

    public static boolean isDebuggable(Context context) {
        return ( 0 != ( context.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE ) );
    }

    public static String getMetricsServer(Context context) {
        return (isDebuggable(context) ? PRINT_METRICS_TEST_SERVER : PRINT_METRICS_PRODUCTION_SERVER);
    }

    public static String getAuthorizationString() {
        String authorizationString = "Basic " + Base64.encodeToString((PRINT_METRICS_USER_NAME + ":" + PRINT_METRICS_PASSWORD).getBytes(), Base64.NO_WRAP);
        return authorizationString;
    }
}
