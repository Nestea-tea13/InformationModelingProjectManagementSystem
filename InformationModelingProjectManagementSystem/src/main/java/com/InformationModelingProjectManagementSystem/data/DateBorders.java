package com.InformationModelingProjectManagementSystem.data;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

public class DateBorders {

    static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    
    public static String[] getBirthdayBorders() {
        String[] dates = {"", ""}; // min, max
        dates[0] = dateFormatter.format(DateUtils.addYears(new Date(), -110)); 
        dates[1] = dateFormatter.format(DateUtils.addYears(new Date(), -4));
        return dates;
    }

}
