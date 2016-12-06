package com.urban.basicsample.core;

import com.digitalpersona.android.ptapi.PtConnectionI;
import com.digitalpersona.android.ptapi.PtConstants;
import com.digitalpersona.android.ptapi.PtException;
import com.digitalpersona.android.ptapi.callback.PtGuiStateCallback;
import com.digitalpersona.android.ptapi.callback.PtIdleCallback;
import com.digitalpersona.android.ptapi.resultarg.ByteArrayArg;
import com.digitalpersona.android.ptapi.resultarg.IntegerArg;
import com.digitalpersona.android.ptapi.struct.*;
import com.urban.basicsample.core.FPDisplay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;

public abstract class OpGrab extends Thread 
{
    private PtConnectionI mConn;
    private Activity mActivity;
    private byte mbyGrabType;

    public OpGrab(PtConnectionI conn,byte byGrabType,Activity aActivity)
    {
        super("GrabThread");
        mConn = conn;
        mActivity = aActivity;
        mbyGrabType = byGrabType;
    }
    
    
    public static Bitmap CreateBitmap(byte [] aImageData,int iWidth)
    {
    	int[] data = new int[aImageData.length];
    	int iLength = aImageData.length;
    	int iHeight = iLength / iWidth;
		for(int i=0; i<iLength; i++)
        {
        	int color = (int)aImageData[i];
        	if(color < 0)
        	{
        		color = 256 + color;
        	}
        	data[i] = Color.rgb(color,color,color);
        }
        return Bitmap.createBitmap(data, iWidth, iHeight, Bitmap.Config.ARGB_8888);	
    }
    
    
    private void ShowImage(byte [] aImage,int iWidth)
    {
    	FPDisplay.mImage = CreateBitmap(aImage,iWidth);
    	FPDisplay.msTitle = "Fingerprint Image"; 
        Intent aIntent = new Intent(mActivity, FPDisplay.class);
        mActivity.startActivityForResult(aIntent,0);
    }

    
    /** 
     * Grab execution code. 
     */
    @Override
    public void run()
    {       
        try
        {
            // Optional: Set session configuration 
        	ModifyConfig();
    
            // Obtain finger template
        	byte [] image = SleepThenGrabImage(mbyGrabType);
//        	byte [] image = GrabImage(mbyGrabType);
            
        	if(image != null)
        	{
	        	int iWidth = (mConn.info().imageWidth);
	            onDisplayMessage("Image grabbed");
	            switch(mbyGrabType)
	            {
	            case PtConstants.PT_GRAB_TYPE_THREE_QUARTERS_SUBSAMPLE:
	            	iWidth = (iWidth * 3)>>2;
	        	case PtConstants.PT_GRAB_TYPE_508_508_8_SCAN508_508_8:
	                ShowImage(image,iWidth);
	        		break;
	        		default:
	        			// unsupported image type for displaying
	            }
        	}            
        }
        catch (PtException e) 
        {
           
        }
        
       
        onFinished();
    }
    
    /**
     * Modify session configuration if needed
     */
    private void ModifyConfig()  throws PtException
    {
        try 
          {
        	final short SESSION_CFG_VERSION = 5;
        	PtSessionCfgV5 sessionCfg = (PtSessionCfgV5) mConn.getSessionCfgEx(SESSION_CFG_VERSION);
        
            // modify session parameters as needed
            
            mConn.setSessionCfgEx(SESSION_CFG_VERSION, sessionCfg);
        }
         catch (PtException e) 
        {
            onDisplayMessage("Unable to set session cfg - " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Obtain finger template.
     */
	@SuppressWarnings("unused")
    private byte[] GrabImage(byte byType) throws PtException
    {
    	
    	PtGuiStateCallback guiCallback = new PtGuiStateCallback() {
			public byte guiStateCallbackInvoke(int guiState, int message,  byte progress,
	                PtGuiSampleImage sampleBuffer, byte[] data) throws PtException 
	                {
			        	String s = PtHelper.GetGuiStateCallbackMessage(guiState,message,progress);
		                
			            if(s != null)
			            {
			                onDisplayMessage(s);
			            }
		
			            return isInterrupted() ? PtConstants.PT_CANCEL : PtConstants.PT_CONTINUE;
	                }
    	};
    	
        try
        {
            // Register notification callback of operation state
            // Valid for entire PTAPI session lifetime
            mConn.setGUICallbacks(null, guiCallback);

            return mConn.grab(byType,PtConstants.PT_BIO_INFINITE_TIMEOUT,true,null,null);
        }
        catch (PtException e) 
        {
            onDisplayMessage("Grab failed - " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Obtain finger template after wakeup.
     */
    private byte[] SleepThenGrabImage(byte byType) throws PtException
    {
    	IntegerArg wakeupCause = new IntegerArg();
    	IntegerArg GuiMessage = new IntegerArg();
    	ByteArrayArg grabbedData =  new ByteArrayArg();

    	PtIdleCallback idleCallback = new PtIdleCallback() {
            public byte idleCallbackInvoke() throws PtException {
                return isInterrupted() ? PtConstants.PT_SLEEP_STOP : PtConstants.PT_SLEEP_CONTINUE;
            }
        };

        try
        {
        	onDisplayMessage("Put Finger");
        	
        	for(;;)
        	{

            	mConn.sleepThenGrab(idleCallback, byType, PtConstants.PT_BIO_INFINITE_TIMEOUT, true, wakeupCause, GuiMessage, grabbedData, null, null);
	
	        	if(wakeupCause.getValue() == PtConstants.PT_WAKEUP_CAUSE_FINGER)
	        	{
	        		if(GuiMessage.getValue() == PtConstants.PT_GUIMSG_GOOD_IMAGE)
	        		{
	        			return grabbedData.getValue();	        			
	        		}
	        		else
	        		{
	        			String s = PtHelper.GetGuiMessage(GuiMessage.getValue());
	        			if(s != null)
	        			{
	        				onDisplayMessage(s + ", put finger again");
	        			}
	        		}
	        	}
	        	else
	        	{
	        		return null;
	        	}
        	}        	
        }
        catch (PtException e) 
        {
            onDisplayMessage("sleepThenGrab failed - " + e.getMessage());
            throw e;
        }
    }
    
    /** 
     * Display message. To be overridden by sample activity.
     * @param message Message text.
     */
    abstract protected void onDisplayMessage(String message);
    
    /** 
     * Called, if operation is finished.
     * @param message Message text.
     */
    abstract protected void onFinished();

}
