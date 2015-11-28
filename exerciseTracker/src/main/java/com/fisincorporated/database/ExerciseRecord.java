package com.fisincorporated.database;

import android.os.Parcel;
import android.os.Parcelable;

public class ExerciseRecord implements Parcelable {
	public static final long INVALID_ROWID = -1;
	private long _id;
	private String exercise ="";
	public static final int DEFAULT_LOG_INTERVAL = 60;
	private int defaultLogInterval = 60;
	public static final int LOG_INTERVAL = 60;
	private int logInterval = 60;
	private int logDetail = 0;
	private int timesUsed =0 ;
	// will be saved in meters, need float so if converted to feet round meters to feet with some accuracy
	public static final float MIN_DISTANCE_TO_LOG = 10f;
	private float minDistanceToLog = 10;
	// elevationInDistCalcs either 0 (don't use) or 1 (do use)
	private int elevationInDistCalcs = 0;
	
	private boolean mSelectable = true;
	
	public ExerciseRecord(){
		super();
		_id = INVALID_ROWID;
	}
	
	public ExerciseRecord(Long _id, String exercise,
			int defaultLogInterval, int logInterval, int logDetail,int timesUsed,  float minDistanceToLog, int elevationInDistCalcs) {
		// this will be INVALID_ROWID for unsaved records
		this._id = _id;
		this.exercise = exercise;
		this.defaultLogInterval = defaultLogInterval;
		this.logInterval = logInterval;
		this.logDetail = logDetail;
		this.timesUsed = timesUsed;
		this.minDistanceToLog = minDistanceToLog;
		this.elevationInDistCalcs = elevationInDistCalcs;
	}

	public ExerciseRecord(Parcel src) {
		readFromParcel(src);
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

	public String getExercise() {
		return exercise;
	}

	public void setExercise(String exercise) {
		this.exercise = exercise;
	}

	public int getDefaultLogInterval() {
		return defaultLogInterval;
	}

	public void setDefaultLogInterval(int defaultLogInterval) {
		this.defaultLogInterval = defaultLogInterval;
	}

	public int getLogInterval() {
		return logInterval;
	}

	public void setLogInterval(int logInterval) {
		this.logInterval = logInterval;
	}

	public int compareTo(LocationRecord other) {
		return (int) ((this._id) - (other.get_id()));
	}

	public int getLogDetail() {
		return logDetail;
	}

	public void setLogDetail(int logDetail) {
		if (logDetail != 0 && logDetail != 1){
			logDetail = 0;
		}
		this.logDetail = logDetail;
	}

	public int getTimesUsed() {
		return timesUsed;
	}

	public void setTimesUsed(int timesUsed) {
		this.timesUsed = timesUsed;
	}

	public float getMinDistanceToLog() {
		return minDistanceToLog;
	}

	public void setMinDistanceToLog(float minDistanceToLog) {
		this.minDistanceToLog = minDistanceToLog;
	}

	
	public int getElevationInDistCalcs() {
		return elevationInDistCalcs;
	}

	public void setElevationInDistCalcs(int elevationInDistCalcs) {
		if (elevationInDistCalcs !=0 && elevationInDistCalcs != 1 ){
			elevationInDistCalcs = 0;
		}
		this.elevationInDistCalcs = elevationInDistCalcs;
	}

	public static final Parcelable.Creator<ExerciseRecord> CREATOR = new Parcelable.Creator<ExerciseRecord>(){
		public ExerciseRecord createFromParcel(Parcel src){
			return new ExerciseRecord(src);
		}
		public ExerciseRecord[] newArray(int size){
			return new ExerciseRecord[size];
		}
	};
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(_id);
		dest.writeString(exercise);
		dest.writeInt(defaultLogInterval);
		dest.writeInt(logInterval);
		dest.writeInt(logDetail);
		dest.writeInt(timesUsed);
		dest.writeFloat(minDistanceToLog);
		dest.writeInt(elevationInDistCalcs);
			
	}
	public void readFromParcel(Parcel src){
		_id = src.readLong();
		exercise = src.readString();
		defaultLogInterval = src.readInt();
		logInterval = src.readInt();
		logDetail = src.readInt();
		timesUsed = src.readInt();
		minDistanceToLog = src.readFloat();
		elevationInDistCalcs = src.readInt();
	}
	 

}
