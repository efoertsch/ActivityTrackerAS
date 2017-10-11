package com.fisincorporated.exercisetracker.database;

public class LocationRecord implements Comparable<LocationRecord>{
    public static final long INVALID_ROWID = -1;
    private long _id;
    private String location; 
    private boolean mSelectable = true; 

    public LocationRecord(String location,  Long _id) { 
	     this.location = location;
	  // this will be INVALID_LOCATION_ID for unsaved records
	     this._id = _id; 
    } 
     
    public boolean isSelectable() { 
         return mSelectable; 
    } 
     
    public void setSelectable(boolean selectable) { 
         mSelectable = selectable; 
    } 
     
    public String getLocation() { 
         return location; 
    } 
     
    public void setLocation(String location) { 
   	 this.location = location; 
    } 
        
    public long get_id() { 
         return _id; 
    } 
     
    public void set_id(long _id) { 
   	 this._id = _id; 
    } 

    public int compareTo(LocationRecord other) { 
         return (int)((this._id)-(other.get_id()));
    } 
}
