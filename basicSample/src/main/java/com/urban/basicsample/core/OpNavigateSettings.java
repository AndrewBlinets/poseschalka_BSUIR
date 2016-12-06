package com.urban.basicsample.core;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.digitalpersona.android.ptapi.PtConstants;

public class OpNavigateSettings
{
    public class LongTouchMode 
    {
        
        /** Don't report long touch. */
        public static final int DISABLED = 0;
        
        /** Report long touch only once in a moment it occurs. Not supported in system navigation. */
        public static final int ONCE = 1;
        
        /** Report long touch repeatedly until finger moves or it is lifted. */
        public static final int STICKY = 2;
    }
    
    @SuppressWarnings("unused")
    private static final long serialVersionUID = -4217723181578697347L;
    
    // Size of serialized byte array, verified by C library
    private static final int NAV_PROP_SERIALIZED_LENGTH = 84;
  
    //Android pop-ups context menu after cca. 1.6s after left button press
    private static final int LONG_TOUCH_ANDROID_INTERVAL = 1600;
    
    /** Delivery of click events enabling. */
    public boolean clickEnabled = true;
    
    /** Processing of tapping click enabled. */
    public boolean tappingClickProcessingEnabled = true;
    
    /** Processing of pressure click enabled. */
    public boolean pressureClickProcessingEnabled = false;
    
    /** Sensor push button used for press and click operations. */
    public boolean sensorButtonEnabled = false;
    
    /** Hardware navigation is used instead of software one. */
    public boolean hwNavigationEnabled = false;
    
    /** Parameters specific to post-processing library mode. */
    public Serializable modeParams = null;
    
    /** Sensor sensitivity in horizontal direction. Input data are multiplied by this value before other processing. */
    public float inputMultiplierX = 1.0f;
    
    /** Sensor sensitivity in vertical direction. Input data are multiplied by this value before other processing. */
    public float inputMultiplierY = 1.0f;
    
    /** Long touch mode. */
    public int longTouchMode = LongTouchMode.STICKY;

    /** Time from user touch of a sensor to long touch system event raising. */
    public int longTouchTime = 3000;
    
    /** Maximum total allowed movement (in pixels) during a "long touch". */
    public int longTouchMaxMovement = 0;
    
    
    public class MouseModeParams implements Serializable
    {
        private static final long serialVersionUID = -1878985172574647855L;
        
        /** If the speed of the cursor gets beyond this value, the cursor gets accelerated.
         * The unit is pixels per 10 milliseconds. */
        public int cursorAccelThreshold = 10;
        
        /** Acceleration  factor of the cursor (1.0 = no acceleration). */
        public float cursorAccelMultiplier = 1.0f;
        
        /** Maximum cursor speed. The unit is pixels per 10 milliseconds. */
        public int cursorMaxSpeed = 5;
        
        /** Scaling factor in direction X. Speed of movement in X-direction is divided by this value. */
        public int gridDivisorX = 1;
        
        /** Scaling factor in direction Y. Speed of movement in Y-direction is divided by this value. */
        public int gridDivisorY = 1;
        
        /** Arctg(straighteningFactor) represents an angle in a sensor plane, in which speed vector is clamped to
         * the closest axis.
         * 0 = no straightening (special value); 1 = maximal straightening; 3 = straighten, if abs(dy) >= 3*abs(dx) or abs(dx) >= 3*abs(dy)*/
        public int straighteningFactor = 0;
        
        /** Maximal speed at the beginning of inertial movement. 0 = do not use inertia. */
        public float inertialMaxSpeed = 0.0f;
        
        /** Inertial friction.
         *  0 = do not slow down at all; 1 = halve the speed each second; 2 = quarter the speed each second; etc ... */
        public float inertialFriction = 0.0f;
        
        /** 
         * If bigger than zero, then after a finger stops move (but it is not lifted), the cursor continues to move 
         * in the same direction. The speed will be equal to the speed just before the finger stopped times 
         * repeatedSpeedMultiplier.
         */
        public float repeatedSpeedMultiplier = 1.0f;
        
        /** The delay before the start of the repetition. */
        public int repeatDelay = 50;

        /** The speed at which the finger is still considered not moving and thus repetition is run. */
        public float noFingerMaxSpeed = 0;        
        
        public MouseModeParams()
        {
        }
        
        public MouseModeParams(MouseModeParams orig)
        {
            this.cursorAccelThreshold = orig.cursorAccelThreshold;
            this.cursorAccelMultiplier = orig.cursorAccelMultiplier;
            this.cursorMaxSpeed = orig.cursorMaxSpeed;
            this.gridDivisorX = orig.gridDivisorX;
            this.gridDivisorY = orig.gridDivisorY;
            this.straighteningFactor = orig.straighteningFactor;
            this.inertialMaxSpeed = orig.inertialMaxSpeed;
            this.inertialFriction = orig.inertialFriction;
            this.repeatedSpeedMultiplier = orig.repeatedSpeedMultiplier;
            this.repeatDelay = orig.repeatDelay;
            this.noFingerMaxSpeed = orig.noFingerMaxSpeed;
        }

        public MouseModeParams(float inputMultiplierX, float inputMultiplierY,
                int cursorAccelThreshold, float cursorAccelMultiplier,
                int cursorMaxSpeed, int gridDivisorX, int gridDivisorY,
                int straighteningFactor, float maxInertialSpeed,
                float inertialFriction, float repeatedSpeedMultiplier,
                int repeatDelay, float noFingerMaxSpeed) 
        {
            this.cursorAccelThreshold = cursorAccelThreshold;
            this.cursorAccelMultiplier = cursorAccelMultiplier;
            this.cursorMaxSpeed = cursorMaxSpeed;
            this.gridDivisorX = gridDivisorX;
            this.gridDivisorY = gridDivisorY;
            this.straighteningFactor = straighteningFactor;
            this.inertialMaxSpeed = maxInertialSpeed;
            this.inertialFriction = inertialFriction;
            this.repeatedSpeedMultiplier = repeatedSpeedMultiplier;
            this.repeatDelay = repeatDelay;
            this.noFingerMaxSpeed = noFingerMaxSpeed;
        }
    }
    
    public class ArrowModeParams implements Serializable
    {
        private static final long serialVersionUID = -7946894592543665156L;
        
        /** Movement of the cursor in pixels (in the direction of both axes) which is considered a keystroke. */
        public int  keyThreshold = 20;

        /** Delay between first and second keystroke (in ms). */
        public int  keyRepeatDelay = 300;

        /** Delay between keystrokes (except for delay between the first and the second one) (in ms). */
        public int  keyRepeatRate = 200;

        /** Turns on/off the key-stroke acceleration. */
        public boolean  accelEnabled = false;

        /** Delay between the second keystroke and the start of linear acceleration (in ms). */
        public int  preAccelPeriodLength = 2000;

        /** Time it takes to accelerate from the low speed (period=keyRepeatRate) to high speed (period=fastKeyRepeatRate). */
        public int  accelPeriodLength = 0;

        /** Speed that is reached at the end of the keystroke acceleration. */
        public int  fastKeyRepeatRate = 100;
        
        public ArrowModeParams()
        {

        }
        
        public ArrowModeParams(ArrowModeParams orig)
        {
            this.keyThreshold = orig.keyThreshold;
            this.keyRepeatDelay = orig.keyRepeatDelay;
            this.keyRepeatRate = orig.keyRepeatRate;
            this.accelEnabled = orig.accelEnabled;
            this.preAccelPeriodLength = orig.preAccelPeriodLength;
            this.accelPeriodLength = orig.accelPeriodLength;
            this.fastKeyRepeatRate = orig.fastKeyRepeatRate;
        }

        public ArrowModeParams(int keyThreshold, int keyRepeatDelay,
                int keyRepeatRate, boolean accelEnabled,
                int preAccelPeriodLength, int accelPeriodLength,
                int fastKeyRepeatRate)
        {
            this.keyThreshold = keyThreshold;
            this.keyRepeatDelay = keyRepeatDelay;
            this.keyRepeatRate = keyRepeatRate;
            this.accelEnabled = accelEnabled;
            this.preAccelPeriodLength = preAccelPeriodLength;
            this.accelPeriodLength = accelPeriodLength;
            this.fastKeyRepeatRate = fastKeyRepeatRate;
        }
    }
    
    /** Constructor. Image headers initialized to default values. */
    public OpNavigateSettings()
    {
        this.modeParams = new ArrowModeParams();
    }

    /**
     * Constructor providing image deep copy.
     * @param orig Source property.
     */
    public OpNavigateSettings(OpNavigateSettings orig)
    {
        this.inputMultiplierX               = orig.inputMultiplierX;
        this.inputMultiplierY               = orig.inputMultiplierY;
        
        this.longTouchTime                  = orig.longTouchTime;
        this.longTouchMaxMovement           = orig.longTouchMaxMovement;
        this.longTouchMode                  = orig.longTouchMode;
        
        this.clickEnabled                   = orig.clickEnabled;
        this.hwNavigationEnabled            = orig.hwNavigationEnabled;
        this.pressureClickProcessingEnabled = orig.pressureClickProcessingEnabled;
        this.sensorButtonEnabled            = orig.sensorButtonEnabled;
        this.tappingClickProcessingEnabled  = orig.tappingClickProcessingEnabled;
        
        if(orig.modeParams instanceof MouseModeParams)
        {
            this.modeParams = new MouseModeParams((MouseModeParams)orig.modeParams);
        }
        else 
        {
            this.modeParams = new ArrowModeParams((ArrowModeParams)orig.modeParams);
        }
    }
    
    /** Create instance with default values for mouse post-processing.
     * @return Property instance
     */
    public static OpNavigateSettings createDefaultMousePostprocessingParams()
    {
        OpNavigateSettings prop = new OpNavigateSettings();
        prop.modeParams = prop.new MouseModeParams();
        return prop;
    }
    
    /** Create instance with default values for discrete post-processing.
     * @return Property instance
     */
    public static OpNavigateSettings createDefaultDiscretePostprocessingParams()
    {
        OpNavigateSettings prop = new OpNavigateSettings();
        prop.modeParams = prop.new ArrowModeParams();
        
        return prop;
    }
    
    /** Serialize settings to a byte array accepted by PTNavigage[WithSleep].
     * @param systemNavigation If settings will be used in system navigation.
     *                         Long-touch interval is then shorted with a time context menu 
     *                         button must be pressed to make context menu appear.
     * @param orientation      Display orientation, as returned by 
     *                         serviceInstance.getResources().getConfiguration().orientation
     * @return
     * @throws Exception       Settings not properly initialized.
     */
    public byte[] serialize(boolean systemNavigation, int orientation) throws Exception
    {
        byte[] retVal = new byte[NAV_PROP_SERIALIZED_LENGTH];
        ByteBuffer buffer = ByteBuffer.wrap(retVal);
        int longTouchT = longTouchTime;

        if(systemNavigation)
        {
            //decrease time needed for long touch raising by the time of
            //Android native long touch
            longTouchT -= LONG_TOUCH_ANDROID_INTERVAL;
            if(longTouchTime < 0)
            {
                longTouchT = 0;
            }
        }

        buffer.order(ByteOrder.LITTLE_ENDIAN);

        //clickEnabled
        buffer.put(clickEnabled? (byte)1 : (byte)0);
        buffer.put((byte)0);
        buffer.put((byte)0);
        buffer.put((byte)0);

        //clickFilteringType
        int clickFilteringType = tappingClickProcessingEnabled ? PtConstants.PT_NPP_FM_CLICK_BY_TAPPING : 0x00000000;
        if(pressureClickProcessingEnabled)
        {
            clickFilteringType |= PtConstants.PT_NPP_FM_CLICK_BY_PRESSURE;
        }
        buffer.putInt(clickFilteringType);

        //navigationMode
        if(modeParams instanceof MouseModeParams)
        {
            buffer.putInt(PtConstants.PT_NPP_NM_MOUSE_MODE);
            buffer.putInt(longTouchMode);
            buffer.putInt(longTouchT);
            buffer.putInt(longTouchMaxMovement);
            buffer.putFloat(inputMultiplierX);
            buffer.putFloat(inputMultiplierY);
            buffer.putInt(orientation);
            buffer.putInt(sensorButtonEnabled ? 1 : 0);

            MouseModeParams params = (MouseModeParams)modeParams;
            buffer.putInt(params.cursorAccelThreshold);
            buffer.putFloat(params.cursorAccelMultiplier);
            buffer.putInt(params.cursorMaxSpeed);
            buffer.putInt(params.gridDivisorX);
            buffer.putInt(params.gridDivisorY);
            buffer.putInt(params.straighteningFactor);
            buffer.putFloat(params.inertialMaxSpeed);
            buffer.putFloat(params.inertialFriction);
            buffer.putFloat(params.repeatedSpeedMultiplier);
            buffer.putInt(params.repeatDelay);
            buffer.putFloat(params.noFingerMaxSpeed);
        } 
        else if(modeParams instanceof ArrowModeParams)
        {
            buffer.putInt(PtConstants.PT_NPP_NM_ARROWKEY_MODE);
            buffer.putInt(longTouchMode);
            buffer.putInt(longTouchT);
            buffer.putInt(longTouchMaxMovement);
            buffer.putFloat(inputMultiplierX);
            buffer.putFloat(inputMultiplierY);
            buffer.putInt(orientation);
            buffer.putInt(sensorButtonEnabled ? 1 : 0);

            ArrowModeParams params = (ArrowModeParams)modeParams;
            buffer.putInt(params.keyThreshold);
            buffer.putInt(params.keyRepeatDelay);
            buffer.putInt(params.keyRepeatRate);
            buffer.put((params.accelEnabled) ? (byte)1 : (byte)0);
            buffer.put((byte)0);
            buffer.put((byte)0);
            buffer.put((byte)0);
            buffer.putInt(params.preAccelPeriodLength);
            buffer.putInt(params.accelPeriodLength);
            buffer.putInt(params.fastKeyRepeatRate);
        } 
        else
        {
            throw new Exception("modeParams aren't instance of neither MouseModeParams, nor ArrowModeParams " + modeParams);
        }

        return retVal;
    }
}
