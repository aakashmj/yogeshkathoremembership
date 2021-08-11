package com.yogeshkathore.foundation;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by aakash on 10/13/2017.
 */
public class PatientDetailsAbstractClass {
	public static String FName="";
	public static String MName="";
	public static String LName="";
	public static String FullName="";
	public static String NIBP="";
	public static String SPO2="";
	public static String PulseRate="";
	public static String Temperature="";

	public static String Designation="";
	public static String School="";
	public static String Education="";
	public static String Number="9960201203";
	public static String MemberID="";
	public static String DOB="";
	public static boolean Register;
	public static String Address="";
	public static String District="";
	public static String Taluka="";
	public static String PinCode="";
	public static Bitmap Photo=null ;
	public static Uri GalleryPhoto=null;
	public static String PhotoPath = "empty";
	public static String GalleryPath="";
	public static Uri CameraURI=null;
	public static boolean Gallery = false;
	public static String Gender = "MALE";
	public static void ClearAllElements() {
		FullName = "";
		Number = "";
		DOB = "";
		String lang = "";
		Gender = "Male";
		Register = false;

	}
}