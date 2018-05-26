package badtzmarupekkle.littlethings.endpoint;

import java.util.ArrayList;
import java.util.List;

import badtzmarupekkle.littlethings.entity.TimeSlot;
import badtzmarupekkle.littlethings.entity.TimeSlotResponse;
import badtzmarupekkle.littlethings.util.ErrorManager;
import badtzmarupekkle.littlethings.util.Validation;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Text;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

@Api(name = "timeslotendpoint", namespace = @ApiNamespace(ownerDomain = "littlethings.com", ownerName = "BadtzMaruPekkle", packagePath = "endpoint/timeslot"))
public class TimeSlotEndpoint {
    private static final int COUNT_DAYS = 7;
    private static final int LENGTH_DESCRIPTION = 500;
    private static final int LENGTH_LOCATION = 50;
    private static final int LENGTH_NAME = 25;
    private static final int LIMIT_SCHEDULE_SIZE = 10;
    private static final int MAX_DAY = 31;
    private static final int MAX_HOUR = 23;
    private static final int MAX_MINUTE = 59;
    private static final int MAX_MONTH = 12;
    private static final int MIN_YEAR = 15;
    private static final int NO_DATE_TIME = 99;

    private static final String ENTITY_TIME_SLOT = "TimeSlot";
    private static final String PROPERTY_COLOR = "Color";
    private static final String PROPERTY_DESCRIPTION = "Description";
    private static final String PROPERTY_END_DATE = "EndDate";
    private static final String PROPERTY_END_TIME = "EndTime";
    private static final String PROPERTY_FRIDAY = "Friday";
    private static final String PROPERTY_LOCATION = "Location";
    private static final String PROPERTY_MONDAY = "Monday";
    private static final String PROPERTY_NAME = "Name";
    private static final String PROPERTY_SATURDAY = "Saturday";
    private static final String PROPERTY_START_DATE = "StartDate";
    private static final String PROPERTY_START_TIME = "StartTime";
    private static final String PROPERTY_SUNDAY = "Sunday";
    private static final String PROPERTY_THURSDAY = "Thursday";
    private static final String PROPERTY_TUESDAY = "Tuesday";
    private static final String PROPERTY_WEDNESDAY = "Wednesday";
    private static final String PROPERTY_WRITER = "Writer";

    private DatastoreService datastore;

    public TimeSlotEndpoint() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    @ApiMethod(name = "add", httpMethod = HttpMethod.POST, path = "timeslot/add")
    public TimeSlotResponse add(TimeSlot timeSlot) {
        TimeSlotResponse response = new TimeSlotResponse();

        if (!Validation.validateUser(timeSlot.getSecret())) {
            response.setErrorCode(ErrorManager.ERROR_UNAUTHORIZED);
            return response;
        }

        if(!validateTimeSlot(timeSlot)) {
            response.setErrorCode(ErrorManager.ERROR_BAD_REQUEST);
            return response;
        }

        Key writerKey = WriterEndpoint.getWriterKey(timeSlot.getWriter());
        Entity timeSlotEntity = new Entity(ENTITY_TIME_SLOT, writerKey);

        List<Boolean> days = timeSlot.getDays();
        for(int i = 0; i < COUNT_DAYS; i++) {
            switch(i + 1) {
                case 1:
                    timeSlotEntity.setProperty(PROPERTY_MONDAY, days.get(i));
                    break;
                case 2:
                    timeSlotEntity.setProperty(PROPERTY_TUESDAY, days.get(i));
                    break;
                case 3:
                    timeSlotEntity.setProperty(PROPERTY_WEDNESDAY, days.get(i));
                    break;
                case 4:
                    timeSlotEntity.setProperty(PROPERTY_THURSDAY, days.get(i));
                    break;
                case 5:
                    timeSlotEntity.setProperty(PROPERTY_FRIDAY, days.get(i));
                    break;
                case 6:
                    timeSlotEntity.setProperty(PROPERTY_SATURDAY, days.get(i));
                    break;
                case 7:
                    timeSlotEntity.setProperty(PROPERTY_SUNDAY, days.get(i));
                    break;
            }
        }

        timeSlotEntity.setUnindexedProperty(PROPERTY_COLOR, timeSlot.getColor());
        timeSlotEntity.setUnindexedProperty(PROPERTY_END_DATE, convertToDate(timeSlot.getEndYear(), timeSlot.getEndMonth(), timeSlot.getEndDay()));
        timeSlotEntity.setUnindexedProperty(PROPERTY_END_TIME, convertToTime(timeSlot.getEndHour(), timeSlot.getEndMinute()));
        timeSlotEntity.setUnindexedProperty(PROPERTY_NAME, timeSlot.getName().trim());
        timeSlotEntity.setUnindexedProperty(PROPERTY_START_DATE, convertToDate(timeSlot.getStartYear(), timeSlot.getStartMonth(), timeSlot.getStartDay()));
        timeSlotEntity.setUnindexedProperty(PROPERTY_START_TIME, convertToTime(timeSlot.getStartHour(), timeSlot.getStartMinute()));
        timeSlotEntity.setUnindexedProperty(PROPERTY_WRITER, timeSlot.getWriter());

        if(Validation.isValidString(timeSlot.getDescription()))
            timeSlotEntity.setUnindexedProperty(PROPERTY_DESCRIPTION, timeSlot.getDescription().trim());

        if (Validation.isValidString(timeSlot.getLocation()))
            timeSlotEntity.setUnindexedProperty(PROPERTY_LOCATION, timeSlot.getLocation());

        int retries = ErrorManager.RETRIES;
        while (true) {
            try {
                datastore.put(timeSlotEntity);
                break;
            } catch (Exception e) {
                if (retries == 0) {
                    ErrorManager.logError(datastore, e.getMessage());
                    response.setErrorCode(ErrorManager.ERROR_GATEWAY_TIMEOUT);
                    return response;
                }
                retries--;
            }
        }

        response.setSuccess(true);
        return response;
    }

    @ApiMethod(name = "delete", httpMethod = HttpMethod.POST, path = "timeslot/delete")
    public TimeSlotResponse delete(TimeSlot timeSlot) {
        TimeSlotResponse response = new TimeSlotResponse();

        if (!Validation.validateUser(timeSlot.getSecret())) {
            response.setErrorCode(ErrorManager.ERROR_UNAUTHORIZED);
            return response;
        }

        Key writerKey = WriterEndpoint.getWriterKey(timeSlot.getWriter());
        Key timeSlotKey = KeyFactory.createKey(writerKey, ENTITY_TIME_SLOT, timeSlot.getId());

        int retries = ErrorManager.RETRIES;
        while(true) {
            try {
                datastore.delete(timeSlotKey);
                break;
            } catch (Exception e) {
                if(retries == 0) {
                    ErrorManager.logError(datastore, e.getMessage());
                    response.setErrorCode(ErrorManager.ERROR_GATEWAY_TIMEOUT);
                    return response;
                }
                retries --;
            }
        }

        response.setSuccess(true);
        return response;
    }

    @ApiMethod(name = "edit", httpMethod = HttpMethod.POST, path = "timeslot/edit")
    public TimeSlotResponse edit(TimeSlot timeSlot) {
        TimeSlotResponse response = new TimeSlotResponse();

        if (!Validation.validateUser(timeSlot.getSecret())) {
            response.setErrorCode(ErrorManager.ERROR_UNAUTHORIZED);
            return response;
        }

        if (!validateTimeSlot(timeSlot)) {
            response.setErrorCode(ErrorManager.ERROR_BAD_REQUEST);
            return response;
        }

        Key writerKey = WriterEndpoint.getWriterKey(timeSlot.getWriter());
        Key timeSlotKey = KeyFactory.createKey(writerKey, ENTITY_TIME_SLOT, timeSlot.getId());

        Entity timeSlotEntity;
        try {
           timeSlotEntity = datastore.get(timeSlotKey);
        } catch (EntityNotFoundException e) {
            response.setErrorCode(ErrorManager.ERROR_NOT_FOUND);
            return response;
        }

        TimeSlot originalTimeSlot = timeSlotEntityToTimeSlot(timeSlotEntity);

        List<Boolean> originalDays = originalTimeSlot.getDays();
        List<Boolean> days = timeSlot.getDays();
        for(int i = 0; i < COUNT_DAYS; i++) {
            if (originalDays.get(i) != days.get(i)) {
                switch (i + 1) {
                    case 1:
                        timeSlotEntity.setProperty(PROPERTY_MONDAY, days.get(i));
                        break;
                    case 2:
                        timeSlotEntity.setProperty(PROPERTY_TUESDAY, days.get(i));
                        break;
                    case 3:
                        timeSlotEntity.setProperty(PROPERTY_WEDNESDAY, days.get(i));
                        break;
                    case 4:
                        timeSlotEntity.setProperty(PROPERTY_THURSDAY, days.get(i));
                        break;
                    case 5:
                        timeSlotEntity.setProperty(PROPERTY_FRIDAY, days.get(i));
                        break;
                    case 6:
                        timeSlotEntity.setProperty(PROPERTY_SATURDAY, days.get(i));
                        break;
                    case 7:
                        timeSlotEntity.setProperty(PROPERTY_SUNDAY, days.get(i));
                        break;
                }
            }
        }

        if (originalTimeSlot.getColor() != timeSlot.getColor())
            timeSlotEntity.setUnindexedProperty(PROPERTY_COLOR, timeSlot.getColor());
        if (originalTimeSlot.getEndYear() != timeSlot.getEndYear() || originalTimeSlot.getEndMonth() != timeSlot.getEndMonth() || originalTimeSlot.getEndDay() != timeSlot.getEndDay())
            timeSlotEntity.setUnindexedProperty(PROPERTY_END_DATE, convertToDate(timeSlot.getEndYear(), timeSlot.getEndMonth(), timeSlot.getEndDay()));
        if (originalTimeSlot.getEndHour() != timeSlot.getEndHour() || originalTimeSlot.getEndMinute() != timeSlot.getEndMinute())
            timeSlotEntity.setUnindexedProperty(PROPERTY_END_TIME, convertToTime(timeSlot.getEndHour(), timeSlot.getEndMinute()));
        if (!originalTimeSlot.getName().equals(timeSlot.getName()))
            timeSlotEntity.setUnindexedProperty(PROPERTY_NAME, timeSlot.getName());
        if (originalTimeSlot.getStartYear() != timeSlot.getStartYear() || originalTimeSlot.getStartMonth() != timeSlot.getStartMonth() || originalTimeSlot.getStartDay() != timeSlot.getStartDay())
            timeSlotEntity.setUnindexedProperty(PROPERTY_START_DATE, convertToDate(timeSlot.getStartYear(), timeSlot.getStartMonth(), timeSlot.getStartDay()));
        if (originalTimeSlot.getStartHour() != timeSlot.getStartHour() || originalTimeSlot.getStartMinute() != timeSlot.getStartHour())
            timeSlotEntity.setUnindexedProperty(PROPERTY_START_TIME, convertToTime(timeSlot.getStartHour(), timeSlot.getStartMinute()));

        if(Validation.isValidString(timeSlot.getDescription()))
            timeSlotEntity.setUnindexedProperty(PROPERTY_DESCRIPTION, timeSlot.getDescription().trim());
        else
            timeSlotEntity.removeProperty(PROPERTY_DESCRIPTION);

        if (Validation.isValidString(timeSlot.getLocation()))
            timeSlotEntity.setUnindexedProperty(PROPERTY_LOCATION, timeSlot.getLocation().trim());
        else
            timeSlotEntity.removeProperty(PROPERTY_LOCATION);

        int retries = ErrorManager.RETRIES;
        while (true) {
            try {
                datastore.put(timeSlotEntity);
                break;
            } catch (Exception e) {
                if (retries == 0) {
                    ErrorManager.logError(datastore, e.getMessage());
                    response.setErrorCode(ErrorManager.ERROR_GATEWAY_TIMEOUT);
                    return response;
                }
                retries--;
            }
        }

        response.setSuccess(true);
        return response;
    }

    @ApiMethod(name = "get", httpMethod = HttpMethod.POST, path = "timeslot/get")
    public TimeSlotResponse get(TimeSlot timeSlot) {
        TimeSlotResponse response = new TimeSlotResponse();

        if (!Validation.validateUser(timeSlot.getSecret())) {
            response.setErrorCode(ErrorManager.ERROR_UNAUTHORIZED);
            return response;
        }

        Key writerKey = WriterEndpoint.getWriterKey(timeSlot.getWriter());
        Key timeSlotKey = KeyFactory.createKey(writerKey, ENTITY_TIME_SLOT, timeSlot.getId());
        Entity timeSlotEntity;
        try {
            timeSlotEntity = datastore.get(timeSlotKey);
        } catch (EntityNotFoundException e) {
            response.setErrorCode(ErrorManager.ERROR_NOT_FOUND);
            return response;
        }

        response.setTimeSlot(timeSlotEntityToTimeSlot(timeSlotEntity));
        response.setSuccess(true);
        return response;
    }

    @ApiMethod(name = "getDay", httpMethod = HttpMethod.POST, path = "timeslot/getDay")
    public TimeSlotResponse getDay(TimeSlot timeSlot) {
        TimeSlotResponse response = new TimeSlotResponse();

        if (!Validation.validateUser(timeSlot.getSecret())) {
            response.setErrorCode(ErrorManager.ERROR_UNAUTHORIZED);
            return response;
        }

        Key writerkey = WriterEndpoint.getWriterKey(timeSlot.getWriter());
        List<TimeSlot> day = new ArrayList<>();
        DateTime instant = new DateTime(DateTimeZone.UTC);
        long convertedInstant = convertToDate(instant.getYear(), instant.getMonthOfYear(), instant.getDayOfMonth());
        String ids = "";

        for(int i = 0; i < COUNT_DAYS; i++) {
            if(timeSlot.getDays().get(i)) {
                Query q = new Query(ENTITY_TIME_SLOT).setAncestor(writerkey);
                Filter filter;
                switch(i + 1) {
                    case 1:
                        filter = new FilterPredicate(PROPERTY_MONDAY, FilterOperator.EQUAL, true);
                        break;
                    case 2:
                        filter = new FilterPredicate(PROPERTY_TUESDAY, FilterOperator.EQUAL, true);
                        break;
                    case 3:
                        filter = new FilterPredicate(PROPERTY_WEDNESDAY, FilterOperator.EQUAL, true);
                        break;
                    case 4:
                        filter = new FilterPredicate(PROPERTY_THURSDAY, FilterOperator.EQUAL, true);
                        break;
                    case 5:
                        filter = new FilterPredicate(PROPERTY_FRIDAY, FilterOperator.EQUAL, true);
                        break;
                    case 6:
                        filter = new FilterPredicate(PROPERTY_SATURDAY, FilterOperator.EQUAL, true);
                        break;
                    case 7:
                        filter = new FilterPredicate(PROPERTY_SUNDAY, FilterOperator.EQUAL, true);
                        break;
                    default:
                        continue;
                }
                q.setFilter(filter);
                PreparedQuery pq = datastore.prepare(q);

                for(Entity result : pq.asIterable(FetchOptions.Builder.withChunkSize(LIMIT_SCHEDULE_SIZE))) {
                    TimeSlot currTimeSlot = timeSlotEntityToTimeSlot(result);
                    if (!ids.contains(Long.toString(currTimeSlot.getId()))) {
                        ids += currTimeSlot.getId();
                        if (currTimeSlot.getStartMinute() != NO_DATE_TIME && currTimeSlot.getEndMinute() != NO_DATE_TIME) {
                            if (convertedInstant > convertToDate(currTimeSlot.getEndYear(), currTimeSlot.getEndMonth(), currTimeSlot.getEndDay()))
                                delete(timeSlot.getWriter(), currTimeSlot.getId());
                            else if (convertedInstant >= convertToDate(currTimeSlot.getStartYear(), currTimeSlot.getStartMonth(), currTimeSlot.getStartDay()))
                                day.add(timeSlotEntityToTimeSlot(result));
                        } else if (currTimeSlot.getStartMinute() == NO_DATE_TIME) {
                            if (convertedInstant > convertToDate(currTimeSlot.getEndYear(), currTimeSlot.getEndMonth(), currTimeSlot.getEndDay()))
                                delete(timeSlot.getWriter(), currTimeSlot.getId());
                            else
                                day.add(timeSlotEntityToTimeSlot(result));
                        } else {
                            if (convertedInstant >= convertToDate(currTimeSlot.getStartYear(), currTimeSlot.getStartMonth(), currTimeSlot.getStartDay()))
                                day.add(timeSlotEntityToTimeSlot(result));
                        }
                    }
                }
            }
        }

        response.setTimeSlots(day);
        response.setSuccess(true);
        return response;
    }

    private TimeSlot timeSlotEntityToTimeSlot(Entity e) {
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setColor(((Long) e.getProperty(PROPERTY_COLOR)).intValue());
        timeSlot.setId(e.getKey().getId());
        timeSlot.setName((String) e.getProperty(PROPERTY_NAME));
        timeSlot.setWriter((boolean) e.getProperty(PROPERTY_WRITER));

        int[] endDate = convertFromDate((long) e.getProperty(PROPERTY_END_DATE));
        timeSlot.setEndDay(endDate[0]);
        timeSlot.setEndMonth(endDate[1]);
        timeSlot.setEndYear(endDate[2]);

        int[] startDate = convertFromDate((long) e.getProperty(PROPERTY_START_DATE));
        timeSlot.setStartDay(startDate[0]);
        timeSlot.setStartMonth(startDate[1]);
        timeSlot.setStartYear(startDate[2]);

        int[] endTime = convertFromTime((long) e.getProperty(PROPERTY_END_TIME));
        timeSlot.setEndMinute(endTime[0]);
        timeSlot.setEndHour(endTime[1]);

        int[] startTime = convertFromTime((long) e.getProperty(PROPERTY_START_TIME));
        timeSlot.setStartMinute(startTime[0]);
        timeSlot.setStartHour(startTime[1]);

        List<Boolean> days = new ArrayList<>();
        for(int i = 0; i < COUNT_DAYS; i++) {
            switch(i+1) {
                case 1:
                    days.add((boolean) e.getProperty(PROPERTY_MONDAY));
                    break;
                case 2:
                    days.add((boolean) e.getProperty(PROPERTY_TUESDAY));
                    break;
                case 3:
                    days.add((boolean) e.getProperty(PROPERTY_WEDNESDAY));
                    break;
                case 4:
                    days.add((boolean) e.getProperty(PROPERTY_THURSDAY));
                    break;
                case 5:
                    days.add((boolean) e.getProperty(PROPERTY_FRIDAY));
                    break;
                case 6:
                    days.add((boolean) e.getProperty(PROPERTY_SATURDAY));
                    break;
                case 7:
                    days.add((boolean) e.getProperty(PROPERTY_SUNDAY));
                    break;
            }
        }
        timeSlot.setDays(days);

        if (e.hasProperty(PROPERTY_DESCRIPTION))
            timeSlot.setDescription(((Text) e.getProperty(PROPERTY_DESCRIPTION)).getValue());
        if (e.hasProperty(PROPERTY_LOCATION))
            timeSlot.setLocation((String) e.getProperty(PROPERTY_LOCATION));

        return timeSlot;
    }

    private int[] convertFromDate(long date) {
        int[] convertedDate = new int[3];
        convertedDate[0] = (int) (date % 100);
        date /= 100;
        convertedDate[1] = (int) (date % 100);
        convertedDate[2] = (int) (date / 100);

        return convertedDate;
    }

    private int[] convertFromTime(long time) {
        int[] convertedTime = new int[2];
        convertedTime[0] = (int) (time % 100);
        convertedTime[1] = (int) (time / 100);

        return convertedTime;
    }

    private long convertToDate(int year, int month, int day) {
        String dateString = "" + year;

        if (month < 10)
            dateString += "0" + month;
        else
            dateString += month;

        if (day < 10)
            dateString += "0" + day;
        else
            dateString += day;

        return Long.parseLong(dateString);
    }

    private long convertToTime(int hour, int minute) {
        String timeString = "";

        if (hour < 10)
            timeString += "0" + hour;
        else
            timeString += hour;

        if (minute < 10 )
            timeString += "0" + minute;
        else
            timeString += minute;

        return Long.parseLong(timeString);
    }

    private void delete(boolean writer, long id) {
        Key writerKey = WriterEndpoint.getWriterKey(writer);
        Key timeSlotKey = KeyFactory.createKey(writerKey, ENTITY_TIME_SLOT, id);

        int retries = ErrorManager.RETRIES;
        while(true) {
            try {
                datastore.delete(timeSlotKey);
                break;
            } catch (Exception e) {
                if(retries == 0) {
                    ErrorManager.logError(datastore, e.getMessage());
                    break;
                }
                retries--;
            }
        }
    }

    private boolean validateTimeSlot(TimeSlot timeSlot) {
        int endDay = timeSlot.getEndDay();
        int endHour = timeSlot.getEndHour();
        int endMinute = timeSlot.getEndMinute();
        int endMonth = timeSlot.getEndMonth();
        int endYear = timeSlot.getEndYear();
        int startDay = timeSlot.getStartDay();
        int startHour = timeSlot.getStartHour();
        int startMinute = timeSlot.getStartMinute();
        int startMonth = timeSlot.getStartMonth();
        int startYear = timeSlot.getStartYear();
        List<Boolean> days = timeSlot.getDays();
        String description = timeSlot.getDescription();
        String location = timeSlot.getLocation();
        String name = timeSlot.getName();

        if (startMinute != NO_DATE_TIME) {
            if (startDay <= 0 || startDay > MAX_DAY | startHour < 0 || startHour > MAX_HOUR || startMinute < 0 || startMinute > MAX_MINUTE || startMonth < 1 || startMonth > MAX_MONTH || startYear < MIN_YEAR)
                return false;
        }

        if (endMinute != NO_DATE_TIME) {
            if (endDay <= 0 || endDay > MAX_DAY | endHour < 0 || endHour > MAX_HOUR || endMinute < 0 || endMinute > MAX_MINUTE || endMonth < 1 || endMonth > MAX_MONTH || endYear < MIN_YEAR)
                    return false;

            DateTime endDateTime = new DateTime(endYear, endMonth, endDay, endHour, endMinute);

            if (endDateTime.getMillis() < System.currentTimeMillis())
                return false;

            if (startMinute != NO_DATE_TIME) {
                DateTime startDateTime = new DateTime(startYear, startMonth, startDay, startHour, startMinute);
                if (startDateTime.getMillis() >= endDateTime.getMillis())
                    return false;
            }
        }
        if (days == null || days.size() != COUNT_DAYS)
            return false;
        else if (!Validation.isValidString(name) || name.length() > LENGTH_NAME)
            return false;
        else if (Validation.isValidString(description) && description.length() > LENGTH_DESCRIPTION)
            return false;
        else if (Validation.isValidString(location) && description.length() > LENGTH_LOCATION)
            return false;

        return true;

    }
}
