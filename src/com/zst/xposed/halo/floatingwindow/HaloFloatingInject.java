package com.zst.xposed.halo.floatingwindow;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findConstructorBestMatch;
import static de.robv.android.xposed.XposedHelpers.findConstructorExact;
import static de.robv.android.xposed.XposedHelpers.findField;
import static de.robv.android.xposed.XposedHelpers.findMethodBestMatch;
import static de.robv.android.xposed.XposedHelpers.findMethodExact;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.IApplicationToken;
import android.view.IWindowManager;
import android.view.Window;
import android.view.WindowManager;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.callbacks.XCallback;


public class HaloFloatingInject implements  IXposedHookZygoteInit , IXposedHookLoadPackage{
	private static final String ID_TAG = "&ID=";
	public static final int FLAG_FLOATING_WINDOW = 0x00002000;
	
	private static String class_boolean = Res.NULL;
	public static XSharedPreferences pref;

		
//	FLAG_ACTIVITY_CLEAR_TASK	If set in an Intent passed to Context.startActivity(), this flag will cause any existing task that would be associated with the activity to be cleared before the activity is started.
	//int	FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET	If set, this marks a point in the task's activity stack that should be cleared when the task is reset.
	//int	FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
	    pref = new XSharedPreferences(Res.MY_PACKAGE_NAME,Res.MY_PACKAGE_NAME);
		inject_WindowManagerService_setAppStartingWindow();
		inject_Activity();
		inject_DecorView_generateLayout();	
		//TODO : add dynamic enabler
	}
	@Override
	public void handleLoadPackage(LoadPackageParam l) throws Throwable {
		inject_ActivityRecord_ActivityRecord(l);
	}
	static Field fullscreen_3 = null;

	public static void inject_ActivityRecord_ActivityRecord(final LoadPackageParam lpparam) {
		try {
			if (!lpparam.packageName.equals("android")) return;
			
			final Constructor<?> contructor3 = XposedHelpers.findConstructorBestMatch(
					findClass("com.android.server.am.ActivityRecord", lpparam.classLoader),
					findClass("com.android.server.am.ActivityManagerService", lpparam.classLoader), 
					findClass("com.android.server.am.ActivityStack", lpparam.classLoader),
					findClass("com.android.server.am.ProcessRecord", lpparam.classLoader),
					int.class, Intent.class, String.class, ActivityInfo.class, Configuration.class,
		            findClass("com.android.server.am.ActivityRecord", lpparam.classLoader), 
		            String.class, int.class, boolean.class );
			
			XposedBridge.hookMethod(contructor3, new XC_MethodHook(XCallback.PRIORITY_HIGHEST) {
				 @Override  protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					 Intent i  = (Intent) param.args[4];
						ActivityInfo aInfo  = (ActivityInfo) param.args[6];
						 String pkg = aInfo.applicationInfo.packageName;
						  // XposedBridge.log("halo:--init----0");  
						 if (pkg.equals("android") || pkg.equals("com.android.systemui")) return;
						 if(aInfo.applicationInfo.uid != Res.previousUid){
							 
						 int _launchedFromUid  = (Integer) param.args[3];
						 if (_launchedFromUid == 0) return;
						 
						 if(!(_launchedFromUid == Res.previousUid && Res.previousUid != -756456451 )){
							 
						 if((i.getFlags()& FLAG_FLOATING_WINDOW)==0){
							 Res.notFloating = true;
							 return;
						 }
						 }
						 
						 }
						 Res.notFloating = false;
						 Res.previousUid = aInfo.applicationInfo.uid;

					   Class<?> d = findClass("com.android.server.am.ActivityRecord", lpparam.classLoader);
					   XposedBridge.log("halo:--"+d.toString() + "----" + pkg);  

					 fullscreen_3 = null;
					   for ( Field fullscreen_1 : d.getDeclaredFields() ) {
				           // XposedBridge.log("halo-activityrecord:field " + ff);
				            if (fullscreen_1.getName().contains("fullscreen")){
				            fullscreen_3 = fullscreen_1;
				            fullscreen_3.setAccessible(true);
				            break;
				            }
					   }
					/*   ActivityInfo aInfo  = (ActivityInfo) param.args[6];
						 String pkg = aInfo.applicationInfo.packageName;
						 if(pkg.equals("com.android.systemui")){isHaloFloat=false;   return; }
						 if(pkg.equals("android"))  {isHaloFloat=false;   return; }

					 Intent intent_p = (Intent)param.args[4];
					isHaloFloat  = (intent_p.getFlags() & FLAG_FLOATING_WINDOW) == FLAG_FLOATING_WINDOW;
*/
					 /*if (floatingWindow) {
			                intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_TASK_ON_HOME);---
			                intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_SINGLE_TOP);
			                intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_CLEAR_TOP);
			                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);-----
		                intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);---
			                
			                // If this is the mother-intent we make it volatile
			                if (topIntent) {
			                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			                }*/
			}
			   @Override
			   protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					/*Intent i  = (Intent) param.args[4];
					ActivityInfo aInfo  = (ActivityInfo) param.args[6];
					 String pkg = aInfo.applicationInfo.packageName;
					  // XposedBridge.log("halo:--init----0");  

					 if((i.getFlags()& FLAG_FLOATING_WINDOW)==0) return;
					//   XposedBridge.log("halo:--floating check----0");  

					// if(!pkg.equals(i.getPackage()))   return; 
					  // XposedBridge.log("halo:--equals---0");  

				   Class<?> d = findClass("com.android.server.am.ActivityRecord", lpparam.classLoader);
				   XposedBridge.log("halo:--"+d.toString() + "----" + pkg);  

				 //   Field fullscreen_1 = null;
				   for ( Field fullscreen_1 : d.getDeclaredFields() ) {
			           // XposedBridge.log("halo-activityrecord:field " + ff);
			            if (fullscreen_1.getName().contains("fullscreen")){
			            //fullscreen_1 = field;
			            fullscreen_1.setAccessible(true);
						fullscreen_1.set(param.thisObject, Boolean.FALSE); 
			            break;
			            }*/
				   if (fullscreen_3 != null)
		            fullscreen_3.set(param.thisObject, Boolean.FALSE); 
				   
					 fullscreen_3 = null;


				  // }
				    
				   
					 
				   
				  // Intent yyy = (Intent)intent_1..get(d.newInstance());
				   
				  // 				  //f (!floatingWindow) return;
				   //yyy.setFlags(yyy.getFlags() & ~Intent.FLAG_ACTIVITY_TASK_ON_HOME);
				   //yyy.setFlags(yyy.getFlags() & ~Intent.FLAG_ACTIVITY_SINGLE_TOP);
	                //yyy.setFlags(yyy.getFlags() & ~Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                //yyy.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
	              ///  yyy.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);

				   //yyy.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				   //intent_1.set(param.thisObject, yyy);
				    
				   
				   
				   
				   
				   

			   }
			 });
			
			
		
		} catch (Exception e) {
			XposedBridge.log("halo-activityrecord:error "+ e.toString() );

		}
	}
		
	public static void inject_WindowManagerService_setAppStartingWindow() {
		try {
			findAndHookMethod("com.android.server.wm.WindowManagerService", null, "setAppStartingWindow",
					IBinder.class, String.class, int.class, CompatibilityInfo.class, CharSequence.class, 
				    int.class, int.class, int.class, IBinder.class, boolean.class, 
				    new XC_MethodHook() { 
				
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					if (Res.notFloating) return;
					// Gets String pkg to get package name
					String pkg = (String)param.args[1];
					//if (!class_boolean.startsWith(pkg + ID_TAG)) return;
					//TODO: if(!pkg.contains("com.whatsapp"))   return;
					
					// Change boolean "createIfNeeded" to FALSE
					param.args[9] = Boolean.FALSE;
					// Removes Blank window placeholder before activity's layout xml fully loads
					
					XposedBridge.log("XHaloFloatingWindow-DEBUG(setAppStartingWindow):" + pkg );
					

					}
				
				});
		} catch (Throwable e) {
		}
}
	
	
	public static void inject_Activity( ) { 
		try{	
			findAndHookMethod( Activity.class,  "onCreate", Bundle.class, new XC_MethodHook() { 
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					
					 Activity thiz = (Activity)param.thisObject;
					 String name = thiz.getWindow().getContext().getClass().getPackage().getName();
					 if (name.startsWith("com.android.systemui"))  return;
					 //boolean isHoloFloat = (!notFloating) ;
					 boolean isHoloFloat = (thiz.getIntent().getFlags() & FLAG_FLOATING_WINDOW) == FLAG_FLOATING_WINDOW;
					 if (Res.notFloating == false){
						 isHoloFloat = true;
					 }
					 if (class_boolean.equals( name + ID_TAG + thiz.getTaskId()))
					 {   		isHoloFloat = true;
					 }else {	class_boolean = Res.NULL;
					 }
					
					 if(isHoloFloat){
						 class_boolean = name + ID_TAG + thiz.getTaskId();
						 XposedBridge.log("XHaloFloatingWindow-DEBUG(onCreate):" + class_boolean);
						 return;
					 }
					 class_boolean = Res.NULL;
					 
				}
				
			});
		
			/*findAndHookMethod( Activity.class,  "finish", new XC_MethodHook() { 
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					 Activity thiz = (Activity)param.thisObject;
					 String name = thiz.getWindow().getContext().getClass().getPackage().getName();
					 if (name.startsWith("com.android.systemui"))  return;

					 if (class_boolean.equals( name + ID_TAG + thiz.getTaskId())) { 
						 thiz.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
					 }
				}
				
			});*/
		} catch (Throwable e) {
		}
		
}
	
	
	public static void inject_DecorView_generateLayout() {
		try {
			findAndHookMethod("com.android.internal.policy.impl.PhoneWindow", null, "generateLayout",
					"com.android.internal.policy.impl.PhoneWindow.DecorView", new XC_MethodHook() { 
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Window window = (Window) param.thisObject;
					Context context = window.getContext();
					String localClassName = context.getClass().getPackage().getName();

					//if (!gotFloat) return;
					 if (!class_boolean.startsWith(localClassName + ID_TAG)) return;
					//TODO :if (!localClassName.equals("com.whatsapp")) {
					//TODO :      return;
					//TODO :       }
					 XposedBridge.log("XHaloFloatingWindow-DEBUG(DecorView): " + class_boolean);

					appleFloating(context, window,localClassName);
					
				}
			});
		} catch (Exception e) { XposedBridge.log("XHaloFloatingWindow-ERROR(DecorView): " + e.toString());
		}
	}
	
	public static void appleFloating(Context context , Window mWindow, String class_name ){
		Intent intent__ = new Intent(context.getPackageManager().getLaunchIntentForPackage(class_name));
	        	ResolveInfo rInfo = context.getPackageManager().resolveActivity(intent__, 0);
	        	ActivityInfo info = rInfo.activityInfo;	            
	        	TypedArray ta = context.obtainStyledAttributes(info.theme, com.android.internal.R.styleable.Window);
	        	
	            TypedValue backgroundValue = ta.peekValue(com.android.internal.R.styleable.Window_windowBackground);
	            // Apps that have no title don't need no title bar
	            boolean gotTitle = ta.getBoolean(com.android.internal.R.styleable.Window_windowNoTitle, false);
	            if (gotTitle) mWindow.requestFeature(Window.FEATURE_NO_TITLE);
	           
	            if (backgroundValue != null && backgroundValue.toString().contains("light")) {
	                context.getTheme().applyStyle(R.style.Theme_Halo_FloatingWindowLight, true);
	            } else {  //Checks if light or dark theme
	                context.getTheme().applyStyle(R.style.Theme_Halo_FloatingWindow, true);
	            }
	            
	            ta.recycle();
	            // Create our new window
	           //mWindow.mIsFloatingWindow = true; < We dont need this. onCreate Hook will compare getTaskId and resize accordingly
	            mWindow.setCloseOnTouchOutsideIfNotSet(true);
	            mWindow.setGravity(Gravity.CENTER);
	            mWindow.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	            WindowManager.LayoutParams params = mWindow.getAttributes(); 
	            //TODO : transparency amount customization
				Float alp = pref.getFloat(Res.KEY_ALPHA, Res.DEFAULT_ALPHA);
				Float dimm = pref.getFloat(Res.KEY_DIM, Res.DEFAULT_DIM);

	            params.alpha = alp;	
	            params.dimAmount = dimm;
	            mWindow.setAttributes((android.view.WindowManager.LayoutParams) params);
		        scaleFloatingWindow(context,mWindow);
	}

	public static void scaleFloatingWindow(Context context ,  Window mWindow ) {		
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics); 
        //TODO : make width/height customizable 
        if (metrics.heightPixels > metrics.widthPixels) { // portrait 
            mWindow.setLayout((int)(metrics.widthPixels * 0.95f), (int)(metrics.heightPixels * 0.7f));
        } else {  // landscape
        	mWindow.setLayout((int)(metrics.widthPixels * 0.7f), (int)(metrics.heightPixels * 0.85f));
        }
    }

}


 
