/**
 * [CS442] Mobile Computing, Spring 2015
 *
 * Restroom information to be drawn on the overall map image as well as its main screen
 *
 * @author: DJ Song
 * */

package com.example.djsong.mcprjfacmon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcelable;

/**
 * Created by DJSong on 2015-03-26.
 */
public class FacRestroomInfo extends FacilityInfoBase
{
    boolean mbMale = true; // false if female.

    // Any special overlay drawing stuff?

    /** This icon related things could go to the super class side.. in more generic form */
    Bitmap mRestroomIconImage;
    float mRestroomIconImageScale = 0.8f;
    Point mRestroomIconImageOffset = new Point(0, 0); // Now it is almost like transformation.. kk

    public FacRestroomInfo(FacExpMapView InOwner, int InFloorNum, int InRoomNum, int InTotalItemNum, Rect InRelativeAreaRect, boolean bInMale)
    {
        super(InOwner, InFloorNum, InRoomNum, InTotalItemNum, InRelativeAreaRect);

        mFacilityType = CommPacketDef.FACTYPE_RESTROOM;

        mbMale = bInMale;

        if(mbMale) {
            mRestroomIconImage = BitmapFactory.decodeResource(InOwner.getResources(), R.drawable.restroom_icon_male);
        }
        else{
            mRestroomIconImage = BitmapFactory.decodeResource(InOwner.getResources(), R.drawable.restroom_icon_female);
        }

        // Base on-map draw settings for the restroom
        mBaseOnMapDrawColorA = 255;
        mBaseOnMapDrawColorR = 255;
        mBaseOnMapDrawColorG = 0;
        mBaseOnMapDrawColorB = 0;
        mBaseOnMapDrawStrokeWidth = 5.0f;
        mbFillBaseOnMapDraw = false;

        // I think this icon image relative stuff better be at the super class,
        // The exact value per sub-class can be just set here.
        mRestroomIconImageOffset.x = 40;
        mRestroomIconImageOffset.y = 0;
    }

    /**
     * Conceptually, this is supposed to load up the sub-item data of facilities (probably get from the server)
     * For now, this is just a dummy and we just hard-code some sample data somewhere..
     * */
    public boolean LoadUpItemData(Parcelable InDummyForNow)
    {
        // For simplicity (just for this semester), just make all the item size identical at the client side.
        // For a real world service, those information should come from the server too.

        for(int ItemIndex = 0; ItemIndex < mSpecifiedTotalItemNum; ++ItemIndex)
        {
            SingleItemInfoBase SampleToiletInfo01 = new SingleToiletItemInfo(this, 0,
                    new Rect(0, ItemIndex * 60, 100, (ItemIndex + 1) * 60));
            mUsableItems.add(ItemIndex, SampleToiletInfo01);
        }

        if(super.LoadUpItemData(InDummyForNow) == false) {
            return false;
        }
        return true;
    }

    /** Direct On-map drawing. Could be like an icon.. or the main drawing? */
    public void OnMapDrawing(Canvas InDrawCanvas)
    {
        super.OnMapDrawing(InDrawCanvas);

        // Draw restroom icon in the rect with some scaling and possibly with some offset?
        RectF DestRect = GetRestroomIconImageDrawRect(mRestroomIconImage.getWidth(), mRestroomIconImage.getHeight());
        Rect SrcRect = new Rect(0, 0, mRestroomIconImage.getWidth(), mRestroomIconImage.getHeight());
        InDrawCanvas.drawBitmap(mRestroomIconImage, SrcRect, DestRect, mDrawPaint);
    }

    /** Drawing for its own pop-up screen. */
    public void DetailedDrawing(Canvas InDrawCanvas)
    {
        super.DetailedDrawing(InDrawCanvas);

    }

    public RectF GetRestroomIconImageDrawRect(int ImageWidth, int ImageHeight)
    {
        int DestWidth = (int) ((float)ImageWidth * mRestroomIconImageScale);
        int DestHeight = (int) ((float)ImageHeight * mRestroomIconImageScale);

        RectF ReturnRect = new RectF(0, 0, DestWidth, DestHeight);
        ReturnRect.left += GetRelativeArea().centerX() - DestWidth / 2;
        ReturnRect.top += GetRelativeArea().centerY() - DestHeight / 2;
        ReturnRect.right += GetRelativeArea().centerX() - DestWidth / 2;
        ReturnRect.bottom += GetRelativeArea().centerY() - DestHeight / 2;

        // Finally apply some offset
        ReturnRect.left += mRestroomIconImageOffset.x;
        ReturnRect.top += mRestroomIconImageOffset.y;
        ReturnRect.right += mRestroomIconImageOffset.x;
        ReturnRect.bottom += mRestroomIconImageOffset.y;

        return ReturnRect;
    }
}
