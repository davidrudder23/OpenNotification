/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.reliableresponse.notification.usermgmt;

import java.io.FileInputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;

import net.reliableresponse.notification.broker.BrokerFactory;

/**
 * @author drig
 * 
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class OnCallGroup extends Group {

	Vector onCallSchedules;

	private static Hashtable cachedMonths;

	static {
		BrokerFactory.getLoggingBroker().logDebug("initializing month cache");
		cachedMonths = new Hashtable();
	}
	
	public OnCallGroup() {
		onCallSchedules = new Vector();
	}
	
	public int getType() {
		return ONCALL;
	}

	public void addMember(Member member, int order) throws InvalidGroupException {
		super.addMember(member, order);
		flushCache();
	}

	public void addMembers(Member[] members) throws InvalidGroupException {
		super.addMembers(members);
		flushCache();
	}

	public void reloadMembers() {
		super.reloadMembers();
		flushCache();
	}

	public void removeMemberFromGroup(int memberNum) {
		super.removeMemberFromGroup(memberNum);
		flushCache();
	}

	private OnCallSchedule makeNewDefaultSchedule() {
		OnCallSchedule schedule = new OnCallSchedule();
		schedule.setAllDay(true);
		schedule.setRepetition(OnCallSchedule.REPEAT_DAILY);
		schedule.setRepetitionCount(1);
		schedule.setFromDate(new Date());
		schedule.setToDate(new Date());
		schedule.setAllDay(true);
		return schedule;
	}

	public void setOnCallSchedule(OnCallSchedule schedule, int memberNum) {
		setOnCallSchedule(schedule, memberNum, true);

	}

	public void setOnCallSchedule(OnCallSchedule schedule, int memberNum, boolean flush) {
		// If we get a member number that's higher than the currently set
		// schedules, backfill w/ dummy data. Otherwise, we can't store the
		// schedule in the vector
		while (memberNum > onCallSchedules.size()) {
			OnCallSchedule newSchedule = makeNewDefaultSchedule();
			onCallSchedules.addElement(newSchedule);
			BrokerFactory.getGroupMgmtBroker().setOnCallSchedule(newSchedule, this, onCallSchedules.size() - 1);
		}
		if (memberNum == onCallSchedules.size()) {
			onCallSchedules.addElement(schedule);
		} else {
			onCallSchedules.setElementAt(schedule, memberNum);
		}
		if (flush)
			flushCache();
		if (autocommit) {
			BrokerFactory.getGroupMgmtBroker().setOnCallSchedule(schedule, this, memberNum);
		}
	}

	public OnCallSchedule[] getOnCallSchedules() {
		OnCallSchedule[] schedules = new OnCallSchedule[members.size()];
		for (int i = 0; i < schedules.length; i++) {
			OnCallSchedule schedule = null;
			if (i < onCallSchedules.size()) {
				schedule = (OnCallSchedule) onCallSchedules.elementAt(i);
			}
			if (schedule == null) {
				schedule = BrokerFactory.getGroupMgmtBroker().getOnCallSchedule(this, i);
			}
			if (schedule == null) {
				schedule = makeNewDefaultSchedule();
			}
			setAutocommit(false);
			setOnCallSchedule(schedule, i, false);
			setAutocommit(true);
			schedules[i] = schedule;
		}
		return schedules;
	}

	public Member[] getOnCallMembers(Date date) {
		int monthInt = date.getMonth();
		int yearInt = date.getYear();
		Vector[][][] month = getMonth(monthInt, yearInt);
		Vector members = month[date.getMinutes()][date.getHours()][date.getDate()];
		if (members == null) {
			return new Member[0];
		}
		return (Member[]) (members.toArray(new Member[0]));
	}

	private int daysInMonth(GregorianCalendar c) {
		return c.getActualMaximum(GregorianCalendar.DATE);
	}

	private void flushCache() {
		BrokerFactory.getLoggingBroker().logDebug("flushing month cache");
		OnCallGroup.cachedMonths.clear();
	}

	/**
	 * This builds a month's representation of this oncall schedule. It will
	 * return who is on call for each [minute][hour][day]
	 * 
	 * @param month
	 *            The number of the month (January is 0, December is 11)
	 * @param year
	 *            The number of the year
	 * @return An array representing the month's minutes/hours/days
	 */
	public Vector[][][] getMonth(int monthInt, int yearInt) {
		Vector[][][] month = (Vector[][][]) OnCallGroup.cachedMonths.get(monthInt + "/" + yearInt);
		if (month == null) {
			month = innerGetMonth(monthInt, yearInt);
			BrokerFactory.getLoggingBroker().logDebug("Adding month to cache");
			OnCallGroup.cachedMonths.put(monthInt + "/" + yearInt, month);
		}

		return month;

	}

	private Vector[][][] innerGetMonth(int month, int year) {

		BrokerFactory.getLoggingBroker().logDebug("inner get month for " + month + "/" + year);
		// Check for the number of days in a month
		GregorianCalendar calendarForCheckingDays = new GregorianCalendar(year, month + 1, 0);

		// Create an initialize the representation
		Vector<Member>[][][] representation = new Vector[60][24][daysInMonth(calendarForCheckingDays)];
		for (int d = 0; d < representation[0][0].length; d++) {
			for (int h = 0; h < 24; h++) {
				for (int m = 0; m < 60; m++) {
					representation[m][h][d] = new Vector();
				}
			}
		}
		Member[] members = getMembers();
		OnCallSchedule[] schedules = getOnCallSchedules();
		if (members == null) {
			return representation;
		}

		// Loop through for each member
		for (int memberNum = members.length - 1; memberNum >= 0; memberNum--) {
			Member member = members[memberNum];
			OnCallSchedule schedule = schedules[memberNum];

			// This section figures out how much we skip for each increment
			int repetitionDays = 0;
			int repetitionMonths = 0;

			if (schedule.getRepetition() == OnCallSchedule.REPEAT_DAILY) {
				repetitionDays = schedule.getRepetitionCount();
			} else if (schedule.getRepetition() == OnCallSchedule.REPEAT_WEEKLY) {
				repetitionDays = schedule.getRepetitionCount() * 7;
			} else if (schedule.getRepetition() == OnCallSchedule.REPEAT_MONTHLY_DATE) {
				repetitionMonths = schedule.getRepetitionCount();
			} else if (schedule.getRepetition() == OnCallSchedule.REPEAT_MONTHLY_DAY) {
				repetitionMonths = schedule.getRepetitionCount();
			}

			Date from = schedule.getFromDate();
			Date to = schedule.getToDate();

			BrokerFactory.getLoggingBroker().logDebug("from=" + from + "\nto=" + to);

			// Make sure the from is earlier than the to
			if (to.compareTo(from) < 0) {
				Date temp = (Date) to.clone();
				to = (Date) from.clone();
				from = (Date) temp.clone();
			}

			// This adjusts the to date, so that it doesn't match the time it
			// ends on
			// like, 9AM to 5PM really ends on 4:59:59 PM
			to.setSeconds(to.getSeconds() - 1);

			Date pointer = (Date) from.clone();
			int daysBeforeBeginningOfMonth = 0;
			if ((pointer.getMonth() != to.getMonth()) &&
				(to.getYear() == year) &&
				(to.getMonth() == month)) {
				daysBeforeBeginningOfMonth = daysInMonth(new GregorianCalendar(from.getYear(), from.getMonth() + 1, 0)) - (pointer.getDate()-1);
				pointer.setDate(1);
				pointer.setMonth(to.getMonth());
				pointer.setYear(to.getYear());
			}
			Date endPointer = (Date) to.clone();
			BrokerFactory.getLoggingBroker().logDebug("starting pointer=" + pointer);
			BrokerFactory.getLoggingBroker().logDebug("starting end pointer=" + endPointer);

			// If there's no repetition, then we only need
			// to do this part if the month matches
			if ((schedule.getRepetitionCount() > 0) || 
					((pointer.getMonth() == month) && (pointer.getYear() == year)) ||
					((endPointer.getMonth() == month) && (endPointer.getYear() == year))
				) {
				// Actually do the skip until we're in the right year
				while ((pointer.getYear() < year) && (endPointer.getYear() < year)) {
					pointer.setDate(pointer.getDate() + repetitionDays);
					pointer.setMonth(pointer.getMonth() + repetitionMonths);
					endPointer.setDate(endPointer.getDate() + repetitionDays);
					endPointer.setMonth(endPointer.getMonth() + repetitionMonths);
					//BrokerFactory.getLoggingBroker().logDebug("pointer=" + pointer);
					//BrokerFactory.getLoggingBroker().logDebug("end pointer=" + endPointer);
					//BrokerFactory.getLoggingBroker().logDebug("pointer=" + pointer.getYear());
					//BrokerFactory.getLoggingBroker().logDebug("end pointer=" + endPointer.getYear());
					//BrokerFactory.getLoggingBroker().logDebug("year=" + year);
				}
				//BrokerFactory.getLoggingBroker().logDebug("pointer's year=" + pointer.getYear());
				//BrokerFactory.getLoggingBroker().logDebug("end pointer's year=" + endPointer.getYear());

				// Now move up to the right month
				while ((pointer.getMonth() != month) && (endPointer.getMonth() != month) &&
						(pointer.getYear() != year) && (endPointer.getYear() != year)) {
					pointer.setDate(pointer.getDate() + repetitionDays);
					pointer.setMonth(pointer.getMonth() + repetitionMonths);
					endPointer.setDate(endPointer.getDate() + repetitionDays);
					endPointer.setMonth(endPointer.getMonth() + repetitionMonths);

				}
				//BrokerFactory.getLoggingBroker().logDebug("pointer's month=" + pointer.getMonth());
				//BrokerFactory.getLoggingBroker().logDebug("end pointer's month=" + endPointer.getMonth());

				//BrokerFactory.getLoggingBroker().logDebug("pointer=" + pointer);
				//BrokerFactory.getLoggingBroker().logDebug("end pointer=" + endPointer);
				// We use this to figure out the distance, in days, between the
				// start and end
				int startLength = from.getDate();
				int endLength = to.getDate();
				if (endLength < startLength) {
					BrokerFactory.getLoggingBroker().logDebug(
							"from's month is "
									+ daysInMonth(new GregorianCalendar(from.getYear(), from.getMonth() + 1, 0))
									+ " days long");
					endLength += daysInMonth(new GregorianCalendar(from.getYear(), from.getMonth() + 1, 0));
				}
				int numDays = endLength - startLength;
				//BrokerFactory.getLoggingBroker().logDebug("schedule is " + numDays + " days long");

				int monthsTimeYears = month+(year*12);
				int pointerMonthsTimeYears = pointer.getMonth()+(pointer.getYear()*12);
				// Now, fill in the month
				while (pointerMonthsTimeYears <= monthsTimeYears) {
					int d = pointer.getDate() - 1;

					// Create the "from" pointer. This one will move
					// towards the "to" pointer
					Date fromPointer = (Date) pointer.clone();
					fromPointer.setMinutes(from.getMinutes());
					fromPointer.setHours(from.getHours());

					// if the from is the previous month,
					// like this goes from March 29 to April 10th,
					// and we're looking at March
					// this will set it to the 1st day of the month
					if (fromPointer.getMonth() != month) {
						fromPointer.setDate(1);
						fromPointer.setMonth(month);
						fromPointer.setYear(year);
						fromPointer.setHours(0);
						fromPointer.setMinutes(0);
						fromPointer.setSeconds(0);
					}

					// This is the end-point. From will move towards To
					Date toPointer = (Date) pointer.clone();
					toPointer.setMinutes(to.getMinutes());
					toPointer.setHours(to.getHours());
					toPointer.setDate(toPointer.getDate() + numDays);
					
					// Adjust for the times where the schedule started
					// before the 1st of this month
					toPointer.setDate(toPointer.getDate()-daysBeforeBeginningOfMonth);

					// Adjust for a To date that is the next month.
					// Like, this goes from March 29 to April 10th,
					// and we're looking at April
					// this will set it to the last day of the month
					if (toPointer.getMonth() > month) {
						toPointer.setMonth(month);
						toPointer.setDate(daysInMonth(calendarForCheckingDays));
						toPointer.setHours(23);
						toPointer.setMinutes(59);
						toPointer.setSeconds(59);
					}

					// Adjust for an all day schedule
					// BrokerFactory.getLoggingBroker().logDebug("Is all day? "+schedule.isAllDay());
					if (schedule.isAllDay()) {
						pointer.setMinutes(0);
						pointer.setHours(0);
						pointer.setSeconds(0); // shouldn't be necessary
						fromPointer.setHours(0);
						fromPointer.setMinutes(0);
						fromPointer.setSeconds(0); // shouldn't be necessary
						toPointer.setHours(23);
						toPointer.setMinutes(59);
						toPointer.setSeconds(59); // shouldn't be necessary
					}

					//BrokerFactory.getLoggingBroker().logDebug("from pointer=" + fromPointer);

					// From minutes and To minutes are the number of minutes
					// into the
					// day they are. Like 3AM is 180 minutes into the day.
					int fromMinutes = (fromPointer.getHours() * 60) + fromPointer.getMinutes();
					int toMinutes = (toPointer.getHours() * 60) + toPointer.getMinutes();
					//BrokerFactory.getLoggingBroker().logDebug("fromMinutes=" + fromMinutes);
					//BrokerFactory.getLoggingBroker().logDebug("toMinutes=" + toMinutes);

					// This handles the case where the from is less than the
					// to.
					// The "normal" case, which is probably going to be
					// around
					// 50%
					// of the time
					// BrokerFactory.getLoggingBroker().logDebug("toMinutes="+toMinutes);
					if (fromMinutes < toMinutes) {
						/*
						 * BrokerFactory.getLoggingBroker().logDebug("fromTime="
						 * + fromPointer.getTime());
						 * BrokerFactory.getLoggingBroker
						 * ().logDebug("toTime="+toPointer.getTime());
						 * BrokerFactory
						 * .getLoggingBroker().logDebug("fromHour="+
						 * fromPointer.getHours());
						 * BrokerFactory.getLoggingBroker
						 * ().logDebug("toHour="+toPointer.getHours());
						 * BrokerFactory .getLoggingBroker().logDebug("fromMin="
						 * +fromPointer .getMinutes());
						 * BrokerFactory.getLoggingBroker().logDebug
						 * ("toMin="+toPointer.getMinutes());
						 */
						// start at From and move to To
						//BrokerFactory.getLoggingBroker().logDebug("Filling in the minutes from "+fromPointer+" to "+toPointer);
						while ((fromPointer.getTime() <= toPointer.getTime()) && (fromPointer.getMonth() == month)) {
							if ((fromPointer.getHours() < toPointer.getHours())
									|| ((fromPointer.getHours() == toPointer.getHours()) && (fromPointer.getMinutes() <= toPointer
											.getMinutes()))) {
								//if ((fromPointer.getHours() >= pointer.getHours())
								//		&& (fromPointer.getMinutes() >= pointer.getMinutes())) {
									// Actually mark the minute with the
									// member
									representation[fromPointer.getMinutes()][fromPointer.getHours()][fromPointer
											.getDate() - 1].addElement(member);
								//}
							}
							fromPointer.setMinutes(fromPointer.getMinutes() + 1);
						}
					}
					// This handles the case where the To minutes is less
					// than
					// the From
					// This handles in day-spanning cases. Like, the
					// nightshift
					// is 10PM to 6AM. From 10PM is later than To 6AM
					// It does it in 2 steps, from midnight to the end, and
					// from the start to midnight
					else if (fromMinutes > toMinutes) {
						while ((fromPointer.getTime() <= toPointer.getTime()) && (fromPointer.getMonth() == month)) {
							//BrokerFactory.getLoggingBroker().logDebug("doing the nightshift case");
							
							// Fill in from the "from" time to the end of the day
							//BrokerFactory.getLoggingBroker().logDebug("filling from "+fromMinutes+" to "+(60*24));
							for (int i = fromMinutes; i < (60 * 24); i++) {
								representation[i % 60][i / 60][fromPointer.getDate() - 1].addElement(member);
							}

							// Fill in from the beginning of the day to the "to" time, but only if From isn't the last
							// day of the month
							if (daysInMonth(calendarForCheckingDays) > fromPointer.getDate()) {
								for (int i = 0; i <= toMinutes; i++) {
									representation[i % 60][i / 60][fromPointer.getDate()].addElement(member);
								}
							}
							fromPointer.setDate(fromPointer.getDate() + 1);
						}
					}
					//toPointer.setDate(toPointer.getDate() + 1);

					pointer.setDate(pointer.getDate() + repetitionDays);
					pointer.setMonth(pointer.getMonth() + repetitionMonths);

					if ((repetitionDays <= 0) && (repetitionMonths <= 0)) {
						pointer.setMonth(month + 1);
					}
					monthsTimeYears = month+(year*12);
					pointerMonthsTimeYears = pointer.getMonth()+(pointer.getYear()*12);
				}
			}
		}

		return representation;
	}
	
	private Vector innerGetMembers(Member member, int day, int month, int year, int hours, int minutes, int count) {
		Vector members = new Vector();
		if (count > 20) return members;
		if (member instanceof User) {
			members.addElement(member);
			return members;
		} else {
			Member[] groupsMembers = ((Group)member).getMembers();
			for (int memberNum = 0; memberNum < groupsMembers.length; memberNum++) {
				Member groupMember = groupsMembers[memberNum];
				if (groupMember instanceof OnCallGroup) {
					members.addAll(((OnCallGroup)groupMember).getIndividualMembers(day, month, year, hours, minutes));
				}
				members.addAll(innerGetMembers(groupMember, day, month, year, hours, minutes, count++));
			}
		}
		return members;
	}
	
	public Vector getIndividualMembers(Date date) {
		int day = date.getDate()-1;
		int month = date.getMonth();
		int year = date.getYear();
		int hours = date.getHours();
		int minutes = date.getMinutes();
		
		return getIndividualMembers(day, month, year, hours, minutes);
	}
	
	public Vector getIndividualMembers(int day, int month, int year, int hours, int minutes) {

		Vector members = new Vector();
		
		Vector[][][] representation = getMonth(month, year);
		
		if (day > representation[0][0].length) {
			return members;
		}
		
		Vector daysMembers = representation[minutes][hours][day];
		for (int memberNum = 0; memberNum < daysMembers.size(); memberNum++) {
			Member member = (Member)daysMembers.elementAt(memberNum);
			if (member instanceof User) {
				members.addElement(member);
			} else {
				members.addAll(innerGetMembers(member, day, month, year, hours, minutes, 0));
			}
		}
		
		return members;
	}

	public static void main(String[] args) throws Exception {
		BrokerFactory.getConfigurationBroker().setConfiguration(new FileInputStream("conf/reliable.properties"));

		OnCallGroup oc = (OnCallGroup) BrokerFactory.getGroupMgmtBroker().getGroupByName("oc");
		System.out.print(oc.getIndividualMembers(new Date(109, 11, 28, 5, 59)));
/*		for (int h = 0; h < 24; h++) {
			for (int m = 0; m < 60; m ++) {
				System.out.print(new Date(2009, 11, 28, h, m));
				System.out.print(oc.getIndividualMembers(new Date(2009, 11, 28, h, m)));
			}
		}
		Vector[][][] representation = (oc).getMonth(11, 109);
		
		
		for (int d = 0; d < representation[0][0].length; d++) {
			for (int h = 0; h < 24; h++) {
				for (int m = 0; m < 60; m ++) {
					System.out.print(representation[m][h][d].size());
				}
				System.out.println ("\n");
			}
			
			System.out.println ("\n------------\n");
		}
*/
	}

}
