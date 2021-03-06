/**
 * [CS442] Mobile Computing, Spring 2015
 *
 * Map image view for building's facility exploration.
 * This is like building's overall viewing class, also contains and manages various sub-facility information.
 * We probably need to separate this into viewing functionality and data management functionality.
 *
 * @author: DJ Song
 * */

package com.example.djsong.mcprjfacmon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcelable;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by DJSong on 2015-03-22.
 */
public class FacExpMapView extends SurfaceView implements SurfaceHolder.Callback
{
    //////////////////////////////////////////////////////////////////////
    // Some object for drawing..

    // Internal buffer bitmap for the internal buffer canvas
    private Bitmap mInternalBufferBitmap = null;

    // Internal buffer canvas to enable final scaling
    private Canvas mInternalBufferCanvas = null;

    private Paint mPaintObject = null;

    /** Real-time asynchronous rendering interface.. */
    private ImageRenderingThread mRenderingThread = null;

    /**
     * Contains bitmap of all floor, with its floor number as the key.
     * */
    private HashMap<Integer, Bitmap> mAllFloorImage = new HashMap<Integer, Bitmap>();
    private Bitmap GetCurrentFloorImage(){
        return mAllFloorImage.get( new Integer(mCurrentFloor) );
    }
    /**
     * Current one among mAllFloorImage
     * */
    private Bitmap mCurrentFloorImage = null;

    /** -1 is B1, no 0 */
    private int mCurrentFloor = 1;
    public int GetCurrentFloor() {return mCurrentFloor;}

    /**
     * Min and Max will be set properly during the init.
     * */
    private int mMinFloor = 1;
    private int mMaxFloor = 1;


    //////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    // Resolutions..

    /**
     * The map's coordinate range that the coordinate data maintained at the server is based on.
     * These are converted by mRenderBufferScale for the rendering to the internal buffer.
     * */
    public static final int mLogicalMapWidth = 2048;
    public static final int mLogicalMapHeight = 2048;

    /**
     * This determines how big will the internal buffer for the rendering.
     * If we confront any kind of performance problem, decrease it.
     * In addition, when you decrease this, consider increase the mPresentScale
     * */
    public static final float mRenderBufferScale = 1.0f;

    public static int GetRenderBufferWidth() { return (int)((float)mLogicalMapWidth * mRenderBufferScale); }
    public static int GetRenderBufferHeight() { return (int)((float)mLogicalMapHeight * mRenderBufferScale); }

    public static int FromMapCoordToRenderCoord(int MapCoordValue)
    {
        return (int)((float)MapCoordValue * mRenderBufferScale);
    }
    public static Point FromMapCoordToRenderCoord(int MapCoordX, int MapCoordY) // Little different version.
    {
        Point RetVal = new Point(0, 0);
        RetVal.x = (int)((float)MapCoordX * mRenderBufferScale);
        RetVal.y = (int)((float)MapCoordY * mRenderBufferScale);
        return RetVal;
    }

    /**
     * Render buffer's draw scale to the map image size.
     * */
    float GetRenderBufferDrawScaleX() { return (float)GetRenderBufferWidth() / (float)mCurrentFloorImage.getWidth(); }
    float GetRenderBufferDrawScaleY() { return (float)GetRenderBufferHeight() / (float)mCurrentFloorImage.getHeight(); }


    /** I am not sure if we got some means to access device size at any time..? */
    private int mCachedScreenSizeX = 720;
    private int mCachedScreenSizeY = 1280;

    //////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    // For interaction..

    // We need interface for translation and scaling of presenting the internal buffer.

    private int mPresentCoordX = 0;
    private int mPresentCoordY = 0;

    private float mPresentScaleX = 1.0f;
    private float mPresentScaleY = 1.0f;

    private float mLastTouchX = 0.0f;
    private float mLastTouchY = 0.0f;

    /**
     * Touch point list for last some duration for circular zooming interaction.
     * */
    private class TimedTouchListElemInfo
    {
        public Point TouchPoint = null;
        public long TimeStamp = 0;

        public TimedTouchListElemInfo(long InTime, int X, int Y)
        {
            TouchPoint = new Point(X, Y);
            TimeStamp = InTime;
        }
    }
    private ArrayList<TimedTouchListElemInfo> mTimedTouchList = new ArrayList<TimedTouchListElemInfo>();
    /**
     * The duration (in millisec) of mTimedTouchList element.
     * */
    private long mTouchListCacheDuration = 600;

    /**
     * These are desired coordinates to be shown at the screen center while zooming in/out.
     * These are internal buffer's coordinate assuming 1.0 scale, so need to be converted to the present coordinate.
     * */
    private int mCachedInternalBufferCenterCoordForZoomX = 0;
    private int mCachedInternalBufferCenterCoordForZoomY = 0;

    /** The circular movement is determined when the total absolute movement is above some standard,
     * while the ratio of the displacement to the abs movement is below some standard. (values below)
     * In addition, the absolute movement is measured relatively to the screen resolution. */
    private static final float mCircularGestureMinAbsMovementToResY = 0.25f;
    private static final float mCircularGestureMaxDisplacementRatioToMovement = 0.15f;
    /**
     * In addition, both x and y movement are needed for the circular gesture.
     * The compared value can be X/Y or Y/X, bigger one will be the divisor, so it won't over 1.0
     * */
    private static final float mCircularGestureMinXtoYorYtoXRatio = 0.5f;

    /** Possible return values for DetectCircularGesture */
    private static final int CIRCULAR_GESTURE_NONE = 0;
    private static final int CIRCULAR_GESTURE_CLOCKWISE = 1;
    private static final int CIRCULAR_GESTURE_COUNTERCLOCKWISE = 2;

    int mCachedLastCircularGestureState = CIRCULAR_GESTURE_NONE;

    private long mLastCircularGestureDetectTime = 0;
    /**
     * Once we detect the circular gesture, it won't be turned off until certain amount of time flows..
     * */
    private static final long mCircularGestureMaintainMinDuration = 200;

    /**
     * Only for the single-touch circular zooming.. Should be bigger than 1.0
     * */
    private static final float mBasicZoomingSensitivity = 1.02f;

    //////////////////////////////
    // It sometimes hard to getting used to the circular gesture stuff.
    // We provide the multi-touch zooming too.

    private int mCachedLastTouchPointNum = 0;

    private float mLastTouchX2 = 0.0f;
    private float mLastTouchY2 = 0.0f;

    /**
     * The initial distance between the two touching point when the multi-touch started.
     * */
    private float mMultiTouchStartDistance = 0.0f;

    private float mMultiTouchStartPresentScaleX = 1.0f;
    private float mMultiTouchStartPresentScaleY = 1.0f;

    /**
     * For the conventional multi-touch zooming..
     * */
    private static final float mMultiTouchZoomingSensitivity = 1.0f;

    // We might better to have multi-touch zooming state variable..

    //////////////////////////////////////////////////////////////////////

    /** The internal data, but they are like rooms, and still have real item one level below. */
    public static ArrayList<FacilityInfoBase> mUsableFacilities;


    public FacExpMapView(Context context)
    {
        super(context);

        mPaintObject = new Paint();

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        mRenderingThread = new ImageRenderingThread(context, holder);

        mLastTouchX = 0.0f;
        mLastTouchY = 0.0f;

        mUsableFacilities = new ArrayList<FacilityInfoBase>();

        // The image of the building map should come from the server for the real world service.
        // We cannot do that for this project, so just put an image at the client side.
        // Most facility creation data are come from the server though..

        // We got an image for the 1st floor of N1 building. kk
        //Bitmap FloorImage_01 = BitmapFactory.decodeResource(getResources(), R.drawable.n1_floor_01_1024x1024);
        Bitmap FloorImage_01 = BitmapFactory.decodeResource(getResources(), R.drawable.n1_floor_01_512x512);
        mAllFloorImage.put( new Integer(1), FloorImage_01 );

        //Bitmap FloorImage_02 = BitmapFactory.decodeResource(getResources(), R.drawable.sample_building_floor_02_1024x1024);
        Bitmap FloorImage_02 = BitmapFactory.decodeResource(getResources(), R.drawable.sample_building_floor_02_512x512);
        mAllFloorImage.put( new Integer(2), FloorImage_02 );

        // They should from the server too, but anyway kk
        mMinFloor = 1;
        mMaxFloor = 2;

        mCurrentFloor = 1; // 1 by default.. we might not have other floor for this semester.. kk
        mCurrentFloorImage = GetCurrentFloorImage();

        // The PresentCoord settings below are just for the N1 floor image.
        Point PresentCoordPoint = FromMapCoordToRenderCoord(-mLogicalMapWidth / 2, -mLogicalMapHeight / 2);
        mPresentCoordX = PresentCoordPoint.x;
        mPresentCoordY = PresentCoordPoint.y;
        mLastTouchX = mPresentCoordX;
        mLastTouchY = mPresentCoordY;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // Any problem if this thread is started right after the creation?
        mRenderingThread.start();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        mCachedScreenSizeX = width;
        mCachedScreenSizeY = height;

        CreateInternalBuffer(width, height);
        UpdateInternalBuffer();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            mRenderingThread.join();
        } catch (InterruptedException ex) { }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        // What would be the circumstance of size change..?

        // Guess the w/h is just same to the width/height of surfaceChanged?
        // Let's leave it empty..
        //mCachedScreenSizeX = w;
        //mCachedScreenSizeY = h;

        //CreateInternalBuffer(w, h);
        //UpdateInternalBuffer();
    }

    private void CreateInternalBuffer(int w, int h)
    {
        // Well, just create with fixed size for now..
        mInternalBufferBitmap = Bitmap.createBitmap(GetRenderBufferWidth(), GetRenderBufferHeight(), Bitmap.Config.ARGB_8888);
        mInternalBufferCanvas = new Canvas();
        mInternalBufferCanvas.setBitmap(mInternalBufferBitmap);
    }

    public void UpdateInternalBuffer()
    {
        // The actual drawing process, but not presenting to the display
        // Call this for some event, or in real-time.. at least PresentToCanvas will be called in real time

        if(mInternalBufferBitmap != null && mInternalBufferCanvas != null)
        {
            mInternalBufferCanvas.drawColor(Color.WHITE); // Clearing

            mCurrentFloorImage = GetCurrentFloorImage(); // Could be set by some variable later..

            if(mCurrentFloorImage != null) {
                // Why the size is different from expected..?
                RectF DestRect = new RectF(0, 0, GetRenderBufferWidth(), GetRenderBufferHeight());
                Rect SrcRect = new Rect(0, 0, mCurrentFloorImage.getWidth(), mCurrentFloorImage.getHeight());
                mInternalBufferCanvas.drawBitmap(mCurrentFloorImage, SrcRect, DestRect, mPaintObject);
            }

            for (int FacIndex = 0; FacIndex < mUsableFacilities.size(); ++FacIndex) {
                FacilityInfoBase CurrFacility = mUsableFacilities.get(FacIndex);
                // Draw facilities only for current floor.
                if (CurrFacility.GetFloorNumber() == mCurrentFloor) {
                    CurrFacility.OnMapDrawing(mInternalBufferCanvas);
                }
            }
        }
    }

    /**
     * Safe cycling of floor number, and it update the buffer with new floor image too.
     * */
    public void CycleCurrentFloor(){
        mCurrentFloor++;
        if(mCurrentFloor == 0) { // No zero
            mCurrentFloor = 1;
        }
        if(mCurrentFloor > mMaxFloor) {
            mCurrentFloor = mMinFloor;
        }
        UpdateInternalBuffer();
    }

    protected float GetPresentWidth() { return mInternalBufferBitmap.getWidth() * mPresentScaleX; }
    protected float GetPresentHeight() { return mInternalBufferBitmap.getHeight() * mPresentScaleY; }
    protected RectF GetPresentDestRect()
    {
        RectF ReturnRect = new RectF(mPresentCoordX, mPresentCoordY,
                GetPresentWidth() + mPresentCoordX, GetPresentHeight() + mPresentCoordY);
        return ReturnRect;
    }

    protected void PresentToCanvas(Canvas canvas)
    {
        // Here, we present the updated buffer (mInternalBufferBitmap) to the main canvas.
        // The scale and draw coordinate will be adjusted..

        if(mInternalBufferBitmap != null && canvas != null)
        {
            RectF DestRect = GetPresentDestRect();
            Rect SrcRect = new Rect(0, 0, mInternalBufferBitmap.getWidth(), mInternalBufferBitmap.getHeight());
            canvas.drawBitmap(mInternalBufferBitmap, SrcRect, DestRect, null);

            //Log.d("MapView", "DestRect " + DestRect.toString());
            //Log.d("MapView", "Center " + GetInternalBufferCoordOfScreenCenter().toString());
        }
    }

    /** For map translation and scaling.. */
    public boolean onTouchEvent(MotionEvent event)
    {
        int action = event.getAction();

        int TouchPointNum = event.getPointerCount();

        float X = event.getX();
        float Y = event.getY();

/* Tried multi-touch zooming
        float X2 = (TouchPointNum >= 2) ? event.getX(1) : 0.0f;
        float Y2 = (TouchPointNum >= 2) ? event.getY(1) : 0.0f;

        float DistanceBetweenTwoTouchPoints = (TouchPointNum >= 2) ?
                FloatMath.sqrt( (X - X2) * (X - X2) + (Y - Y2) * (Y - Y2) ) : 0.0f;
*/

        // When this is true, no circular gesture.
        boolean bDoMultiTouchZooming = false;

        float MovingDeltaX = 0.0f;
        float MovingDeltaY = 0.0f;

        UpdateTimedTouchList((int)X, (int)Y);
        int CircularGestureState = CIRCULAR_GESTURE_NONE;

        switch (action) {
            case MotionEvent.ACTION_UP:

                break;
            case MotionEvent.ACTION_DOWN:

/* Tried multi-touch zooming
                if(mCachedLastTouchPointNum < 2 && TouchPointNum >= 2)
                {
                    // Start the multi-touch zooming by save the initial distance between the two points.
                    mMultiTouchStartDistance = DistanceBetweenTwoTouchPoints;

                    mMultiTouchStartPresentScaleX = mPresentScaleX;
                    mMultiTouchStartPresentScaleY = mPresentScaleY;

                    // Let's make little similar stuff for the circular zooming..
                    // Cache the desired center coordinate when zooming is started.
                    Point CachedCenterCoord = GetInternalBufferCoordOfScreenCoord(mCachedScreenSizeX/2, mCachedScreenSizeY/2);
                    mCachedInternalBufferCenterCoordForZoomX = CachedCenterCoord.x;
                    mCachedInternalBufferCenterCoordForZoomY = CachedCenterCoord.y;
                }
*/

                break;
            case MotionEvent.ACTION_MOVE:
                // Relative movement control. We probably introduce some sort of velocity..
                MovingDeltaX = (X - mLastTouchX);
                MovingDeltaY = (Y - mLastTouchY);

                // For zoom in/out with single touching event. I just have no actual device, so had to implement single touch zooming interface.
                // The basic idea of using circular gesture for zooming action is from
                // Malacria, S., Lecolinet, E., and Guiard, Y. (2010, April).
                // "Clutch-free panning and integrated pan-zoom control on touch-sensitive surfaces: the cyclostar approach."
                // In Proceedings of the SIGCHI Conference on Human Factors in Computing Systems (pp. 2615-2624).
                final int OriginallyDetectedCircularGestureState = DetectCircularGesture();
                // This can be set to have different value..
                CircularGestureState = OriginallyDetectedCircularGestureState;

                // Even for non-circular gesture, we maintain the circular gesture a little period of time,
                // if we previously detected circular gesture.
                if(OriginallyDetectedCircularGestureState != CIRCULAR_GESTURE_NONE)
                {
                    mLastCircularGestureDetectTime = System.currentTimeMillis();
                }
                else //if(OriginallyDetectedCircularGestureState == CIRCULAR_GESTURE_NONE)
                {
                    if(mCachedLastCircularGestureState != CIRCULAR_GESTURE_NONE) {
                        if (Math.abs(mLastCircularGestureDetectTime - System.currentTimeMillis()) < mCircularGestureMaintainMinDuration)
                        {
                            CircularGestureState = mCachedLastCircularGestureState;
                        }
                    }
                }

/* Tried multi-touch zooming
                // No circular gesture for the multi-touch contact.
                if(TouchPointNum >= 2)
                {
                    CircularGestureState = CIRCULAR_GESTURE_NONE;
                    bDoMultiTouchZooming = true;
                }
*/

                // Cache the wanted center coordinate of the internal buffer if circular gesture is started or changed direction.
                if(CircularGestureState != CIRCULAR_GESTURE_NONE && CircularGestureState != mCachedLastCircularGestureState)
                {
                    Point TouchListCenterCoord = GetTimedTouchListCenter();
                    Point CachedCenterCoord = GetInternalBufferCoordOfScreenCoord(TouchListCenterCoord.x, TouchListCenterCoord.y);
                    mCachedInternalBufferCenterCoordForZoomX = CachedCenterCoord.x;
                    mCachedInternalBufferCenterCoordForZoomY = CachedCenterCoord.y;
                }

                break;
        }

/* Tried multi-touch zooming
        if(bDoMultiTouchZooming)
        {
            // Handle multi-touch contact zooming for continuous multi-touching drag..
            mPresentScaleX = mMultiTouchZoomingSensitivity * (DistanceBetweenTwoTouchPoints / mMultiTouchStartDistance)
                    * mMultiTouchStartPresentScaleX;

            mPresentScaleY = mMultiTouchZoomingSensitivity * (DistanceBetweenTwoTouchPoints / mMultiTouchStartDistance)
                    * mMultiTouchStartPresentScaleY;

            // Similarly to the circular zooming, adjust the present coordinate.
            Point PresentCoordForZoom = GetPresentCoordFromWantedInternalBufferCenterCoord(
                    mCachedInternalBufferCenterCoordForZoomX, mCachedInternalBufferCenterCoordForZoomY);
            mPresentCoordX = PresentCoordForZoom.x;
            mPresentCoordY = PresentCoordForZoom.y;

        }
        else */
        if(CircularGestureState != CIRCULAR_GESTURE_NONE)
        {
            // Do the zooming action for circular gesture.
            if(CircularGestureState == CIRCULAR_GESTURE_CLOCKWISE)
            {
                mPresentScaleX *= mBasicZoomingSensitivity;
                mPresentScaleY *= mBasicZoomingSensitivity;
            }
            else if(CircularGestureState == CIRCULAR_GESTURE_COUNTERCLOCKWISE)
            {
                mPresentScaleX /= mBasicZoomingSensitivity;
                mPresentScaleY /= mBasicZoomingSensitivity;
            }

            // Derive the present coordinate for showing the desired zooming spot at the center during the zooming action

            Point PresentCoordForZoom = GetPresentCoordFromWantedInternalBufferCenterCoord(
                    mCachedInternalBufferCenterCoordForZoomX, mCachedInternalBufferCenterCoordForZoomY);
            mPresentCoordX = PresentCoordForZoom.x;
            mPresentCoordY = PresentCoordForZoom.y;

            //Log.d("MapView", " Internal " + mCachedInternalBufferCenterCoordForZoomX + " " + mCachedInternalBufferCenterCoordForZoomY +
              //      " Present " + mPresentCoordX + " " + mPresentCoordY);
        }
        // While not zooming, scrolling based on the touching action.
        else
        {
            mPresentCoordX += (int)MovingDeltaX;
            mPresentCoordY += (int)MovingDeltaY;
        }

        // Clamp mPresentCoord to prevent the map getting out of screen bound.
        mPresentCoordX = Math.min(mPresentCoordX, 0);
        mPresentCoordY = Math.min(mPresentCoordY, 0);
        mPresentCoordX = Math.max( mPresentCoordX, -1 * ((int) GetPresentWidth() - mCachedScreenSizeX) );
        mPresentCoordY = Math.max( mPresentCoordY, -1 * ((int) GetPresentHeight() - mCachedScreenSizeY) );

        mLastTouchX = X;
        mLastTouchY = Y;
/* Tried multi-touch zooming
        mLastTouchX2 = X2;
        mLastTouchY2 = Y2;
*/

        mCachedLastCircularGestureState = CircularGestureState;

        mCachedLastTouchPointNum = TouchPointNum;

        return true;
    }

    /**
     * Update the elements of mTimedTouchList
     * */
    private void UpdateTimedTouchList(int NewX, int NewY)
    {
        long CurrTime = System.currentTimeMillis();

        mTimedTouchList.add(new TimedTouchListElemInfo(CurrTime, NewX, NewY));

        // Remove old one
        for(int EI = 0; EI < mTimedTouchList.size(); ++EI)
        {
            TimedTouchListElemInfo CurrInfo = mTimedTouchList.get(EI);
            if( Math.abs(CurrInfo.TimeStamp - CurrTime) > mTouchListCacheDuration )
            {
                mTimedTouchList.remove(EI);
                --EI; // Removed current index element, so make it to get the object at the same index for next iteration.
            }
        }
    }

    /**
     * See if touching gesture for last some period was a circular movement.
     * */
    private int DetectCircularGesture()
    {
        // The basic idea of using circular gesture for zooming action is from
        // Malacria, S., Lecolinet, E., and Guiard, Y. (2010, April).
        // "Clutch-free panning and integrated pan-zoom control on touch-sensitive surfaces: the cyclostar approach."
        // In Proceedings of the SIGCHI Conference on Human Factors in Computing Systems (pp. 2615-2624).

        int RetVal = CIRCULAR_GESTURE_NONE;

        // Add the absolute movement
        float TotalAbsMovement = 0.0f;

        float TotalXMove = 0.0f;
        float TotalYMove = 0.0f;


        // I don't know if there is any vector class in default android library. Just regard Point as 2D vector.
        Point TotalDisplacementVector = new Point(0, 0);
        for(int EI = 0; EI < mTimedTouchList.size() - 1; ++EI)
        {
            TimedTouchListElemInfo CurrInfo = mTimedTouchList.get(EI);
            TimedTouchListElemInfo NextInfo = mTimedTouchList.get(EI + 1);

            Point CurrToNextVector = new Point( NextInfo.TouchPoint.x - CurrInfo.TouchPoint.x, NextInfo.TouchPoint.y - CurrInfo.TouchPoint.y );

            // Just add the abs values.
            TotalAbsMovement += (float)Math.sqrt( (double)(CurrToNextVector.x * CurrToNextVector.x + CurrToNextVector.y * CurrToNextVector.y) );

            TotalXMove += (float)Math.abs(CurrToNextVector.x);
            TotalYMove += (float)Math.abs(CurrToNextVector.y);

            // This care about the sign.
            TotalDisplacementVector.x += CurrToNextVector.x;
            TotalDisplacementVector.y += CurrToNextVector.y;
        }

        float TotalDisplacement = (float)Math.sqrt( (double)(TotalDisplacementVector.x * TotalDisplacementVector.x + TotalDisplacementVector.y * TotalDisplacementVector.y) );

        float XtoYorYtoXRatio = 0.0f;
        if(TotalXMove > 0.0f && TotalYMove > 0.0f)
        {
            // X/Y or Y/X, whatever bigger will be the divisor.
            XtoYorYtoXRatio = (TotalXMove >= TotalYMove) ? (TotalYMove / TotalXMove) : (TotalXMove / TotalYMove);
        }

        if(TotalAbsMovement > 0.0f)
        {
            //Log.d("MapView", "ABS " + TotalAbsMovement + " DispRatio " + TotalDisplacement / TotalAbsMovement + " Ratio " + XtoYorYtoXRatio);

            // The condition for the circular gesture.
            if( TotalAbsMovement / (float)mCachedScreenSizeY >= mCircularGestureMinAbsMovementToResY &&
                // This means the movement almost came back to its original starting point
                TotalDisplacement / TotalAbsMovement <= mCircularGestureMaxDisplacementRatioToMovement &&
                // Not like one dimensional movement.
                XtoYorYtoXRatio >= mCircularGestureMinXtoYorYtoXRatio )
            {
                // Now, let's decide if clockwise or counter clockwise.

                // Sample 3 points from first to half point of the touch list
                int TouchLostHalfSize = mTimedTouchList.size() / 2;
                if(TouchLostHalfSize >= 2)
                {
                    Point SampleP0 = mTimedTouchList.get(0).TouchPoint;
                    Point SampleP1 = mTimedTouchList.get(TouchLostHalfSize / 2).TouchPoint;
                    Point SampleP2 = mTimedTouchList.get(TouchLostHalfSize).TouchPoint;

                    if(AreThreePointsInClockwise(SampleP0, SampleP1, SampleP2) == true)
                    {
                        RetVal = CIRCULAR_GESTURE_CLOCKWISE;
                    }
                    else
                    {
                        RetVal = CIRCULAR_GESTURE_COUNTERCLOCKWISE;
                    }
                }
            }
        }

        return RetVal;
    }

    private boolean AreThreePointsInClockwise(Point P0, Point P1, Point P2)
    {
        Point P1toP0 = new Point(P0.x - P1.x, P0.y - P1.y);
        Point P1toP2 = new Point(P2.x - P1.x, P2.y - P1.y);

        // I am not really sure.. just done something like cross product of 3D vector. Looks like working.

        int Det = P1toP0.x * P1toP2.y - P1toP0.y * P1toP2.x;

        if(Det <= 0) {
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Just the averaged point..
     * */
    private Point GetTimedTouchListCenter()
    {
        int ListSize = mTimedTouchList.size();

        if(ListSize <= 0)
        {
            return new Point(0, 0);
        }

        int AvgX = 0;
        int AvgY = 0;

        for(int EI = 0; EI < ListSize; ++EI)
        {
            TimedTouchListElemInfo CurrInfo = mTimedTouchList.get(EI);
            AvgX += CurrInfo.TouchPoint.x;
            AvgY += CurrInfo.TouchPoint.y;
        }

        return new Point( AvgX / ListSize, AvgY / ListSize );
    }

    /**
     * Based on the present coordinate (upper left origin),
     * get the internal buffer coordinate at designated screen's coordinate assuming 1.0 scale
     * */
    private Point GetInternalBufferCoordOfScreenCoord(int ScreenX, int ScreenY)
    {
        int RetX = (int)( (float)(-1 * mPresentCoordX + ScreenX) / mPresentScaleX );
        int RetY = (int)( (float)(-1 * mPresentCoordY + ScreenY) / mPresentScaleY );
        Point RetCoord = new Point(RetX, RetY);

        return RetCoord;
    }

    /**
     * It derives final present coordinate based on the specified internal buffer's center coordinate (assuming 1.0 scale) and current present scale.
     * The return value of this is intended to be assigned to mPresentCoordX/Y.
     * */
    private Point GetPresentCoordFromWantedInternalBufferCenterCoord(int WantedCenterX, int WantedCenterY)
    {
        int RetX = -1 * (int)( (float)(WantedCenterX * mPresentScaleX - mCachedScreenSizeX/2) );
        int RetY = -1 * (int)( (float)(WantedCenterY * mPresentScaleY - mCachedScreenSizeY/2) );

        Point RetCoord = new Point(RetX, RetY);

        return RetCoord;
    }

    /** Calling this to set all item info gotten from server might be slow.. Setting all at once is better
     * However, How to ensure the packet from server contains item data in expected order?
     * @return returns true if the occupied state is changed. */
    public boolean SetSingleItemOccupied(int FloorNumber, int RoomNumber, int ItemIndex, boolean bInOccupied)
    {
        boolean bIsChanged = false;

        for(int FacIndex = 0; FacIndex < mUsableFacilities.size(); ++FacIndex)
        {
            FacilityInfoBase CurrFacility = mUsableFacilities.get(FacIndex);
            // FloorNumber and RoomNumber together can serve as an unique identifier of a facility.
            if(CurrFacility.GetFloorNumber() == FloorNumber && CurrFacility.GetRoomNumber() == RoomNumber)
            {
                if(CurrFacility.GetSingleItemOccupied(ItemIndex) != bInOccupied)
                {
                    bIsChanged = true;
                }

                CurrFacility.SetSingleItemOccupied(ItemIndex, bInOccupied);
                break;
            }
        }

        return bIsChanged;
    }

    //////////////////////////////////////////////////////////////////////
    // Packet callbacks..

    public void OnRecvPacket_Cli_UsageState(CommPacketDef_Cli_UsageState InRecvPacket)
    {
        boolean bAtLeastOneChanged = false;
        for(int DataIndex = 0; DataIndex < InRecvPacket.mDataArray.size(); ++DataIndex)
        {
            TransmitFacilityItemData CurrData = InRecvPacket.mDataArray.get(DataIndex);
            if( SetSingleItemOccupied(CurrData.mFloorNumber, CurrData.mRoomNumber, CurrData.mItemNumber, CurrData.mbIsOccupied) == true )
            {
                bAtLeastOneChanged = true;
            }
        }

        // Update drawing if any occupied state is changed.
        if(bAtLeastOneChanged) {

            UpdateInternalBuffer();

        }
    }

    public void OnRecvPacket_Cli_FacInit(CommPacketDef_Cli_FacInit InRecvPacket)
    {
        // Create new facilities

        // Any possible thread safety issue here?

        for(int NewFacIndex = 0; NewFacIndex < InRecvPacket.mDataArray.size(); ++NewFacIndex)
        {
            TransmitFacilityCreateData CurrData = InRecvPacket.mDataArray.get(NewFacIndex);

            FacilityInfoBase NewFacData = null;
            if(CurrData.mFacilityType == CommPacketDef.FACTYPE_RESTROOM)
            {
                // The facility area data from the server is based on logical coordinate.
                // They will be scale to the internal buffer's resolution at inside of the class.
                NewFacData = new FacRestroomInfo(this, CurrData.mFloorNumber, CurrData.mRoomNumber, CurrData.mTotalItemNumber,
                        new Rect(CurrData.mRelativeAreaLeft, CurrData.mRelativeAreaTop, CurrData.mRelativeAreaRight, CurrData.mRelativeAreaBottom),
                        CurrData.mbMaleRestRoom);
            }
            else if(CurrData.mFacilityType == CommPacketDef.FACTYPE_PARKINGLOT)
            {
                // Nothing defined currently for the parking lot.
            }

            if(NewFacData != null)
            {
                // For sub-item creation (hack for client)
                NewFacData.LoadUpItemData(null);
                mUsableFacilities.add(NewFacData);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    // ImageRenderingThread.. for real time rendering

    class ImageRenderingThread extends Thread
    {
        SurfaceHolder mHolder;

        /**
         * The minimum time between rendering frame. Inverse of FPS, but in millisecond unit
         * */
        long mMinFrameTime = 33;

        public ImageRenderingThread(Context InContext, SurfaceHolder InHolder)
        {
            super();

            mHolder = InHolder;
        }

        public void run()
        {
            boolean bLoop = true;
            while (bLoop)
            {
                long StartTickTime = System.currentTimeMillis();

                Canvas LockedCanvas = mHolder.lockCanvas(null);
                synchronized (mHolder) {
                    //UpdateInternalBuffer(); // Probably no need to call this in real time.. but we might need eventually..?
                    PresentToCanvas(LockedCanvas);
                }

                if (LockedCanvas != null) {
                    mHolder.unlockCanvasAndPost(LockedCanvas);
                }

                // Frame rate controlling

                long EndTickTime = System.currentTimeMillis();
                // Use the abs value because I guess the currentTimeMillis might return reset value at some time..?
                long FrameDelta = Math.abs(EndTickTime - StartTickTime);
                if(FrameDelta < mMinFrameTime)
                {
                    try {
                        sleep(mMinFrameTime - FrameDelta);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    //////////////////////////////////////////////////////////////////////
}

