package com.urban.basicsample.core;

import com.digitalpersona.android.ptapi.PtConnectionAdvancedI;
import com.digitalpersona.android.ptapi.PtConstants;
import com.digitalpersona.android.ptapi.PtException;
import com.digitalpersona.android.ptapi.PtGlobal;
import com.digitalpersona.android.ptapi.struct.PtInfo;
import com.digitalpersona.android.ptapi.struct.PtSessionCfgV5;
import com.digitalpersona.android.ptapi.usb.PtUsbHost;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public class InitApi {
	
	private static final String ACTION_USB_PERMISSION = "com.digitalpersona.java.ptapi.dpfpddusbhost.USB_PERMISSION";
	private Context mContext;
	
	private PtGlobal mPtGlobal = null;
    private PtConnectionAdvancedI mConn = null;
    private PtInfo mSensorInfo = null;
    //private Thread mRunningOp = null;
    //private final Object mCond = new Object();
	
    public PtGlobal getPtGlobal(){
    	return mPtGlobal;
    }
    
    public PtConnectionAdvancedI getConn(){
    	return mConn;
    }
    
	public InitApi(Context context){
		mContext = context;
		initialize();
	}

	private void initialize() {
		if(initializePtapi())
        {
        	Context applContext = mContext;
			PendingIntent mPermissionIntent;                 
			mPermissionIntent = PendingIntent.getBroadcast(applContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
			IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			applContext.registerReceiver(mUsbReceiver, filter);

			try
			{
	            // Open PTAPI session
				if(PtUsbHost.PtUsbCheckAndRequestPermissions(applContext, mPermissionIntent))
				{
		            openPtapiSession();
		            //registerButtonListenners();
				}
			}
			catch (PtException e)
			{
				//dislayMessage("Error during device opening - " + e.getMessage());
			}

        }
	}
	
	private boolean initializePtapi()
    {
        // Load PTAPI library
        Context aContext = mContext;
        mPtGlobal = new PtGlobal(aContext);

        try
        {
            // Initialize PTAPI interface
            mPtGlobal.initialize();
            return true;
        }
        catch (java.lang.UnsatisfiedLinkError ule) 
        {
            // Library wasn't loaded properly during PtGlobal object construction
            //dislayMessage("libjniPtapi.so not loaded");
            mPtGlobal = null;
            return false;

        } 
        catch (PtException e)
        {
            //dislayMessage(e.getMessage());
            return false;
        }
    }
	
	private void openPtapiSession()
    {
        try
        {
        	// Try to open session
    		openPtapiSessionInternal();
            
            // Device successfully opened
    		return;
        } 
        catch (PtException e)
        {
                //dislayMessage("Error during device opening - " + e.getMessage());
        }
    }
	
	private void openPtapiSessionInternal() throws PtException
    {
    	// Try to open device
		try
		{
			mConn = (PtConnectionAdvancedI)mPtGlobal.open("USB");
			mSensorInfo = mConn.info();
		}
		catch (PtException e)
		{
				throw e; 
		}

		configureOpenedDevice();        	
    }
	
	private void configureOpenedDevice() throws PtException
    {
    	 PtSessionCfgV5 sessionCfg = (PtSessionCfgV5) mConn.getSessionCfgEx(PtConstants.PT_CURRENT_SESSION_CFG);
         sessionCfg.callbackLevel |= PtConstants.CALLBACKSBIT_NO_REPEATING_MSG;
         mConn.setSessionCfgEx(PtConstants.PT_CURRENT_SESSION_CFG, sessionCfg);
    }
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	    	String action = intent.getAction();
	    	if (ACTION_USB_PERMISSION.equals(action)) {
	    		synchronized (this) {
	    			UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
	    			if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
	    				if(device != null){
	    					openPtapiSession();
	    					//registerButtonListenners();
	    				}                
	    			} 
	    			else
	    			{
	    				System.exit(0);
	    			}
	    		}
	    	}
	    }
	};	
	
}
