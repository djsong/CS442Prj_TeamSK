/**
 * [CS442] Mobile Computing, Spring 2015
 *
 * The base class for any facility types in the building.
 * To be used both for on-map drawing and its own main screen.
 *
 * @author: DJ Song
 * */

package com.example.djsong.mcprjfacmon;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by DJSong on 2015-03-26.
 */
public class FacilityInfoBase
{
    /** its (owner) building's reference. Probably not really needed for this semester though.. */
    private FacExpMapView mOwnerBuildingView;

    /**
     * Might not need this..? but just for convenience anyway..
     * Let's use constants defined in the CommPacketDef.
     * This needs to be set at the constructor of each sub-class.
     * */
    protected int mFacilityType = CommPacketDef.FACTYPE_UNKNOWN;

    /** -1 is B1, no 0 */
    private int mFloorNumber;
    /** This might also has floor information */
    private int mRoomNumber;

    /** Those are the real stuff in here.
     * We are interested to see the occupation state of a single item. */
    protected ArrayList<SingleItemInfoBase> mUsableItems;

    /**
     * This is simply the array size of mUsableItems.
     * This is set before adding the elements to the mUsableItems. (comes from the server)
     * Not for some sort of reserving memory, just for knowing how many sub-items have to be created.
     * Kinda like a hack..
     * */
    protected int mSpecifiedTotalItemNum;

    /** Basic dimension relative to its owner */
    private Rect mRelativeAreaRect;

    protected Paint mDrawPaint;

    /** Set those base on-map drawing parameters at the constructor of sub-class.
     * Not for detailed drawing. */
    protected int mBaseOnMapDrawColorA = 255;
    protected int mBaseOnMapDrawColorR = 0;
    protected int mBaseOnMapDrawColorG = 0;
    protected int mBaseOnMapDrawColorB = 0;
    protected float mBaseOnMapDrawStrokeWidth = 5.0f;
    protected boolean mbFillBaseOnMapDraw = false;

    public FacilityInfoBase(FacExpMapView InOwner, int InFloorNum, int InRoomNum, int InTotalItemNum, Rect InRelativeAreaRect)
    {
        mOwnerBuildingView = InOwner;
        mFloorNumber = InFloorNum;
        mRoomNumber = InRoomNum;

        mRelativeAreaRect = new Rect(InRelativeAreaRect);

        mUsableItems = new ArrayList<SingleItemInfoBase>();
        // Create items according to this number later. This is just for simplifying our job for this project..
        mSpecifiedTotalItemNum = InTotalItemNum;

        mDrawPaint = new Paint();
    }

    /**
     * Conceptually, this is supposed to load up the sub-item data of facilities (probably get from the server)
     * For now, this is just a dummy and we just hard-code some sample data somewhere..
     * */
    public boolean LoadUpItemData(Parcelable InDummyForNow)
    {

        return true;
    }

    /** Direct On-map drawing. Could be like an icon.. or the main drawing? */
    public void OnMapDrawing(Canvas InDrawCanvas)
    {
        // Draw just an area box, and any additional stuff at sub-class..
        if(mbFillBaseOnMapDraw) {
            mDrawPaint.setStyle(Paint.Style.FILL);
        }
        else{
            mDrawPaint.setStyle(Paint.Style.STROKE);
        }
        mDrawPaint.setStrokeWidth(mBaseOnMapDrawStrokeWidth);
        mDrawPaint.setARGB(mBaseOnMapDrawColorA, mBaseOnMapDrawColorR, mBaseOnMapDrawColorG, mBaseOnMapDrawColorB);
        InDrawCanvas.drawRect(mRelativeAreaRect.left, mRelativeAreaRect.top,
                mRelativeAreaRect.right, mRelativeAreaRect.bottom, mDrawPaint);


        // Draw all the sub-items
        for(int ItemIndex = 0; ItemIndex < mUsableItems.size(); ++ItemIndex)
        {
            mUsableItems.get(ItemIndex).OnFacilityMapDrawing(InDrawCanvas, mDrawPaint);
        }

    }

    /** Drawing for its own pop-up screen. */
    public void DetailedDrawing(Canvas InDrawCanvas)
    {
        // Draw all the sub-items
        for(int ItemIndex = 0; ItemIndex < mUsableItems.size(); ++ItemIndex)
        {
            mUsableItems.get(ItemIndex).OnFacilityDetailedDrawing(InDrawCanvas, mDrawPaint);
        }
    }

    /** When we get some data from the server, we are going to call this to apply the received data */
    public void SetSingleItemOccupied(int ItemIndex, boolean bInOccupied)
    {
        if(ItemIndex >= 0 && ItemIndex < mUsableItems.size())
        {
            mUsableItems.get(ItemIndex).SetOccupied(bInOccupied);
        }
    }
    public boolean GetSingleItemOccupied(int ItemIndex)
    {
        if(ItemIndex >= 0 && ItemIndex < mUsableItems.size())
        {
            return mUsableItems.get(ItemIndex).IsOccupied();
        }
        return false;
    }

    public FacExpMapView GetOwner() {return mOwnerBuildingView;}
    public int GetFacilityType() {return mFacilityType;}
    public int GetFloorNumber() {return mFloorNumber;}
    public int GetRoomNumber() {return mRoomNumber;}

    public Rect GetRelativeArea() {return mRelativeAreaRect;}
}
