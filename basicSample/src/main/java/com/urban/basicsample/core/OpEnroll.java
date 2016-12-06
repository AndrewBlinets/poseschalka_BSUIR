package com.urban.basicsample.core;

import com.digitalpersona.android.ptapi.PtConnectionI;
import com.digitalpersona.android.ptapi.PtConstants;
import com.digitalpersona.android.ptapi.PtException;
import com.digitalpersona.android.ptapi.callback.PtGuiStateCallback;
import com.digitalpersona.android.ptapi.resultarg.PtBirArg;
import com.digitalpersona.android.ptapi.struct.PtBir;
import com.digitalpersona.android.ptapi.struct.PtFingerListItem;
import com.digitalpersona.android.ptapi.struct.PtGuiSampleImage;
import com.digitalpersona.android.ptapi.struct.PtInputBir;
import com.digitalpersona.android.ptapi.struct.PtSessionCfgV5;

public abstract class OpEnroll extends Thread
{
    private static short SESSION_CFG_VERSION = 5;
    
    private PtConnectionI mConn;
    private int mFingerId;

    public OpEnroll(PtConnectionI conn, int fingerId)
    {
        super("EnrollmentThread" + fingerId);
        mConn = conn;
        mFingerId = fingerId;
    }
    
    /** 
     * Enrollment execution code. 
     */
    @SuppressWarnings("unused")
	@Override
    public void run()
    {       
        try
        {
            // Optional: Set session configuration to enroll 3-5 swipes instead of 5-10
            modifyEnrollmentType();
            // Obtain finger template
            PtInputBir template = enroll();
         
            // Test, if finger already isn't enrolled in device. 
            // If yes and template corresponds to finger ID, remove it. Otherwise report an error.
            /*if(testAndClean())
            {
                // Store enrolled template and finger ID to device
                addFinger(template);
            }*/
            //This code show how to convert templates, it makes 2 conversions (to ISO template and back) 
            //and verifies that the result match with the original template 
            //Note: PtConvertTemplateEx is supported only by TCD50 V3 (TCD50 with area sensor) and TCD51
            if(false)
            {
            	//Convert just enrolled template to ISO template
            	byte [] mIsoRawTemplate = mConn.convertTemplateEx(PtConstants.PT_TEMPLATE_TYPE_AUTO,PtConstants.PT_TEMPLATE_ENVELOPE_NONE,
            	template.bir.data,PtConstants.PT_TEMPLATE_TYPE_ISO_FMR,PtConstants.PT_TEMPLATE_ENVELOPE_NONE,null,0);


				//Convert ISO template back to alpha template, get raw template (without header)
            	byte [] aAlphaRawTemplate = mConn.convertTemplateEx(PtConstants.PT_TEMPLATE_TYPE_ISO_FMR,PtConstants.PT_TEMPLATE_ENVELOPE_NONE,
            			mIsoRawTemplate,PtConstants.PT_TEMPLATE_TYPE_ALPHA,PtConstants.PT_TEMPLATE_ENVELOPE_NONE,null,0);

            	//Create template with header
            	PtBir aAlphaBir = new PtBir();
            	aAlphaBir.factorsMask = template.bir.factorsMask;
            	aAlphaBir.formatID= template.bir.formatID;
            	aAlphaBir.formatOwner= template.bir.formatOwner;
            	aAlphaBir.headerVersion= template.bir.headerVersion;
            	aAlphaBir.purpose= template.bir.purpose;
            	aAlphaBir.quality= template.bir.quality;
            	aAlphaBir.type= template.bir.type;

            	//add raw alpha template data to this header, round up size to 4 and add empty payload (another 4 zero bytes)
            	aAlphaBir.data = new byte[4+((aAlphaRawTemplate.length + 3) & ~3)];
            	//copy raw template
            	for(int i=0;i<aAlphaRawTemplate.length;i++)
            	{
            		aAlphaBir.data[i] = aAlphaRawTemplate[i];
            	}
            	//fill additional zero bytes
            	for(int i=aAlphaRawTemplate.length;i<aAlphaBir.data.length;i++)
            	{
            		aAlphaBir.data[i] = 0;
            	}

            	//Convert PtBir to PtInputBir 
            	PtInputBir aAlphaInputBir = MakeInputBirFromBir(aAlphaBir);
            	
            	//Match the resulting template against the old template from PtEnroll, should match
            	if(mConn.verify(0,0,false,aAlphaInputBir,null,null,null,null,PtConstants.PT_BIO_INFINITE_TIMEOUT,false,null,null,null))
            	{
            		onDisplayMessage("Match");
            	}
            	else
            	{
            		onDisplayMessage("No match!");
            	}
            }
            
        }
        catch (PtException e) 
        {
            // Errors reported in nested methods
            if(e.getCode() == PtException.PT_STATUS_OPERATION_CANCELED)
            {
            }
        }
        
        onFinished();
    }
    
    /**
     * Modify enrollment to 3-5 swipes.
     */
    private void modifyEnrollmentType()  throws PtException
    {
        try 
        {
            PtSessionCfgV5 sessionCfg = (PtSessionCfgV5) mConn.getSessionCfgEx(SESSION_CFG_VERSION);
            sessionCfg.enrollMinTemplates = (byte) 3;
            sessionCfg.enrollMaxTemplates = (byte) 5;
            mConn.setSessionCfgEx(SESSION_CFG_VERSION, sessionCfg);
        } 
        catch (PtException e)
        {
            onDisplayMessage("Unable to set session cfg - " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Simple conversion PtBir to PtInputBir
     */
    private static PtInputBir MakeInputBirFromBir(PtBir aBir)
    {
    	PtInputBir aInputBir = new PtInputBir();
    	aInputBir.form = PtConstants.PT_FULLBIR_INPUT;
    	aInputBir.bir = aBir;
    	return aInputBir;
    }
    
    /**
     * Obtain finger template.
     */
    private PtInputBir enroll() throws PtException
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
    	
    	
    	PtBirArg newTemplate = new PtBirArg();
        
        try 
        {
            // Register notification callback of operation state
            // Valid for entire PTAPI session lifetime
            mConn.setGUICallbacks(null, guiCallback);

            // Enroll finger, don't store template directly to device but return it to host
            // to allow verification, if finger isn't already enrolled
            mConn.enroll(PtConstants.PT_PURPOSE_ENROLL, null, newTemplate,
                         null, null, PtConstants.PT_BIO_INFINITE_TIMEOUT,
                         null, null, null);
        }
        catch (PtException e) 
        {
            onDisplayMessage("Enrollment failed - " + e.getMessage());
            throw e;
        }
        
        // Convert obtained BIR to INPUT BIR class
        return MakeInputBirFromBir(newTemplate.value);
    }
    
    /** Test, if finger already isn't enrolled in device. 
     * If yes and template corresponds to finger ID, remove it. Otherwise report an error.
     * @return True, if finger can be stored.
     */
    private boolean testAndClean()
    {
        try 
        {
            // List fingers stored in device
            PtFingerListItem[] fingerList = mConn.listAllFingers();
            
            if(fingerList != null)
            {
                for(int i=0; i<fingerList.length; i++)
                {
                    PtFingerListItem item = fingerList[i];
                    byte[] fingerData = item.fingerData;

                    if((fingerData != null) && (fingerData.length >= 1))
                    {
                        int fingerId = item.fingerData[0];
                        if(fingerId == mFingerId)
                        {
                            // Delete finger from device
                            mConn.deleteFinger(item.slotNr);
                        } 
                        else
                        {
                            PtInputBir comparedBir = new PtInputBir();
                            comparedBir.form = PtConstants.PT_SLOT_INPUT;
                            comparedBir.slotNr = item.slotNr;
                            
                            // Verify, if template doesn't match the enrolled one (last good template)
                            if(mConn.verifyMatch(null, null, null, null,
                                    comparedBir, null, null, null, null) == true)
                            {
                                onDisplayMessage("Finger already enrolled as " + FingerId.NAMES[fingerId]);
                                return false;
                            }
                        }
                    }
                }
            }
        }
        catch (PtException e)
        {
            onDisplayMessage("testAndClean failed - " + e.getMessage());
            return false;
        }
        
        return true;
    }
    
    /**
     * Store enrolled template and finger ID to device
     * @param template InputBir.
     */
    private void addFinger(PtInputBir template)
    {
        try 
        {
            //store template
            int slot = mConn.storeFinger(template);

            //store fingerId
            byte[] fingerData = new byte[1];
            fingerData[0] = (byte) mFingerId;
            mConn.setFingerData(slot, fingerData);
        } 
        catch (PtException e) 
        {
            onDisplayMessage("addFinger failed - " + e.getMessage());
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
