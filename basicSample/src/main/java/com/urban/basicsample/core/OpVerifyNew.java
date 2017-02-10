package com.urban.basicsample.core;


import com.digitalpersona.android.ptapi.PtConnectionI;
import com.digitalpersona.android.ptapi.PtConstants;
import com.digitalpersona.android.ptapi.PtException;
import com.digitalpersona.android.ptapi.callback.PtGuiStateCallback;
import com.digitalpersona.android.ptapi.callback.PtIdleCallback;
import com.digitalpersona.android.ptapi.resultarg.IntegerArg;
import com.digitalpersona.android.ptapi.resultarg.PtBirArg;
import com.digitalpersona.android.ptapi.struct.PtBir;
import com.digitalpersona.android.ptapi.struct.PtGuiSampleImage;
import com.digitalpersona.android.ptapi.struct.PtInputBir;
import com.digitalpersona.android.ptapi.struct.PtSessionCfgV5;
import com.urban.basicsample.Log_file;

public abstract class OpVerifyNew extends Thread {

	private PtConnectionI mConn;
	// private PtInputBir template;
	private static short SESSION_CFG_VERSION = 5;
	private int count;

	Log_file file = new Log_file();

	public OpVerifyNew(PtConnectionI conn, int c) {
		super("VerifyAllThread");

		//file.writeFile("OpVerifyNew    OpVerifyNew");
		mConn = conn;
		count = c;
	}

	@Override
	public void run() {
	//	file.writeFile("OpVerifyNew    run");
		PtInputBir template = null;
		try {

			modifyEnrollmentType(count);
			template = enroll();

		} catch (PtException e) {
			//file.writeFile( "OpVerifyNew extends Thread  run() " + e.getCode()+"");
			//onDisplayMessage("Verification failed - " + e.getMessage());
			onDisplayMessage(e.getCode()+"");
		}
		catch (NullPointerException e)
		{
			onDisplayMessage("Модуль не подключен -  ПОДКЛЮЧИТЕ МОДУЛЬ!!!\n" +
					"Connect module!!! Please!" );
		}
		if (template != null) {
			onFinished(template);
		} else {
			onStop();
		}
		//
		// super.run();
	}

	private int sleepThenVerify() throws PtException {
		//file.writeFile("OpVerifyNew    sleepThenVerify");
		IntegerArg wakeupCause = new IntegerArg();
		IntegerArg GuiMessage = new IntegerArg();

		PtIdleCallback idleCallback = new PtIdleCallback() {
			public byte idleCallbackInvoke() throws PtException {
				return isInterrupted() ? PtConstants.PT_SLEEP_STOP : PtConstants.PT_SLEEP_CONTINUE;
			}
		};

		try {

			onDisplayMessage("Приложите палец");

			for (;;) {
				mConn.sleepThenCapture(idleCallback, PtConstants.PT_PURPOSE_VERIFY,
						PtConstants.PT_BIO_INFINITE_TIMEOUT, wakeupCause, GuiMessage, null, null, null, null);

				if (wakeupCause.getValue() == PtConstants.PT_WAKEUP_CAUSE_FINGER) {
					if (GuiMessage.getValue() == PtConstants.PT_GUIMSG_GOOD_IMAGE) {
						return mConn.verifyAll(null, null, null, null, null, null, null,
								PtConstants.PT_BIO_INFINITE_TIMEOUT, false, null, null, null);
					} else {
						String s = PtHelper.GetGuiMessage(GuiMessage.getValue());
						if (s != null) {
							onDisplayMessage(s + ", приложите палец снова");
						}
					}
				} else {
					return -1;
				}

			}
		} catch (PtException e) {
			onDisplayMessage("Enrollment failed - " + e.getMessage());
			throw e;
		}
	}

	private int verify() throws PtException {
	//	file.writeFile("OpVerifyNew    verify");
		PtGuiStateCallback guiCallback = new PtGuiStateCallback() {
			public byte guiStateCallbackInvoke(int guiState, int message, byte progress, PtGuiSampleImage sampleBuffer,
					byte[] data) throws PtException {
				String s = PtHelper.GetGuiStateCallbackMessage(guiState, message, progress);

				if (s != null) {
					onDisplayMessage(s);
				}

				return isInterrupted() ? PtConstants.PT_CANCEL : PtConstants.PT_CONTINUE;
			}
		};

		int index = -1;

		try {
			// Register notification callback of operation state
			// Valid for entire PTAPI session lifetime
			mConn.setGUICallbacks(null, guiCallback);

			index = mConn.verifyAll(null, null, null, null, null, null, null,
					PtConstants.PT_BIO_INFINITE_TIMEOUT, true, null, null, null);
		} catch (PtException e) {
			onDisplayMessage("Enrollment failed - " + e.getMessage());
			throw e;
		}

		return index;
	}

	private PtInputBir enroll() throws PtException {
		//file.writeFile("OpVerifyNew    enroll");
		PtGuiStateCallback guiCallback = new PtGuiStateCallback() {
			public byte guiStateCallbackInvoke(int guiState, int message, byte progress, PtGuiSampleImage sampleBuffer,
					byte[] data) throws PtException {
				String s = PtHelper.GetGuiStateCallbackMessage(guiState, message, progress);

				if (s != null) {
					onDisplayMessage(s);
				}

				return isInterrupted() ? PtConstants.PT_CANCEL : PtConstants.PT_CONTINUE;
			}
		};

		PtBirArg newTemplate = new PtBirArg();

		try {
			// Register notification callback of operation state
			// Valid for entire PTAPI session lifetime
			mConn.setGUICallbacks(null, guiCallback);

			// Enroll finger, don't store template directly to device but return it to host
			// to allow verification, if finger isn't already enrolled
			mConn.enroll(PtConstants.PT_PURPOSE_ENROLL, null, newTemplate, null, null,
					PtConstants.PT_BIO_INFINITE_TIMEOUT, null, null, null);
		} catch (PtException e) {
			onDisplayMessage("Enrollment failed - " + e.getMessage());
			throw e;
		}

		// Convert obtained BIR to INPUT BIR class
		return MakeInputBirFromBir(newTemplate.value);
	}

	private static PtInputBir MakeInputBirFromBir(PtBir aBir) {

		if (aBir.data != null) {
			PtInputBir aInputBir = new PtInputBir();
			aInputBir.form = PtConstants.PT_FULLBIR_INPUT;
			aInputBir.bir = aBir;
			return aInputBir;
		} else {
			return null;
		}
	}

	private void modifyEnrollmentType(int c) throws PtException {
	//	file.writeFile("OpVerifyNew    modifyEnrollmentType   " + c);
		try {
			PtSessionCfgV5 sessionCfg = (PtSessionCfgV5) mConn.getSessionCfgEx(SESSION_CFG_VERSION);
			sessionCfg.enrollMinTemplates = (byte) 3;
			sessionCfg.enrollMaxTemplates = (byte) 5;
			mConn.setSessionCfgEx(SESSION_CFG_VERSION, sessionCfg);
		} catch (PtException e) {
			onDisplayMessage("Unable to set session cfg - " + e.getMessage());
			throw e;
		}
		catch (NullPointerException e)
		{
			onDisplayMessage("Модуль не подключен - " + e.getMessage());
			throw e;
		}
	}

	abstract protected void onDisplayMessage(String message);

	abstract protected void onFinished(PtInputBir template);

	abstract protected void onWrite(PtBir ptBir);
	
	abstract protected void onStop();

}
