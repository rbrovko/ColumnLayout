/*
 *  Copyright 2014 Vihtori Mäntylä
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.viht0ri.columntextview;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;

import java.io.BufferedInputStream;
import java.io.IOException;


public class ColumnLayoutDemo extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.viht0ri.columntextview.R.layout.example);
        ColumnLayout cl = (ColumnLayout)findViewById(R.id.column_layout);
        int columnWidth = cl.getColumnWidth();
        Drawable dummyDrawable = getResources().getDrawable(R.drawable.android_robot_from_wikipedia);
        Html.ImageGetter imageGetter = new DummyImageGetter(dummyDrawable, columnWidth);
        String testHtml = loadText();
        for(int i = 0; i < 3; i++) {
            testHtml = testHtml.concat(testHtml);
        }
        CharSequence spanned = Html.fromHtml(testHtml, imageGetter, null);
        cl.setText(spanned);
    }

    private String loadText( ) {
        String text;
        try {
            BufferedInputStream bis = new BufferedInputStream(getResources().openRawResource(R.raw.test));
            byte[] bytes = new byte[bis.available()];
            bis.read(bytes);
            bis.close();
            text = new String(bytes, "UTF-8");
        }catch(IOException ioe) {
            throw new RuntimeException("Something went wrong");
        }
        return text;
    }

    private static class DummyImageGetter implements Html.ImageGetter {
        Drawable mDrawable;
        public DummyImageGetter(Drawable drawable, int targetWidth) {
            float scaleFactor = (float) targetWidth / drawable.getIntrinsicWidth();
            int adjustedHeight = (int)(scaleFactor * drawable.getIntrinsicHeight());
            drawable.setBounds(0, 0, targetWidth, adjustedHeight);
            mDrawable = drawable;
        }
        @Override
        public Drawable getDrawable(String source) {
            return mDrawable;
        }
    }
}
