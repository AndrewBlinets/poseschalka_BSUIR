package com.urban.basicsample;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalpersona.android.ptapi.PtConnectionAdvancedI;
import com.digitalpersona.android.ptapi.PtException;
import com.digitalpersona.android.ptapi.PtGlobal;
import com.digitalpersona.android.ptapi.struct.PtBir;
import com.digitalpersona.android.ptapi.struct.PtInputBir;
import com.digitalpersona.android.ptapi.usb.PtUsbHost;
import com.urban.basicsample.core.OpVerifyNew;
import com.urban.basicsample.dao.DBHelper;
import com.urban.basicsample.parser.Loader;
import com.urban.basicsample.util.DbUtils;
import com.urban.basicsample.util.PassEncrypter;

public class ScanActivity extends Activity implements android.view.View.OnClickListener {

	private static final int DIALOG = 1;
	private static final int PASS_DIALOG = 100;
	private static final int REPEAT_OR_ADD = 99;
	private static final int dialogShow = 42;
	private static final int tvUpdate = 24;

	private PtConnectionAdvancedI mConn = null;
	private PtGlobal mPtGlobal = null;
	private Thread mRunningOp = null;
	private PtInputBir mTemplate = null;
	private DbUtils dbUtils = null;

	private String timeStart = "";
	private String timeEnd = "";

	private String firstName;
	private String lastName;
	private String mGroup;
	private int week;
	private String day;
	private String subject;
	private int numSubgroup;
	private int part = 1;
	private int lessonId;

	private int subgroup;

	private int count_repeats = 0;

	private static final String Tag = "MyLog";
	Log_file file = new Log_file();
	private AtomicBoolean isTreadStarted = new AtomicBoolean(false);

	WebView webviewActionView;

	private static final String ACTION_USB_PERMISSION = "com.digitalpersona.java.ptapi.dpfpddusbhost.USB_PERMISSION";

	private final Object mCond = new Object();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		file.writeFile( " 103 ScanActivity   onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);

		/*boolean flag = true;
		InputStream stream = null;
		try {
			File sdPath = Environment.getExternalStorageDirectory();
			stream = getAssets().open(sdPath.getAbsolutePath() + "/Attendance/gif_animation.gif");
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}

		if(flag) {
			webviewActionView = (WebView) findViewById(R.id.imageView1);
			webviewActionView.setWebViewClient(new MyWebViewClient());
			webviewActionView.getSettings().setJavaScriptEnabled(true);

			GifWebView view = new GifWebView(this, stream);
			webviewActionView.addView(view);
		}*/

		Intent intent = getIntent();
		week = intent.getIntExtra("week", 0);
		((TextView) findViewById(R.id.stv2)).setText(week + " учебная неделя" );
		dbUtils = new DbUtils(getApplicationContext());
	}

	@Override
	protected void onResume() {
		initialize();
		if (!isTreadStarted.get()) {
			init();
		}
		super.onResume();
	}

	@Override
	protected void onStop() {

		if (mRunningOp != null) {
			mRunningOp.interrupt();
		}
		closeSession();
		terminatePtapi();
		super.onStop();
	}

	public void initialize() {

		if (initializePtapi()) {
			Context applContext = getApplicationContext();
			PendingIntent mPermissionIntent;
			mPermissionIntent = PendingIntent.getBroadcast(applContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
			IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			applContext.registerReceiver(mUsbReceiver, filter);

			try {
				// Open PTAPI session
				if (PtUsbHost.PtUsbCheckAndRequestPermissions(applContext, mPermissionIntent)) {
					openPtapiSession();
				}
			} catch (PtException e) {
				file.writeFile( " 167  ScanActivity   initialize исключение  " + "Error during device opening ScanActivity   initialize - " + e.getMessage() );
				displayMessage("Error during device opening ScanActivity   initialize - " + e.getMessage());//pri otkl
			}

		}
	}

	private boolean initializePtapi() {
		// Load PTAPI library

		Context aContext = getApplicationContext();
		mPtGlobal = new PtGlobal(aContext);

		try {
			// Initialize PTAPI interface
			mPtGlobal.initialize();
			return true;
		} catch (java.lang.UnsatisfiedLinkError ule) {
			// Library wasn't loaded properly during PtGlobal object construction
			displayMessage("libjniPtapi.so not loaded");
			file.writeFile( " 187 ScanActivity   initializePtapi ошибка libjniPtapi.so not loaded");
			mPtGlobal = null;
			return false;

		} catch (PtException e) {
			displayMessage(e.getMessage());
			file.writeFile( " 193 ScanActivity   initializePtapi исключение " + e.getMessage());
			return false;
		}
	}

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
							openPtapiSession();
						}
					} else {
						System.exit(0);
					}
				}
			}
		}
	};

	private void openPtapiSession() {
		try {
			// Try to open session
			openPtapiSessionInternal();

			// Device successfully opened
			return;
		} catch (PtException e) {
			file.writeFile( " 225 ScanActivity   openPtapiSession исключение  " + e.getMessage());
			displayMessage("Error during device opening  -  " + e.getMessage());
		}
	}

	private void openPtapiSessionInternal() throws PtException {
		// Try to open device
		try {
			mConn = (PtConnectionAdvancedI) mPtGlobal.open("USB");
//			mSensorInfo = mConn.info();
		} catch (PtException e) {
			file.writeFile( " 236 ScanActivity   openPtapiSessionInternal исключение " + e.getMessage());
			throw e;
		}
	}

	private void init() {
		synchronized (mCond) {
			isTreadStarted.set(true);
			mRunningOp = new OpVerifyNew(mConn, 3) {
				@Override
				protected void onFinished(PtInputBir template) {
					synchronized (mCond) {
						mRunningOp = null;
						displayMessage("OK");
						verifyStudent(template);
						mCond.notifyAll();
					}
				}

				@Override
				protected void onDisplayMessage(String message) {
					displayMessage(message);

				}

				@Override
				protected void onWrite(PtBir ptBir) {
				}

				@Override
				protected void onStop() {
					synchronized (mCond) {

						mRunningOp = null;
						mCond.notifyAll(); // notify onDestroy that operation has finished
						closeSession();
						/*
						 * try {
						 *
						 * Constants.mConn.close(); Constants.mConn = null; Constants.mPtGlobal = null; } catch
						 * (PtException e) { // TODO Auto-generated catch block e.printStackTrace(); }
						 */
						// displayMessage(template.toString());

					}
				}
			};
			mRunningOp.start();
		}

	}



	private void verifyStudent(PtInputBir template) {
		file.writeFile( " 291 ScanActivity   verifyStudent");
		boolean flag = false;
		SQLiteDatabase database = null;
		try {
			DBHelper helper = new DBHelper(getApplicationContext());
			database = helper.getWritableDatabase();
			// String[] selectionArgs = new String[] { mGroup };
			// Cursor c = database.query("Students", null, "GroupId = ?", selectionArgs, null, null, null);
			Cursor c = database.query("Students", null, null, null, null, null, null);
			displayMessage(c.toString());
			if (c != null) {
				if (c.moveToFirst()) {
					do {
						byte[] t = c.getBlob(c.getColumnIndex("Scan"));
						try {
							ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(t));
							PtInputBir storedTemplate = (PtInputBir) in.readObject();

							if (mConn.verifyMatch(null, null, false, template, storedTemplate, null, null, null, null)) {
								// displayMessage(storedTemplate.toString());
								firstName = c.getString(c.getColumnIndex("FirstName"));
								lastName = c.getString(c.getColumnIndex("LastName"));
								mGroup = c.getString(c.getColumnIndex("GroupId"));
								numSubgroup = c.getInt(c.getColumnIndex("SubGroup"));
								int id = c.getInt(c.getColumnIndex("_id"));
								file.writeFile( "ScanActivity   verifyStudent id" + id);
								if (!dbUtils.checkSchedule(mGroup)) {
									// new Loader(getApplicationContext()).execute(mGroup).get();
									Loader loader = new Loader(ScanActivity.this);
									loader.execute(mGroup);
									loader.get();
									/*
									 * MyTask mt = new MyTask(); mt.execute(mGroup); mt.get();
									 */
									// identifyClass();
								}
								/*
								 * if (groupNumbers == null || groupNumbers.indexOf(mGroup) == -1) { identifyClass();
								 * groupNumbers += mGroup; }
								 */
								// -
								identifyClass();
								// -
								writeToDb(id);
								flag = true;
								displayMessage("DIALOG");

								break;
								// showDialog(DIALOG);
								/*
								 * } else { Intent intent = new Intent(ScanActivity.this, AddStudentActivity.class);
								 * startActivityForResult(intent, 1);
								 */
							}

							// break;
						} catch (Exception e) {
							file.writeFile( " 348 ScanActivity   verifyStudent исключение " + e.getMessage());
							displayMessage("ERRORver" + e.toString());
						}

					} while (c.moveToNext());
					if (!flag) {
						// displayMessage("REPEAT_OR_ADD");
						addStudent(template);
					}
					flag = false;
				} else {
					// displayMessage("REPEAT_OR_ADD");
					addStudent(template);
				}
			}

		} catch (Exception e) {
			file.writeFile( " 365 ScanActivity   verifyStudent исключение " + e.getMessage());
			displayMessage(e.toString());
		} finally {
			if (database != null)
				database.close();
		}

	}

	private void addStudent(PtInputBir template) {
		file.writeFile( " 375 ScanActivity   addStudent");
		if (template != null) {
			Object o = template;
			ByteArrayOutputStream bos = null;
			try {
				bos = new ByteArrayOutputStream();
				ObjectOutput out = new ObjectOutputStream(bos);
				out.writeObject(o);
				out.close();
			} catch (IOException e) {
				file.writeFile( " 385 ScanActivity   addStudent исключение " + e.getMessage());
				displayMessage(e.toString());
			}
			// displayMessage("addStudent");
			Intent intent = new Intent(ScanActivity.this, AddStudentActivity.class);
			intent.putExtra("template", bos.toByteArray());
			startActivityForResult(intent, 1);
		} else {
			displayMessage("Template is null");
		}
	}

	private void writeToDb(int studentId) {
		file.writeFile( " 398 ScanActivity   writeToDb  id_student " + studentId);
		SQLiteDatabase database = null;
		try {
			DBHelper helper = new DBHelper(getApplicationContext());
			database = helper.getWritableDatabase();

			String[] selectionArgs = new String[] { String.valueOf(lessonId), String.valueOf(studentId) };
			Cursor c = database.query("Attendance", null, "LessonId = ? AND StudentId = ?", selectionArgs, null, null,
					null);
			//if(lessonId != 0)
			if (c.getCount() == 0) {
				database.beginTransaction();
				ContentValues cv = new ContentValues();
				cv.put("LessonId", lessonId);
				cv.put("StudentId", studentId);
				if (part == 1) {
					{
						cv.put("Attendance2", 1);
						cv.put("Attendance1", 1);
					}
				} else {
					cv.put("Attendance2", 1);
				}

				database.insert("Attendance", null, cv);

				database.setTransactionSuccessful();
				database.endTransaction();
			} else {
				String id;
				if (c.moveToFirst()) {
					ContentValues cv = new ContentValues();
					id = c.getString(c.getColumnIndex("_id"));
					if (part == 1) {
						cv.put("Attendance1", 1);
						database.update("Attendance", cv, "_id = ?", new String[] { id });
					} else {
						cv.put("Attendance2", 1);
						database.update("Attendance", cv, "_id = ?", new String[] { id });
					}
				}

			}
		} finally {
			if (database != null)
				database.close();
		}

	}

	private void identifyClass() {
		file.writeFile( " 449 ScanActivity   identifyClass");
		Date date = new Date();
		int hour = date.getHours();
		int min = date.getMinutes();
		int intDay = date.getDay();
		switch (intDay) {
			case 0:
				day = "Воскресенье";
				break;
			case 1:
				day = "Понедельник";
				break;
			case 2:
				day = "Вторник";
				break;
			case 3:
				day = "Среда";
				break;
			case 4:
				day = "Четверг";
				break;
			case 5:
				day = "Пятница";
				break;
			case 6:
				day = "Суббота";
				break;
		}
		file.writeFile( "ScanActivity   identifyClass 123");
		SQLiteDatabase database = null;
		String namesub = " ";
		try {
			DBHelper helper = new DBHelper(getApplicationContext());
			database = helper.getWritableDatabase();
			String[] selectionArgs = new String[] { mGroup, day };
			Cursor c = database
					.query("Schedule", null, "StudentGroup = ? AND Day = ?", selectionArgs, null, null, null);

			if (c != null) {
				if (c.moveToFirst()) {
					do {
						String weekNumber = c.getString(c.getColumnIndex("WeekNumber"));
						subgroup = c.getInt(c.getColumnIndex("NumSubgroup"));
						file.writeFile( "492 ScanActivity   identifyClass неделя " + weekNumber + " подгруппа " + subgroup);
						file.writeFile( "493 ScanActivity   identifyClass неделя реальная неделя " + week + " подгруппа " + numSubgroup);

						if ((weekNumber.charAt(0) == '0' || weekNumber.indexOf(String.valueOf(week)) != -1)
								&& (subgroup == 0 || subgroup == numSubgroup)) {
							timeStart = c.getString(c.getColumnIndex("LessonTimeStart"));
							timeEnd = c.getString(c.getColumnIndex("LessonTimeEnd"));
							StringTokenizer stkS = new StringTokenizer(timeStart, ":");
							StringTokenizer stkE = new StringTokenizer(timeEnd, ":");
							int[] arS = new int[stkS.countTokens()];
							int[] arE = new int[stkS.countTokens()];
							for (int i = 0; i < arS.length; i++) {
								arS[i] = Integer.parseInt(stkS.nextToken());
								arE[i] = Integer.parseInt(stkE.nextToken());
							}
							if ((arS[0] * 60 + arS[1]) <= (hour * 60 + min)
									&& (arE[0] * 60 + arE[1]) >= (hour * 60 + min)) {
								// numSubgroup = c.getInt(c.getColumnIndex("NumSubgroup"));
								if (((hour * 60 + min) - (arS[0] * 60 + arS[1])) <= 45) {
									part = 1;
								} else {
									part = 2;
								}
								subject = c.getString(c.getColumnIndex("Subject"));
								file.writeFile( " 516 ScanActivity   identifyClass  предмет " + subject);
								// Toast.makeText(getApplicationContext(), subject, 1000).show();
								/*
								 * ((TextView) findViewById(R.id.stv1)).setText(mGroup); ((TextView)
								 * findViewById(R.id.stv2)).setText(subject); ((TextView)
								 * findViewById(R.id.stv3)).setText("?????: " + part);
								 */
								displayMessage("UPDATE_TV");

								writeLessonToDb();

								break;
							}
						}

					} while (c.moveToNext());
				}

			} else {
				Log.d("DEBUG", "Cursor is null");
			}

		} finally {
			if (database != null)
				database.close();
		}
	}

	private void writeLessonToDb() {
		file.writeFile(" 545 ScanActivity   writeLessonToDb");
		if(subject != null) {

			Date d = new Date();
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
			String date = format.format(d);

			SQLiteDatabase database = null;

			try {
				DBHelper helper = new DBHelper(getApplicationContext());
				database = helper.getWritableDatabase();

				String[] selectionArgs = new String[] { date, subject,
														timeStart, timeEnd, String.valueOf(subgroup)};
				Cursor c = null;
				try {


				c = database.query("Lessons", null, "Date = ? AND Subject = ? AND TimeStart = ? AND TimeEnd = ? AND SubGroup =?",
						selectionArgs, null, null, null);
				}
				catch (Exception e)
				{
					file.writeFile(" 569 ScanActivity   writeLessonToDb exception " + e.getMessage());
				}
				if (c.getCount() != 0) {
					if (c.moveToFirst()) {
						lessonId = c.getInt(c.getColumnIndex("_id"));
						file.writeFile(" 574 ScanActivity   writeLessonToDb id lesson = " + lessonId);
					}
				} else {
					file.writeFile(" 577 ScanActivity   writeLessonToDb создаем новый lesson ");
					database.beginTransaction();

					ContentValues cv = new ContentValues();
					cv.put("Date", date);
					cv.put("GroupId", mGroup);
					cv.put("Subject", subject);
					cv.put("SubGroup", subgroup);
					cv.put("TimeStart", timeStart);
					cv.put("TimeEnd", timeEnd);

					lessonId = (int) database.insert("Lessons", null, cv);
					file.writeFile(" 589  ScanActivity   writeLessonToDb  id нового lesson = " + lessonId);
					database.setTransactionSuccessful();
					database.endTransaction();
				}


			} finally {
				database.close();
			}
		}
	}

	protected Dialog onCreateDialog(int id) {
		file.writeFile( " 602 ScanActivity   onCreateDialog id = " + id);
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		switch (id) {
			case REPEAT_OR_ADD:
				adb.setTitle("Студент не найден");
				adb.setMessage("Повторить?");
				adb.setIcon(android.R.drawable.ic_dialog_info);
				adb.setPositiveButton("Повторить", myClickListenerNew);
				adb.setNegativeButton("Добавить", myClickListenerNew);
				break;
			case PASS_DIALOG:
				final EditText editText = new EditText(this);
				editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String nPass = editText.getText().toString();
						// Toast.makeText(LoginActivity.this, "Input was: "+nPass, 1000).show();
						if (PassEncrypter.verifyAdminPass(getApplicationContext(), nPass)) {
							/*Intent intent = new Intent();
							intent.putExtra("id_flag", 0);
							setResult(RESULT_OK, intent);
							finish();*/
							finish();
						} else {
							editText.setText("");
							Toast.makeText(getApplicationContext(), "Неверный пароль", Toast.LENGTH_SHORT).show();
						}
					}
				};

				adb.setTitle("Авторизация").setMessage("Введите пароль:")
						.setPositiveButton("Ок", onClickListener).setView(editText).create();
				break;
			default:
				// ?????????
				if(lastName.equals(""))
				{
					adb.setMessage("Данные не были введены, повторите сканирование, пожалуйста!");
					// ??????
					adb.setIcon(android.R.drawable.ic_dialog_info);
					// ?????? ?????????????? ??????
					adb.setPositiveButton("Ok", myClickListener);
				}
				else {
					if (subject != null)
						adb.setTitle(subject + ", гр: " + mGroup);
					else
						adb.setTitle("В данный момент занятий нету, гр: " + mGroup);
					// ?????????
					adb.setMessage(firstName + " " + lastName);
					// ??????
					adb.setIcon(android.R.drawable.ic_dialog_info);
					// ?????? ?????????????? ??????
					adb.setPositiveButton("Ok", myClickListener);
					// ?????? ?????????????? ??????
					// adb.setNegativeButton("?????????", myClickListener);
				}
				break;
		}
		adb.setCancelable(false);
		// ??????? ??????
		return adb.create();

		// return super.onCreateDialog(id);
	}

	OnClickListener myClickListener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				// ????????????? ??????
				case Dialog.BUTTON_POSITIVE:
					init();
					dialog.dismiss();
					break;
				// ?????????? ??????
				case Dialog.BUTTON_NEGATIVE:
					closeSession();
					terminatePtapi();
					finish();
					break;

			}
		}
	};

	OnClickListener myClickListenerNew = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				// ????????????? ??????
				case Dialog.BUTTON_POSITIVE:
					file.writeFile( "Dialog.BUTTON_POSITIVE");
					init();
					dialog.dismiss();
					break;
				// ?????????? ??????
				case Dialog.BUTTON_NEGATIVE:
					file.writeFile( "Dialog.BUTTON_NEGATIVE:");
					addStudent(mTemplate);
					break;

			}
		}
	};

	@Override
	public void onBackPressed() {
		file.writeFile( "ScanActivity   onBackPressed");
		Toast.makeText(getApplicationContext(), "Таким образом выйти невозможно", Toast.LENGTH_SHORT).show();
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		file.writeFile( "ScanActivity   onCreateOptionsMenu");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scan, menu);
		return true;
	}

	public void displayMessage(String text) {
		switch (text) {
			case "REPEAT_OR_ADD":
				mHandler.sendMessage(mHandler.obtainMessage(0, REPEAT_OR_ADD, 0, text));
				break;
			case "DIALOG":
				mHandler.sendMessage(mHandler.obtainMessage(0, dialogShow, 0, text));
				break;
			case "UPDATE_TV":
				mHandler.sendMessage(mHandler.obtainMessage(0, tvUpdate, 0, text));
				break;
			default:
				mHandler.sendMessage(mHandler.obtainMessage(0, 0, 0, text));
				break;
		}
		/*
		 * if (text.equalsIgnoreCase("DIALOG")) { mHandler.sendMessage(mHandler.obtainMessage(0, dialogShow, 0, text));
		 * } else { mHandler.sendMessage(mHandler.obtainMessage(0, 0, 0, text)); }
		 */
	}

	private Handler mHandler = new Handler() {

		int i = 101;

		public void handleMessage(Message aMsg) {
			//file.writeFile( "ScanActivity  mHandler  handleMessage");
			switch (aMsg.arg1) {
				case dialogShow:
					showDialog(i++);
					break;
				case REPEAT_OR_ADD:
					showDialog(REPEAT_OR_ADD);
					break;
				case tvUpdate:
					((TextView) findViewById(R.id.stv1)).setText(mGroup);
					((TextView) findViewById(R.id.stv2)).setText(subject + " " + week + " учебная неделя");
					((TextView) findViewById(R.id.stv3)).setText("Часть: " + part);
					break;
				default:
					String str_message = (String) aMsg.obj;
					//String str1 = "\\(";
					//file.writeFile( "ScanActivity  mHandler  handleMessage str1 " + str1);
					//String str2 = "\\)";
				//	file.writeFile( "ScanActivity  mHandler  handleMessage str2 " + str2);
				//	String[] str = str_message.split(str1);
				//	if(str.length > 1) {
						if (str_message.equals("-1052"))
							finish();
						/*	((TextView) findViewById(R.id.EnrollmentTextView1)).setText
									("Для подключение модуля ОБНОВИТЬ");
						}
					else*/
					((TextView) findViewById(R.id.EnrollmentTextView1)).setText(str_message);
					//file.writeFile( "ScanActivity  mHandler  handleMessage default " + (String) aMsg.obj);
					break;
			}
			/*
			 * if (aMsg.arg1 == dialogShow) { showDialog(i++); } else { ((TextView)
			 * findViewById(R.id.EnrollmentTextView1)).setText((String) aMsg.obj); }
			 */
		}
	};

	@Override
	protected void onDestroy() {
		// Cancel running operation
		synchronized (mCond) {
			while (mRunningOp != null) {
				mRunningOp.interrupt();
				isTreadStarted.set(false);
				try {
					mCond.wait();
				} catch (InterruptedException e) {
				}
			}
		}

		// Close PTAPI session
		// closeSession();

		// displayMessage("onDestroy");
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1 && resultCode == RESULT_OK) {
			int id = data.getIntExtra("id", -1);
			mGroup = data.getStringExtra("group");
			firstName = data.getStringExtra("firstName");
			lastName = data.getStringExtra("lastName");
			numSubgroup = data.getIntExtra("subgroup",0);
			if (id != -1 && !firstName.equals("")) {
				if (!dbUtils.checkSchedule(mGroup)) {
					if(!isOnline()){
						Toast.makeText(getApplicationContext(), "Нет доступа к интернету!!!",
								Toast.LENGTH_SHORT).show();
						return;
					}
					Loader loader = new Loader(ScanActivity.this);
					loader.execute(mGroup);
					/*
					 * MyTask mt = new MyTask(); mt.execute(mGroup);
					 */
					try {
						loader.get();
					} catch (InterruptedException e) {
						file.writeFile( "830 ScanActivity   onActivityResult " + "ERROR" + e.toString());
						displayMessage("ERROR" + e.toString());
					} catch (ExecutionException e) {
						file.writeFile( "833 ScanActivity   onActivityResult " + "ERROR" + e.toString());
						displayMessage("ERROR" + e.toString());
					}
					// identifyClass();
				}
				/*
				 * if (groupNumbers == null || groupNumbers.indexOf(mGroup) == -1) { identifyClass(); groupNumbers +=
				 * mGroup; }
				 */
				identifyClass();
				if(subject != null)
				writeToDb(id);
				displayMessage("DIALOG");
				// init();
			}
			else {
				displayMessage("DIALOG");
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void closeSession() {
		if (mConn != null) {
			try {
				mConn.close();
			} catch (PtException e) {
				// Ignore errors
			}
			mConn = null;
		}
	}

	private void terminatePtapi() {
		try {
			if (mPtGlobal != null) {
				mPtGlobal.terminate();
			}
		} catch (PtException e) {
			// ignore errors

		}
		mPtGlobal = null;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.s_exit:
				showDialog(PASS_DIALOG);
				break;
			/*case R.id.refresh:
				Intent i = new Intent( this , this.getClass() );
				finish();
				this.startActivity(i);
				break;*/
			default:
				break;
		}
	}

	public  boolean isOnline()
	{
		String cs = Context.CONNECTIVITY_SERVICE;
		ConnectivityManager cm = (ConnectivityManager)
				getSystemService(cs);
		if (cm.getActiveNetworkInfo() == null)
			return false;
		else
			return  true;
	}

	private class MyWebViewClient extends WebViewClient {
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}
}