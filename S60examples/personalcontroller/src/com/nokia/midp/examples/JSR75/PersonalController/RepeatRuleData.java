/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.PersonalController;

import javax.microedition.pim.RepeatRule;

/**
 A container for RepeatRule data that provides an accessor method using a string to access an integer value.
*/
public final class RepeatRuleData {

    public static final String[] frequencyArray     = {"no value", "Daily", "Weekly", "Monthly", "Yearly" };
    public static final String[] monthInYearArray   ={ "no value", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
    public static final String[] weekInMonthArray   ={ "no value", "First", "Second", "Third", "Fourth", "Fifth", "Last", "Secondlast", "Thirdlast", "Fourthlast", "Fifthlast" };
    public static final String[] dayInWeekArray     ={ "no value", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };

    public static int getArrayIndex(int intValue, String[] array){
     for (int i=0; i<array.length ; i++){
            if ( intValue==getIntValues(array[i]) ) return i;
        }
        return -1; // No int value for String founded
    }

    public static int getIntValues(String str){
        int intValue=-1; // default; String not founded
        if ((str.compareTo("no value"))             == 0)   intValue = 0;
    // RepeatRule.FREQUENCY
        else if ((str.compareTo("Daily"))           == 0)   intValue = RepeatRule.DAILY;
        else if ((str.compareTo("Weekly"))          == 0)   intValue = RepeatRule.WEEKLY;
        else if ((str.compareTo("Monthly"))     == 0)   intValue = RepeatRule.MONTHLY;
        else if ((str.compareTo("Yearly"))          == 0)   intValue = RepeatRule.YEARLY;
    // RepeatRule.MONTH_IN_YEAR
        else if ((str.compareTo("January"))     == 0)   intValue = RepeatRule.JANUARY;
        else if ((str.compareTo("February"))        == 0)   intValue = RepeatRule.FEBRUARY;
        else if ((str.compareTo("March"))           == 0)   intValue = RepeatRule.MARCH;
        else if ((str.compareTo("April"))           == 0)   intValue = RepeatRule.APRIL;
        else if ((str.compareTo("May"))             == 0)   intValue = RepeatRule.MAY;
        else if ((str.compareTo("June"))            == 0)   intValue = RepeatRule.JUNE;
        else if ((str.compareTo("July"))                == 0)   intValue = RepeatRule.JULY;
        else if ((str.compareTo("August"))          == 0)   intValue = RepeatRule.AUGUST;
        else if ((str.compareTo("September"))   == 0)   intValue = RepeatRule.SEPTEMBER;
        else if ((str.compareTo("October"))     == 0)   intValue = RepeatRule.OCTOBER;
        else if ((str.compareTo("November"))        == 0)   intValue = RepeatRule.NOVEMBER;
        else if ((str.compareTo("December"))        == 0)   intValue = RepeatRule.DECEMBER;
    // RepeatRule.WEEK_IN_MONTH
        else if ((str.compareTo("First"))           == 0)   intValue = RepeatRule.FIRST;
        else if ((str.compareTo("Second"))      == 0)   intValue = RepeatRule.SECOND;
        else if ((str.compareTo("Third"))           == 0)   intValue = RepeatRule.THIRD;
        else if ((str.compareTo("Fourth"))          == 0)   intValue = RepeatRule.FOURTH;
        else if ((str.compareTo("Fifth"))           == 0)   intValue = RepeatRule.FIFTH;
        else if ((str.compareTo("Last"))            == 0)   intValue = RepeatRule.LAST;
        else if ((str.compareTo("Second last")) == 0)   intValue = RepeatRule.SECONDLAST;
        else if ((str.compareTo("Third last"))      == 0)   intValue = RepeatRule.THIRDLAST;
        else if ((str.compareTo("Fourth last")) == 0)   intValue = RepeatRule.FOURTHLAST;
        else if ((str.compareTo("Fifth last"))      == 0)   intValue = RepeatRule.FIFTHLAST;
    // RepeatRule.DAY_IN_WEEK
        else if ((str.compareTo("Sunday"))      == 0)   intValue = RepeatRule.SUNDAY;
        else if ((str.compareTo("Monday"))      == 0)   intValue = RepeatRule.MONDAY;
        else if ((str.compareTo("Tuesday"))     == 0)   intValue = RepeatRule.TUESDAY;
        else if ((str.compareTo("Wednesday"))   == 0)   intValue = RepeatRule.WEDNESDAY;
        else if ((str.compareTo("Thursday"))        == 0)   intValue = RepeatRule.THURSDAY;
        else if ((str.compareTo("Friday"))          == 0)   intValue = RepeatRule.FRIDAY;
        else if ((str.compareTo("Saturday"))        == 0)   intValue = RepeatRule.SATURDAY;
        return intValue;
    }
}
