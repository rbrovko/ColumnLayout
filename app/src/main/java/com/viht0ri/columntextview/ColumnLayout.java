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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;

import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ColumnLayout extends View {
    private List<Layout> layouts = new ArrayList<Layout>();
    int mColumnWidth;
    int mSpacing;
    boolean mTextLayoutNeeded = true;

    CharSequence mText;
    TextPaint mPaint;

    public ColumnLayout(Context ctx) {
        this(ctx, null);
    }

    public ColumnLayout(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        mPaint = new TextPaint();
        Resources res = ctx.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        //Set sensible default values
        mColumnWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, metrics);
        mSpacing = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, metrics);
    }

    /**
     * Set the space between the columns. The view does not have automatic padding around the text.
     * @param width
     */
    public void setColumnWidth(int width) {
        mColumnWidth = width;
        mTextLayoutNeeded = true;
        invalidate();
    }

    public int getColumnWidth() {
        return mColumnWidth;
    }

    public void setText(CharSequence text) {
        mText = text;
        mTextLayoutNeeded = true;
        invalidate();
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        mTextLayoutNeeded = true;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTextLayoutNeeded = true;
    }

    private int getOverflowBegin() {
        if (layouts.size() > 0) {
            Layout last = layouts.get(layouts.size() - 1);
            return last.getLineEnd(last.getLineCount() - 1);
        } else {
            return -1;
        }
    }

    /**
     * Get the text that didn't fit to the screen.
     * @return
     */
    public CharSequence getOverflow() {
        int start = getOverflowBegin();
        if(start > -1 && start < mText.length() - 1) {
            return mText.subSequence(start, mText.length());
        }
        else return "";
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mTextLayoutNeeded) {
            createLayouts(getWidth(), getHeight());
        }
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        for(Layout l : layouts) {
            l.draw(canvas);
            canvas.translate(mColumnWidth, 0);
            canvas.translate(mSpacing, 0);
        }
        canvas.restore();
    }

    private void createLayouts(int width, int height) {
        layouts.clear();
        if(mText == null) {
            return;
        }
        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        Layout masterLayout = createLayout(mColumnWidth, mText, mPaint);
        int startLine = 0;
        int usedWidth = 0;
        while(usedWidth < availableWidth - mSpacing - mColumnWidth) {
            int startLineTop = masterLayout.getLineTop(startLine);
            int endLine = startLine;
            for(int i = startLine; i < masterLayout.getLineCount(); i++) {
                if(masterLayout.getLineBottom(i) - startLineTop < availableHeight) {
                    endLine = i;
                } else if(endLine == startLine) {
                    //A large image can be larger than the available height, skip the content
                    Toast.makeText(getContext(), "Skipping too large content", Toast.LENGTH_SHORT).show();
                    startLine++;
                    startLineTop = masterLayout.getLineTop(startLine);
                    endLine = startLine;
                } else {
                    break;
                }
            }
            int columnStart = masterLayout.getLineStart(startLine);
            int columnEnd = masterLayout.getLineEnd(endLine);
            layouts.add(createLayout(mColumnWidth, mText, columnStart, columnEnd, mPaint));
            if(endLine == masterLayout.getLineCount() - 1) {
                break;
            }
            usedWidth += mColumnWidth;
            startLine = endLine;
        }
        mTextLayoutNeeded = false;
    }

    private static Layout createLayout(int width, CharSequence text, TextPaint paint) {
        return new StaticLayout(text, 0, text.length(), paint,
                width, Layout.Alignment.ALIGN_NORMAL, 1f, 0, true);
    }

    private static Layout createLayout(int width, CharSequence text, int offset, int end, TextPaint paint) {
        return new StaticLayout(text, offset, end, paint,
                width, Layout.Alignment.ALIGN_NORMAL, 1f, 0, true);
    }
}
