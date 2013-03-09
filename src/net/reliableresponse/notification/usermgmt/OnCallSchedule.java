/*
 * Created on Apr 10, 2006
 *
 *Copyright Reliable Response, 2006
 */
package net.reliableresponse.notification.usermgmt;

import java.util.Date;

public class OnCallSchedule {
	public static final int REPEAT_DAILY = 0;
	public static final int REPEAT_WEEKLY = 1;
	public static final int REPEAT_MONTHLY_DAY = 2;
	public static final int REPEAT_MONTHLY_DATE = 3;
	
	boolean allDay;
	Date fromDate, toDate;

	int repetition;
	int repetitionCount = 1;
	
	public OnCallSchedule() {
		
	}

	public boolean isAllDay() {
		return allDay;
	}

	public void setAllDay(boolean allDay) {
		this.allDay = allDay;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public int getRepetition() {
		return repetition;
	}

	public void setRepetition(int repetition) {
		this.repetition = repetition;
	}

	public int getRepetitionCount() {
		if (repetitionCount < 0) {
			return 1;
		}
		return repetitionCount;
	}

	public void setRepetitionCount(int repetitionCount) {
		this.repetitionCount = repetitionCount;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public String toString() {
		StringBuffer ret = new StringBuffer();
		switch (repetition) {
		case REPEAT_DAILY: ret.append ("Daily");
		break;
		case REPEAT_WEEKLY: ret.append ("Weekly");
		break;
		case REPEAT_MONTHLY_DAY: ret.append ("Monthly/day");
		break;
                case REPEAT_MONTHLY_DATE: ret.append ("Monthly/date");
                break;
		}

		ret.append (" schedule starting on ");
		ret.append (fromDate);
		ret.append (" going to ");
		ret.append (toDate);
		if (repetitionCount == 0) {
			ret.append (", not repeating");
		} else {
			ret.append (", repeating every "+repetitionCount);
		}
		if (isAllDay()) {
			ret.append (", and lasting all day");
		}

		return ret.toString();
	}

}
