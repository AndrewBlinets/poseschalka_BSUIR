package com.urban.basicsample.core;

import com.digitalpersona.android.ptapi.PtConstants;
import com.urban.basicsample.MyFileClass;

public class PtHelper
{
	/** 
     * Create message for given GUI callback message
     */

    static  MyFileClass file = new MyFileClass();

	public static final String GetGuiStateCallbackMessage(int guiState, int message,  byte progress)
	{
     //   file.writeFile("PtHelper     GetGuiStateCallbackMessage");
		String s = null;
		
		if((guiState & PtConstants.PT_MESSAGE_PROVIDED) != PtConstants.PT_MESSAGE_PROVIDED)
        {
			return s;
        }
        switch(message)
        {
        // Generic GUI messages:            
        case PtConstants.PT_GUIMSG_PUT_FINGER:
        case PtConstants.PT_GUIMSG_PUT_FINGER2:
        case PtConstants.PT_GUIMSG_PUT_FINGER3:
        case PtConstants.PT_GUIMSG_PUT_FINGER4:
        case PtConstants.PT_GUIMSG_PUT_FINGER5:
            s = "Приложите палец";
            break;
        case PtConstants.PT_GUIMSG_KEEP_FINGER:
            s = "Не убирайте палец";
            break;
        case PtConstants.PT_GUIMSG_REMOVE_FINGER:
            s = "Уберите палец";
            break;
        case PtConstants.PT_GUIMSG_BAD_QUALITY:
        case PtConstants.PT_GUIMSG_TOO_STRANGE:
            s = "Плохое качество";
            break;
        case PtConstants.PT_GUIMSG_TOO_LEFT:
            s = "Немного правее";
            break;
        case PtConstants.PT_GUIMSG_TOO_RIGHT:
            s = "Немного левее";
            break;
        case PtConstants.PT_GUIMSG_TOO_LIGHT:
            s = "Слишком ярко";
            break;
        case PtConstants.PT_GUIMSG_TOO_DRY:
            s = "Слишком темно";
            break;
        case PtConstants.PT_GUIMSG_TOO_SMALL:
            s = "Слишком мелко";
            break;
        case PtConstants.PT_GUIMSG_TOO_SHORT:
            s = "Слишком коротко";
            break;
        case PtConstants.PT_GUIMSG_TOO_HIGH:
            s = "Слишком высоко";
            break;
        case PtConstants.PT_GUIMSG_TOO_LOW: 
            s = "Слишком низко";
            break;
        case PtConstants.PT_GUIMSG_TOO_FAST:
            s = "Слишком быстро";
            break;
        case PtConstants.PT_GUIMSG_TOO_SKEWED:  
            s = "Слишком наклонен";
            break;
        case PtConstants.PT_GUIMSG_TOO_DARK:  
            s = "Слишком темно";
            break;
        case PtConstants.PT_GUIMSG_BACKWARD_MOVEMENT:  
            s = "Backward movement";
            break;
        case PtConstants.PT_GUIMSG_JOINT_DETECTED:  
            s = "Joint Detected";
            break;
        case PtConstants.PT_GUIMSG_CENTER_AND_PRESS_HARDER:  
            s = "Press Center and Harder";
            break;
        case PtConstants.PT_GUIMSG_PROCESSING_IMAGE:  
            s = "Обработка изображения";
            break;
        // Enrollment specific:
        case PtConstants.PT_GUIMSG_NO_MATCH:
            s = "Template extracted from last swipe doesn't match previous one";
            break;
        case PtConstants.PT_GUIMSG_ENROLL_PROGRESS:
            s = "Прогресс";
            if((guiState & PtConstants.PT_PROGRESS_PROVIDED) == PtConstants.PT_PROGRESS_PROVIDED)
            {
                s += ": " + progress + '%';
            }

        default:
            break;
        }
        
        return s;
	}

	public static final String GetGuiMessage(int message)
	{
     //   file.writeFile("PtHelper     GetGuiMessage   " + message);
		return GetGuiStateCallbackMessage(PtConstants.PT_MESSAGE_PROVIDED, message, (byte) 0);
	}
	
	
}
