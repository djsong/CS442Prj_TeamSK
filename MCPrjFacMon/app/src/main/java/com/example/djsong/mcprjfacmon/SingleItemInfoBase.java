/**
 * [CS442] Mobile Computing, Spring 2015
 *
 * The most basic and atomic item data to be possibly seen by one user (of its occupation state)
 *
 * @author: DJ Song
 * */

package com.example.djsong.mcprjfacmon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcelable;

/**
 * Created by DJSong on 2015-03-26.
 */
public class SingleItemInfoBase
{
    /** This item is belong to this OwnerFacility */
    private FacilityInfoBase mOwnerFacility;

    /** ID or index in its owner facility. Probably just same as the array index.. */
    private int mItemIndex;

    /** Just the variable that we are going to see! */
    private boolean mbIsOccupied = false;

    /** Basic dimension relative to its owner */
    private Rect mRelativeAreaRect;

    /** Set those base on-map drawing parameters at the constructor of sub-class.
     * Not for detailed drawing. */
    protected int mBaseOnMapDrawColorA = 255;
    protected int mBaseOnMapDrawColorR = 0;
    protected int mBaseOnMapDrawColorG = 0;
    protected int mBaseOnMapDrawColorB = 0;
    protected float mBaseOnMapDrawStrokeWidth = 5.0f;
    protected boolean mbFillBaseOnMapDraw = false;

    /** To visualize whether this item is in use or not */
    Bitmap mUsageStateImage_Available; // O
    Bitmap mUsageStateImage_Occupied; // X
    float mUsageStateImageScale = 0.5f;

    public SingleItemInfoBase(FacilityInfoBase InOwner, int InItemIndex, Rect InRelativeAreaRect)
    {
        mOwnerFacility = InOwner;
        mItemIndex = InItemIndex;

        mRelativeAreaRect = new Rect(InRelativeAreaRect);

        mUsageStateImage_Available = BitmapFactory.decodeResource(InOwner.GetOwner().getResources(), R.drawable.usage_icon_available);
        mUsageStateImage_Occupied = BitmapFactory.decodeResource(InOwner.GetOwner().getResources(), R.drawable.usage_icon_occupied);
    }

    /** For direct On-map drawing. */
    public void OnFacilityMapDrawing(Canvas InDrawCanvas, Paint InDrawPaint)
    {
        // Draw just an area box, and any additional stuff at sub-class..
        if(mbFillBaseOnMapDraw) {
            InDrawPaint.setStyle(Paint.Style.FILL);
        }
        else{
            InDrawPaint.setStyle(Paint.Style.STROKE);
        }
        InDrawPaint.setStrokeWidth( Math.max(1.0f, GetOwner().ApplyRenderScale(mBaseOnMapDrawStrokeWidth)) );
        InDrawPaint.setARGB(mBaseOnMapDrawColorA, mBaseOnMapDrawColorR, mBaseOnMapDrawColorG, mBaseOnMapDrawColorB);

        Rect AbsDrawRect = GetAbsoluteOnMapAreaDrawRect();
        InDrawCanvas.drawRect(AbsDrawRect.left, AbsDrawRect.top, AbsDrawRect.right, AbsDrawRect.bottom, InDrawPaint);

        if(mbIsOccupied)
        {
            int Dummy = 0;
            ++Dummy;
        }
        // Draw usage icon
        Bitmap CurrentStateImage = mbIsOccupied ? mUsageStateImage_Occupied : mUsageStateImage_Available;
        RectF DestRect = GetUsageImageDrawRect(CurrentStateImage.getWidth(), CurrentStateImage.getHeight());
        Rect SrcRect = new Rect(0, 0, CurrentStateImage.getWidth(), CurrentStateImage.getHeight());
        InDrawCanvas.drawBitmap(CurrentStateImage, SrcRect, DestRect, InDrawPaint);
    }

    /** Drawing for its own pop-up screen. */
    public void OnFacilityDetailedDrawing(Canvas InDrawCanvas, Paint InDrawPaint)
    {

    }

    public FacilityInfoBase GetOwner() {return mOwnerFacility;}
    public int GetItemIndex() {return mItemIndex;}
    public Rect GetRelativeArea() {return mRelativeAreaRect;}

    public void SetOccupied(boolean bInOccupied) {mbIsOccupied = bInOccupied;}
    public boolean IsOccupied() {return mbIsOccupied;}

    public void SetUsageStateImageScale(float InScale) {mUsageStateImageScale = InScale;}

    /** Returns an area rect that is absolute to its owner.
     * Could be still relative in terms of whole world..? kk
     * This is logical coordinate (not for drawing). */
    public Rect GetAbsoluteOnMapAreaRect()
    {
        // I don't know how Java automatically manages dynamically created object..
        // Just guess it's fine to simply return created object..?

        Rect ReturnRect = new Rect(mRelativeAreaRect);

        Rect OwnerRect = mOwnerFacility.GetRelativeArea();
        ReturnRect.left += OwnerRect.left;
        ReturnRect.top += OwnerRect.top;
        ReturnRect.right += OwnerRect.left;
        ReturnRect.bottom += OwnerRect.top;

        return ReturnRect;
    }

    /**
     * Based on GetAbsoluteOnMapAreaRect just above, apply the rendering scale.
     * */
    public Rect GetAbsoluteOnMapAreaDrawRect()
    {
        Rect ReturnRect = GetAbsoluteOnMapAreaRect();

        ReturnRect.left = GetOwner().ApplyRenderScale(ReturnRect.left);
        ReturnRect.top = GetOwner().ApplyRenderScale(ReturnRect.top);
        ReturnRect.right = GetOwner().ApplyRenderScale(ReturnRect.right);
        ReturnRect.bottom = GetOwner().ApplyRenderScale(ReturnRect.bottom);

        return ReturnRect;
    }

    /** Considers image scale in addition to the owner rect */
    public RectF GetUsageImageDrawRect(int ImageWidth, int ImageHeight)
    {
        int DestWidth = (int) ((float)ImageWidth * mUsageStateImageScale);
        int DestHeight = (int) ((float)ImageHeight * mUsageStateImageScale);

        RectF ReturnRect = new RectF(0, 0, DestWidth, DestHeight);

        // Place it at the relative center

        ReturnRect.left += GetRelativeArea().centerX() - DestWidth / 2;
        ReturnRect.top += GetRelativeArea().centerY() - DestHeight / 2;
        ReturnRect.right += GetRelativeArea().centerX() - DestWidth / 2;
        ReturnRect.bottom += GetRelativeArea().centerY() - DestHeight / 2;

        // Finally transform to the owner's location.

        Rect OwnerRect = mOwnerFacility.GetRelativeArea();
        ReturnRect.left += OwnerRect.left;
        ReturnRect.top += OwnerRect.top;
        ReturnRect.right += OwnerRect.left;
        ReturnRect.bottom += OwnerRect.top;

        // Finally, apply the rendering scale
        ReturnRect.left = GetOwner().ApplyRenderScale(ReturnRect.left);
        ReturnRect.top = GetOwner().ApplyRenderScale(ReturnRect.top);
        ReturnRect.right = GetOwner().ApplyRenderScale(ReturnRect.right);
        ReturnRect.bottom = GetOwner().ApplyRenderScale(ReturnRect.bottom);

        return ReturnRect;
    }

}
