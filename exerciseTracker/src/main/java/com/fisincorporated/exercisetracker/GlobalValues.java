package com.fisincorporated.exercisetracker;

public class GlobalValues {

	public static final String LOG_TAG = "ActivityTracker";
	public static final String BUNDLE = "Bundle";
	public static final String SORT_ORDER = "SORT_ORDER";
	public static final String DATABASE_NAME = "exercise_tracker.db";

	public static final String EXERCISE_FILTER_PHRASE = "EXERCISE_FILTER_PHRASE";
	public static final String LOCATION_FILTER_PHRASE = "LOCATION_FILTER_PHRASE";
	public static final String UNDEFINED = "Undefined";
	public static final String TITLE = "Title";
	public static final String MESSAGE = "Message";
	public static final String POSITIVE_BUTTON_MSG = "Positive_button_msg";
	public static final String NEGATIVE_BUTTON_MSG = "Negative_button_msg";
	public static final String NEUTRAL_BUTTON_MSG = "Neutral_button_msg";
	public static final String KML_ROUTE_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://www.opengis.net/kml/2.2\">         <Document>";
	public static final String KML_ROUTE_TRAILER = "</coordinates></LineString></Placemark></Document></kml>";
	public static final float TIME_TO_FRACTION_HOURS = 1000 * 60 * 60;
	public static final float TIME_TO_MINUTES = 1000 * 60;
	public static final String MESSAGE_STRING = "Message_string";
	public static final String CURSOR_POSITION = "cursor_position";
	public static final String NO_DATE = "no date";
	public static final String START_DATE = "start_date";
	public static final String DISPLAY_TARGET= "display_target";
	// where to go
	public static final int DISPLAY_STATS = 1;
	public static final int DISPLAY_MAP = 2;
	public static final int DISPLAY_CHART = 3;
	// values for LoadManagers
	public static final int MAP_LOADER =1;
	public static final int ACTIVITY_LIST_LOADER = 2;
	public static final int ACTIVITY_PAGER_LOADER = 3;
	public static final int DELETE_ACTIVITY_LIST_LOADER = 4;
	public static final int EXERCISE_LIST_LOADER = 5;
	public static final int EXERCISE_PAGER_LOADER = 6;
	// values for Bar charts
	public static final String BAR_CHART_TYPE = "bar_chart_type";
	public static final int DISTANCE_VS_ELEVATION = 0;
	public static final int BAR_CHART_LAST_MONTH = 1;
	public static final int BAR_CHART_DISTANCE_WEEKLY = 2;
	public static final int BAR_CHART_DISTANCE_MONTHLY = 3;
	public static final int BAR_CHART_DISTANCE_YEARLY = 4;
	public static final int BAR_CHART_TIME_DEFAULT = BAR_CHART_LAST_MONTH;

	// For notifications
	public static final int NOTIFICATION_LOGGER = 1;

	//Shared Preferences file
	public static final String SHARED_PREFERENCES = "SHARED_PREFERENCES";

	// User selected image for startup screen
	public static final String IMAGE_DIR = "images";
	public static final String IMAGE_FILE_NAME = "user_start_image.jpeg";

	// -1 means no distance pins are to be placed on map
	public static final int DEFAULT_NO_PINS = -1;

	// Back up
	public static final String SQLITE_MIME_TYPE = "application/x-sqlite3";
	public static final String BACKUP_TYPE = "BACKUP_TYPE";
	public static final int BACKUP_TO_LOCAL = 1111;
	public static final int BACKUP_TO_DRIVE = 1234;
	public static final boolean DRIVE_SIGNIN_SUCCESSFUL = true;
	public static final boolean DRIVE_SIGNIN_UNSUCCESSFUL = false;

	public static final String DISPLAY_UNITS_PREFERENCE_KEY = "DISPLAY_UNITS_PREFERENCE_KEY";

	// Photos of course
	public static final String PHOTO_POINTS = "PHOTO_POINTS";
	public static final String PHOTO_POINT_INDEX = "PHOTO_POINT_INDEX";
	public static final String PHOTO_DETAIL_INDEX = "PHOTO_DETAIL_INDEX";
    public static final String PHOTO_DETAIL_LIST = "PHOTO_DETAIL_LIST";
    public static final String PHOTO_URI = "PHOTO_URI" ;
	public static final int PICK_PHOTO = 54321;

	public static final String DISPLAY_SELECT_CUSTOM_PHOTO = "DISPLAY_SELECT_CUSTOM_PHOTO";

}
