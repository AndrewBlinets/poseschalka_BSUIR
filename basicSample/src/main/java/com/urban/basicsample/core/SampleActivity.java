package com.urban.basicsample.core;

/**
 * Sample showing finger enrollment, identification, and removal.
 */


import com.digitalpersona.android.ptapi.PtConnectionAdvancedI;
import com.digitalpersona.android.ptapi.PtConstants;
import com.digitalpersona.android.ptapi.PtException;
import com.digitalpersona.android.ptapi.PtGlobal;
import com.digitalpersona.android.ptapi.struct.PtInfo;
import com.digitalpersona.android.ptapi.struct.PtSessionCfgV5;
import com.digitalpersona.android.ptapi.usb.PtUsbHost;
import com.urban.basicsample.Log_file;
import com.urban.basicsample.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;


public class SampleActivity extends Activity 
{
    
	private static final String ACTION_USB_PERMISSION = "com.digitalpersona.java.ptapi.dpfpddusbhost.USB_PERMISSION";
    
    private PtGlobal mPtGlobal = null;
    private PtConnectionAdvancedI mConn = null;
    private PtInfo mSensorInfo = null;
    private Thread mRunningOp = null;
    private final Object mCond = new Object();

    Log_file file = new Log_file();

    public PtGlobal getPtGlobal(){
    	return mPtGlobal;
    }
    public PtConnectionAdvancedI getmConn(){
    	return mConn;
    }
    /** Initialize activity and obtain PTAPI session. */
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        file.writeFile(" 58 SampleActivity   onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
                
        // Load PTAPI library and initialize its interface
        initialize();
       /* if(initializePtapi())
        {
        	Context applContext = getApplicationContext();
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
		            registerButtonListenners();
				}
			}
			catch (PtException e)
			{
				dislayMessage("Error during device opening - " + e.getMessage());
			}

        }*/
        
    }
	

	
	public void initialize() {
        file.writeFile(" 94 SampleActivity   initialize");
		if(initializePtapi())
        {
        	Context applContext = getApplicationContext();
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
		            registerButtonListenners();
				}
			}
			catch (PtException e)
			{ file.writeFile(" 94 SampleActivity   initialize " + e.getMessage());
				dislayMessage("Error during device opening - " + e.getMessage());
			}

        }
	}
	
    /** Close PTAPI session. */
    @Override
    protected void onDestroy()
    {
        // Cancel running operation
        synchronized(mCond)
        {
            while(mRunningOp != null)
            {
                mRunningOp.interrupt();
                try 
                {
                    mCond.wait();
                } catch (InterruptedException e)
                {
                }
            }
        }
        
        // Close PTAPI session
        closeSession();
        
        // Terminate PTAPI library
        terminatePtapi();
        
        super.onDestroy();
    }
    
    
    private void registerButtonListenners()
    {
        file.writeFile(" 151 SampleActivity   registerButtonListenners");
        setEnrollButtonListener(R.id.ButtonLeftThumb,  FingerId.LEFT_THUMB);
        setEnrollButtonListener(R.id.ButtonLeftIndex,  FingerId.LEFT_INDEX);
        setEnrollButtonListener(R.id.ButtonLeftMiddle, FingerId.LEFT_MIDDLE);
        setEnrollButtonListener(R.id.ButtonLeftRing,   FingerId.LEFT_RING);
        setEnrollButtonListener(R.id.ButtonLeftLittle, FingerId.LEFT_LITTLE);
        
        setEnrollButtonListener(R.id.ButtonRightThumb, FingerId.RIGHT_THUMB);
        setEnrollButtonListener(R.id.ButtonRightIndex, FingerId.RIGHT_INDEX);
        setEnrollButtonListener(R.id.ButtonRightMiddle,FingerId.RIGHT_MIDDLE);
        setEnrollButtonListener(R.id.ButtonRightRing,  FingerId.RIGHT_RING);
        setEnrollButtonListener(R.id.ButtonRightLittle,FingerId.RIGHT_LITTLE);
        
        setIdentifyButtonListener();
        setDeleteAllButtonListener();
        setGrabButtonListener();
        setNavigateRawButtonListener();
        setNavigateMouseButtonListener();
        setNavigateDiscreteButtonListener();
        
        SetQuitButtonListener();
    }
    
    /**
     * Set listener for an enrollment button.
     * @param buttonId Resource ID of a button
     * @param fingerId Finger ID.
     */
    private void setEnrollButtonListener(final int buttonId, final int fingerId)
    {
        file.writeFile(" 181 SampleActivity   setEnrollButtonListener");
    	Button aButton = (Button)findViewById(buttonId);
    	aButton.setVisibility(Button.VISIBLE);
    	OnClickListener aListener = new View.OnClickListener() 
    	{
            public void onClick(View view)
            {
                synchronized(mCond)
                {
                    if(mRunningOp == null)
                    {
                        mRunningOp = new OpEnroll(mConn, fingerId) 
                        {
                            @Override
                            protected void onDisplayMessage(String message) 
                            {
                                dislayMessage(message);
                            }
    
                            @Override
                            protected void onFinished()
                            {
                                synchronized(mCond)
                                {
                                    mRunningOp = null;
                                    mCond.notifyAll();  //notify onDestroy that operation has finished
                                }
                            }
                        };
                        mRunningOp.start();
                    }
                }
            }
        };
        aButton.setOnClickListener(aListener);
    }
    
    /**
     * Set listener for a identification button.
     */
    private void setIdentifyButtonListener()
    {
        file.writeFile(" 223 SampleActivity   setIdentifyButtonListener");
    	Button aButton = (Button)findViewById(R.id.ButtonVerifyAll);
    	aButton.setVisibility(Button.VISIBLE);
        OnClickListener aListener = new View.OnClickListener()
        {
            public void onClick(View view)
            {
                synchronized(mCond)
                {
                    if(mRunningOp == null)
                    {
                        mRunningOp = new OpVerifyAll(mConn)
                        {
                            @Override
                            protected void onDisplayMessage(String message)
                            {
                                dislayMessage(message);
                            }
    
                            @Override
                            protected void onFinished()
                            {
                                synchronized(mCond)
                                {
                                    mRunningOp = null;
                                    mCond.notifyAll();  //notify onDestroy that operation has finished
                                }
                            }
                        };
                        mRunningOp.start();
                    }
                }
            }
        };
        aButton.setOnClickListener(aListener);
    }
    /**
     * Common helper for setNavigatexxxButtonListener()
     */
    private OpNavigate createNavigationOperationHelper(OpNavigateSettings aSettings)
    {

    	OpNavigate aOperation = new OpNavigate(mConn, aSettings) 
        {
            @Override
            protected void onDisplayMessage(String message) 
            {
                dislayMessage(message);
            }

            @Override
            protected void onFinished() 
            {
                synchronized(mCond)
                {
                    mRunningOp = null;
                    mCond.notifyAll();  //notify onDestroy that operation has finished
                }
            }
        };
    	return aOperation;
    }
    
    /**
     * Set listener for a navigate button in raw mode.
     */
    private void setNavigateRawButtonListener()
    {

    	Button aButton = (Button)findViewById(R.id.ButtonNavigateRaw);
    	//disable navigation for area sensors
    	if((mSensorInfo.sensorType & PtConstants.PT_SENSORBIT_STRIP_SENSOR) == 0)
    	{
    		aButton.setVisibility(Button.GONE);
    		return;
    	}
    	aButton.setVisibility(Button.VISIBLE);
    	OnClickListener aListener = new View.OnClickListener() 
    	{
            public void onClick(View view)
            {
                synchronized(mCond)
                {
                    if(mRunningOp == null)
                    {
                        mRunningOp = createNavigationOperationHelper(null);
                        mRunningOp.start();
                    }
                }
            }
        };
        aButton.setOnClickListener(aListener);
    }
    
    /**
     * Set listener for a navigate button in mouse mode.
     */
    private void setNavigateMouseButtonListener()
    {
    	Button aButton = (Button)findViewById(R.id.ButtonNavigateMouse);
    	//disable navigation for area sensors
    	if((mSensorInfo.sensorType & PtConstants.PT_SENSORBIT_STRIP_SENSOR) == 0)
    	{
    		aButton.setVisibility(Button.GONE);
    		return;
    	}
    	aButton.setVisibility(Button.VISIBLE);
    	OnClickListener aListener = new View.OnClickListener()
        {
            public void onClick(View view)
            {
                synchronized(mCond)
                {
                    if(mRunningOp == null)
                    {
                        mRunningOp = createNavigationOperationHelper(OpNavigateSettings.createDefaultMousePostprocessingParams()); 
                        mRunningOp.start();
                    }
                }
            }
        }; 
        aButton.setOnClickListener(aListener);
    }
    
    /**
     * Set listener for a navigate button in discrete mode.
     */
    private void setNavigateDiscreteButtonListener()
    {
    	Button aButton = (Button)findViewById(R.id.ButtonNavigateDiscrete);
    	//disable navigation for area sensors
    	if((mSensorInfo.sensorType & PtConstants.PT_SENSORBIT_STRIP_SENSOR) == 0)
    	{
    		aButton.setVisibility(Button.GONE);
    		return;
    	}
    	aButton.setVisibility(Button.VISIBLE);
    	OnClickListener aListener = new View.OnClickListener()
    	{
            public void onClick(View view)
            {
                synchronized(mCond)
                {
                    if(mRunningOp == null)
                    {
                        mRunningOp = createNavigationOperationHelper(OpNavigateSettings.createDefaultDiscretePostprocessingParams()); 
                        mRunningOp.start();
                    }
                }
            }
        };
        aButton.setOnClickListener(aListener);
    }
    
    /**
     * Set listener for a delete all button.
     */
    private void setDeleteAllButtonListener()
    {
    	Button aButton = (Button)findViewById(R.id.ButtonDeleteAll);
    	aButton.setVisibility(Button.VISIBLE);
    	OnClickListener aListener = new View.OnClickListener()
        {
            public void onClick(View view)
            {
                synchronized(mCond)
                {
                    if(mRunningOp == null)
                    {
                        try
                        {
                            // No interaction with a user needed
                            mConn.deleteAllFingers();
                            dislayMessage("All fingers deleted");
                        } 
                        catch (PtException e)
                        {
                            dislayMessage("Delete All failed - " + e.getMessage());
                        }
                    }
                }
            }
        };
        aButton.setOnClickListener(aListener);
    }
    
    /**
     * Set listener for a grab button.
     */
    private void setGrabButtonListener()
    {
    	Button aButton = (Button)findViewById(R.id.ButtonGrab);
    	aButton.setVisibility(Button.VISIBLE);
        OnClickListener aListener = new View.OnClickListener()
        {
            public void onClick(View view)
            {
                synchronized(mCond)
                {
                    if(mRunningOp == null)
                    {
                        mRunningOp = new OpGrab(mConn,PtConstants.PT_GRAB_TYPE_THREE_QUARTERS_SUBSAMPLE,SampleActivity.this)
                        {
                            @Override
                            protected void onDisplayMessage(String message)
                            {
                                dislayMessage(message);
                            }
    
                            @Override
                            protected void onFinished()
                            {
                                synchronized(mCond)
                                {
                                    mRunningOp = null;
                                    mCond.notifyAll();  //notify onDestroy that operation has finished
                                }
                            }
                        };
                        mRunningOp.start();
                    }
                }
            }
        };
        aButton.setOnClickListener(aListener);
    }
    
    
    private void SetQuitButtonListener()
    {
    	Button aButton = (Button)findViewById(R.id.ButtonQuit);
    	aButton.setVisibility(Button.VISIBLE);
    	OnClickListener aListener = new View.OnClickListener()
        {
            public void onClick(View view)
            {
            	System.exit(0);
            }
        };
        aButton.setOnClickListener(aListener);
    }

     
    /**
     * Load PTAPI library and initialize its interface.
     * @return True, if library is ready for use.
     */
    private boolean initializePtapi()
    {
        file.writeFile(" 472 SampleActivity   initializePtapi");
        // Load PTAPI library
        Context aContext = getApplicationContext();
        mPtGlobal = new PtGlobal(aContext);

        try
        {
            // Initialize PTAPI interface
            mPtGlobal.initialize();
            return true;
        }
        catch (java.lang.UnsatisfiedLinkError ule) 
        {
            file.writeFile(" 485 SampleActivity   initializePtapi libjniPtapi.so not loaded");
            // Library wasn't loaded properly during PtGlobal object construction
            dislayMessage("libjniPtapi.so not loaded");
            mPtGlobal = null;
            return false;

        } 
        catch (PtException e)
        {
            file.writeFile(" 494 SampleActivity   initializePtapi исключение " + e.getMessage());
            dislayMessage(e.getMessage());
            return false;
        }
    }

    /**
     * Terminate PTAPI library.
     */
    private void terminatePtapi()
    {
        try{
            if(mPtGlobal != null)
            {
                mPtGlobal.terminate();
            }
        } catch (PtException e)
        {
            //ignore errors
        }
        mPtGlobal = null;
    }
    
    
    private void configureOpenedDevice() throws PtException
    {
    	 PtSessionCfgV5 sessionCfg = (PtSessionCfgV5) mConn.getSessionCfgEx(PtConstants.PT_CURRENT_SESSION_CFG);
         sessionCfg.callbackLevel |= PtConstants.CALLBACKSBIT_NO_REPEATING_MSG;
         mConn.setSessionCfgEx(PtConstants.PT_CURRENT_SESSION_CFG, sessionCfg);
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
       
    /**
     * Open PTAPI session.
     */
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
            file.writeFile(" 557  SampleActivity   openPtapiSession  Error during device opening - " + e.getMessage());
                dislayMessage("Error during device opening - " + e.getMessage());
        }
    }
    
    private void closeSession()
    {

        if(mConn != null)
        {
            try 
            {
                mConn.close();
            }
            catch (PtException e) 
            {
                // Ignore errors
            }
            mConn = null;
        }
    }
    
    /** 
     * Display message in TextView. 
     */
    public void dislayMessage(String text)
    {
        file.writeFile(" 584 SampleActivity   dislayMessage  " + text);
        mHandler.sendMessage(mHandler.obtainMessage(0, 0, 0, text));
    }
    
    /**
     * Transfer messages to the main activity thread.
     */
    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message aMsg)
        {

            ((TextView)findViewById(R.id.EnrollmentTextView)).setText((String) aMsg.obj);
        }
    };
    
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {

	    	String action = intent.getAction();
	    	if (ACTION_USB_PERMISSION.equals(action)) {
	    		synchronized (this) {
	    			UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
	    			if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
	    				if(device != null){
	    					openPtapiSession();
	    					registerButtonListenners();
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