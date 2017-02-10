package com.urban.basicsample.core;

import com.digitalpersona.android.ptapi.PtConnectionI;
import com.digitalpersona.android.ptapi.PtConstants;
import com.digitalpersona.android.ptapi.PtException;
import com.digitalpersona.android.ptapi.callback.PtGuiStateCallback;
import com.digitalpersona.android.ptapi.callback.PtIdleCallback;
import com.digitalpersona.android.ptapi.resultarg.IntegerArg;
import com.digitalpersona.android.ptapi.struct.PtFingerListItem;
import com.digitalpersona.android.ptapi.struct.PtGuiSampleImage;
import com.urban.basicsample.Log_file;

public abstract class OpVerifyAll extends Thread
{
    
    private PtConnectionI mConn;
    Log_file file = new Log_file();

    public OpVerifyAll(PtConnectionI conn)
    {
        super("VerifyAllThread");
        file.writeFile(" OpVerifyAll      OpVerifyAll");
        mConn = conn;
    }
    
    /** 
     * Operation execution code. 
     */
    @Override
    public void run()
    {
        file.writeFile(" 32 OpVerifyAll run");
    	try 
        {
            // List fingers stored in device
    		onDisplayMessage(mConn.toString());
    		PtFingerListItem[] fingerList = mConn.listAllFingers();
            
            if((fingerList == null) || (fingerList.length == 0))
            {
                onDisplayMessage("No templates enrolled");
            } 
            else
            {
            	int index = sleepThenVerify();
            	
            	onDisplayMessage(index+"");
            	
            	
            	//int index = verify();
                if (-1 != index)
                {
                    // Display finger ID
                    for(int i=0; i<fingerList.length; i++)
                    {
                        PtFingerListItem item = fingerList[i];
                        int slotId = item.slotNr;
                        
                        if(slotId == index)
                        {
                            byte[] fingerData = item.fingerData;
        
                            if((fingerData != null) && (fingerData.length >= 1))
                            {
                                int fingerId = item.fingerData[0];
                                onDisplayMessage(FingerId.NAMES[fingerId] + " finger matched");
                                
                            } 
                            else
                            {
                                onDisplayMessage("No fingerData set");
                            }
                        }
                    }
                }
                else
                {
                    onDisplayMessage("No match found.");
                }
                
            }
        } 
        catch (PtException e)
        {
            file.writeFile(" 85 OpVerifyAll run Verification failed - " + e.getMessage());
            onDisplayMessage("Verification failed - " + e.getMessage());
        }
        
        onFinished();
    }
    
    @SuppressWarnings("unused")
    private int verify() throws PtException
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
    	
    	
    	int index=-1;
        
        try 
        {
            // Register notification callback of operation state
            // Valid for entire PTAPI session lifetime
            mConn.setGUICallbacks(null, guiCallback);

            index = mConn.verifyAll(null, null, null, null, null, null, null, 
                    PtConstants.PT_BIO_INFINITE_TIMEOUT, true,
                    null, null, null);            
        }
        catch (PtException e) 
        {
            onDisplayMessage("Enrollment failed - " + e.getMessage());
            throw e;
        }
        
        return index;
    }


    
    private int sleepThenVerify() throws PtException
    {
    	IntegerArg wakeupCause = new IntegerArg();
    	IntegerArg GuiMessage = new IntegerArg();
    	
    	PtIdleCallback idleCallback = new PtIdleCallback() {
            public byte idleCallbackInvoke() throws PtException {
                return isInterrupted() ? PtConstants.PT_SLEEP_STOP : PtConstants.PT_SLEEP_CONTINUE;
            }
        };
    	
    	
        try 
        {

        	onDisplayMessage("Приложите палец");
        	
        	for(;;)
        	{
        		mConn.sleepThenCapture(idleCallback, PtConstants.PT_PURPOSE_VERIFY, PtConstants.PT_BIO_INFINITE_TIMEOUT, wakeupCause, GuiMessage, null, null, null, null);

	        	if(wakeupCause.getValue() == PtConstants.PT_WAKEUP_CAUSE_FINGER)
	        	{
	        		if(GuiMessage.getValue() == PtConstants.PT_GUIMSG_GOOD_IMAGE)
	        		{
	        			return mConn.verifyAll(null, null, null, null, null, null, null, PtConstants.PT_BIO_INFINITE_TIMEOUT, false, null, null, null);      
	        		}
	        		else
	        		{
	        			String s = PtHelper.GetGuiMessage(GuiMessage.getValue());
	        			if(s != null)
	        			{
	        				onDisplayMessage(s + ", приложите палец снова");
	        			}
	        		}
	        	}
	        	else
	        	{
	        		return -1;
	        	}
        		
        		
        	}
        }
        catch (PtException e) 
        {
            onDisplayMessage("Enrollment failed - " + e.getMessage());
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
