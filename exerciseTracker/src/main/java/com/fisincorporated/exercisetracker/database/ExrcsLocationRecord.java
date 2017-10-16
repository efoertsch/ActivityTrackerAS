package com.fisincorporated.exercisetracker.database;

public class ExrcsLocationRecord {
	public static final long INVALID_ROWID = -1;
	private long _id = INVALID_ROWID;
	private String location = "";
	private int timesUsed= 0;
	
	private boolean mSelectable = true;
	
	public ExrcsLocationRecord(){
		super();
	}
	
	public ExrcsLocationRecord(Long _id, String location) {
		// this will be INVALID_ROWID for unsaved records
		this._id = _id;
		this.location = location;
		this.timesUsed = 0;
	}
	public ExrcsLocationRecord(long _id, String location, int timesUsed) {
		set_id(_id);
		setLocation(location);
		setTimesUsed(timesUsed);
		
	}

	public boolean isSelectable() {
		return mSelectable;
	}

	public long get_id() {
		return _id;
	}

	public void set_id(long _id) {
		this._id = _id;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getTimesUsed() {
		return timesUsed;
	}

	public void setTimesUsed(int timesUsed) {
		this.timesUsed = timesUsed;
	}
	
}
