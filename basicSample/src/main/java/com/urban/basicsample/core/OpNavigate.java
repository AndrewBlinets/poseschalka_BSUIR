package com.urban.basicsample.core;

import com.digitalpersona.android.ptapi.PtConnectionI;
import com.digitalpersona.android.ptapi.PtConstants;
import com.digitalpersona.android.ptapi.PtException;
import com.digitalpersona.android.ptapi.callback.PtNavigationCallback;
import com.digitalpersona.android.ptapi.struct.PtNavigationData;

public abstract class OpNavigate extends Thread implements PtNavigationCallback
{       
    private PtConnectionI mConn;
    private OpNavigateSettings mSettings;

    public OpNavigate(PtConnectionI conn, OpNavigateSettings settings)
    {
        super("NavigateThread");
        mConn = conn;
        mSettings = settings;
    }
    
    public byte navigationCallbackInvoke(PtNavigationData navigationData)
    {        
        int deltaX = -navigationData.dx;
        int deltaY = navigationData.dy;
        
        onDisplayMessage("dx:" + deltaX + "dy:" + deltaY + "signalBits:" + Integer.toHexString(navigationData.signalBits));

        try
        {
            sleep(25);
        }
        catch (InterruptedException e) 
        {
        }

        return isInterrupted() ? PtConstants.PT_CANCEL : PtConstants.PT_CONTINUE;
    }
    

    /** 
     * Operation execution code. 
     */
    @Override
    public void run()
    {       
        try
        {
            byte[] settings = (mSettings == null) ? null : mSettings.serialize(false, 0);
            mConn.navigate(-1, this, settings);
        }
        catch (PtException e)
        {
            onDisplayMessage("Navigation failed - " + e.getMessage());
        } 
        catch (Exception e)
        {
            onDisplayMessage(e.getLocalizedMessage());
        }

        onFinished();
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
