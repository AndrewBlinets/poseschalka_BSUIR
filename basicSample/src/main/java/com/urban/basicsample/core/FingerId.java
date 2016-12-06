package com.urban.basicsample.core;

/**
 * List of finger IDs.
 */
public abstract class FingerId {
    
    /** No finger matched. */
    public static final int NONE = 0x00000000;

    /** Right thumb finger. */
    public static final int RIGHT_THUMB = 0x00000001;

    /** Right index finger. */
    public static final int RIGHT_INDEX = 0x00000002;

    /** Right middle finger. */
    public static final int RIGHT_MIDDLE = 0x00000003;

    /** Right ring. */
    public static final int RIGHT_RING = 0x00000004;

    /** Right little. */
    public static final int RIGHT_LITTLE = 0x00000005;

    /** Left thumb finger. */
    public static final int LEFT_THUMB = 0x00000006;

    /** Left index finger. */
    public static final int LEFT_INDEX = 0x00000007;

    /** Left middle finger. */
    public static final int LEFT_MIDDLE = 0x00000008;

    /** Left ring finger. */
    public static final int LEFT_RING = 0x00000009;

    /** Left little finger. */
    public static final int LEFT_LITTLE = 0x0000000a;
    
    /** 
     * String representations of finger names. 
     * For other purposes that sample this should be handled as <string-array> resource.
     */
    public static final String NAMES[] = {
        "None", "Right Thumb", "Right Index", "Right Middle", "Right Ring", "Right Little",
                "Left Thumb",  "Left Index",  "Left Middle",  "Left Ring",  "Left Little"
    };
}
