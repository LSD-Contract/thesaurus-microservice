package com.lsd.thesaurus.utilities;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class Utilities {

	public static String convertLocalDateToDDMMMYYYYForamt(LocalDate localDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
		Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		return sdf.format(date);
	}
	
	public static String convertLocalDateToDDMMMYYYYForamt(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
		return sdf.format(date);
	}
	
	public static LocalDate convertDateToLocalDate(Date dateToConvert) {
	    return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
	
	public static Date convertLocalDateToDate(LocalDate localDateToConvert) {
	    return java.util.Date.from(localDateToConvert.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}
}
