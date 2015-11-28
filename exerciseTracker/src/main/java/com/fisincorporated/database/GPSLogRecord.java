package com.fisincorporated.database;

public class GPSLogRecord implements Comparable<GPSLogRecord>{
	
	 public static final long INVALID_GPSLOG_ID = -1; 
	   private long _id = INVALID_GPSLOG_ID; //(Database unique pk record id)
	   private long locationExerciseId;
		private float latitude;
		private float longitude;
		private int elevation;
		private String timestamp;
		private int distanceFromLastPoint;
		
	    private boolean mSelectable = true; 

	  
	    public GPSLogRecord(long _id, long locationExerciseId, float latitude, float longitude,
				int elevation, String timestamp, int distanceFromLastPoint) {
			super();
			this._id = _id;
			this.locationExerciseId = locationExerciseId;
			this.latitude = latitude;
			this.longitude = longitude;
			this.elevation = elevation;
			this.timestamp = timestamp;
			this.distanceFromLastPoint = distanceFromLastPoint;
		}

		public GPSLogRecord() {
			// TODO Auto-generated constructor stub
		}

		public boolean isSelectable() { 
	         return mSelectable; 
	    } 
	     
	    public void setSelectable(boolean selectable) { 
	         mSelectable = selectable; 
	    } 
	    
	    public long get_id() {
			return _id;
		}

		public void set_id(long _id) {
			this._id = _id;
		}

		public long getLocationExerciseId() {
			return locationExerciseId;
		}

		public void setLocationExerciseId(long locationExerciseId) {
			this.locationExerciseId = locationExerciseId;
		}

		public float getLatitude() {
			return latitude;
		}

		public void setLatitude(float latitude) {
			this.latitude = latitude;
		}

		public float getLongitude() {
			return longitude;
		}

		public void setLongitude(float longitude) {
			this.longitude = longitude;
		}

		public int getElevation() {
			return elevation;
		}

		public void setElevation(int elevation) {
			this.elevation = elevation;
		}

		public String getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}

		public int compareTo(GPSLogRecord other) { 
	         return (int)((this._id)-(other.get_id()));
	    }

		public int getDistanceFromLastPoint() {
			return distanceFromLastPoint;
		}

		public void setDistanceFromLastPoint(int distanceFromLastPoint) {
			this.distanceFromLastPoint = distanceFromLastPoint;
		}
}
