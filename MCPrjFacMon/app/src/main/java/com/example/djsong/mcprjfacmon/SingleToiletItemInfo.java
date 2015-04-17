/**
 * [CS442] Mobile Computing, Spring 2015
 *
 * Single toilet space info that we are going to see if somebody is shitting inside.
 *
 * @author: DJ Song
 * */

package com.example.djsong.mcprjfacmon;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcelable;

/**
 * Created by DJSong on 2015-03-26.
 */
public class SingleToiletItemInfo extends SingleItemInfoBase
{
    // If we need additional stuff to be drawn, like shit can kk..

    public SingleToiletItemInfo(FacilityInfoBase InOwner, int InItemIndex, Rect InRelativeAreaRect)
    {
        super(InOwner, InItemIndex, InRelativeAreaRect);

        // Base on-map draw settings for a single toilet
        mBaseOnMapDrawColorA = 255;
        mBaseOnMapDrawColorR = 255;
        mBaseOnMapDrawColorG = 0;
        mBaseOnMapDrawColorB = 255;
        mBaseOnMapDrawStrokeWidth = 4.0f;
        mbFillBaseOnMapDraw = false;
    }

    /** For direct On-map drawing. */
    public void OnFacilityMapDrawing(Canvas InDrawCanvas, Paint InDrawPaint)
    {
        super.OnFacilityMapDrawing(InDrawCanvas, InDrawPaint);
    }

    /** Drawing for its own pop-up screen. */
    public void OnFacilityDetailedDrawing(Canvas InDrawCanvas, Paint InDrawPaint)
    {
        super.OnFacilityDetailedDrawing(InDrawCanvas, InDrawPaint);
    }

}