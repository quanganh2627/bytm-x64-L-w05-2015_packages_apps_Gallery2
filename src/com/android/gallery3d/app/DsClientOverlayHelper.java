/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file was modified by Dolby Laboratories, Inc. The portions of the
 * code that are surrounded by "DOLBY..." are copyrighted and
 * licensed separately, as follows:
 *
 * (C) 2011-2013 Dolby Laboratories, Inc.
 * All rights reserved.
 *
 * This program is protected under international and U.S. Copyright laws as
 * an unpublished work. This program is confidential and proprietary to the
 * copyright owners. Reproduction or disclosure, in whole or in part, or the
 * production of derivative works therefrom without the express permission of
 * the copyright owners is prohibited.
 *
 */

//DOLBY_DAP_GUI
package com.android.gallery3d.app;

import android.content.Context;
import android.widget.ImageView;

public class DsClientOverlayHelper {
    private ImageView mSwitch;

    public DsClientOverlayHelper(Context context) {
    }

    public ImageView getIV() {
        return mSwitch;
    }
    public int[] layoutDolbySwitch() {
        int[] dim = new int[4];
        return dim;
    }
}
//DOLBY_DAP_GUI END
