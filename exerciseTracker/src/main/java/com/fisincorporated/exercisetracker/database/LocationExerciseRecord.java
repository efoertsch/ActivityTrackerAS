package com.fisincorporated.exercisetracker.database;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Timestamp;


public class LocationExerciseRecord implements Comparable<LocationExerciseRecord>, Parcelable {
    public static final Long INVALID_ROWID = -1l;
    private Long _id = INVALID_ROWID; //(Database unique pk record id)
    private Long locationId = INVALID_ROWID;
    private Long exerciseId = INVALID_ROWID;
    private String description = null;
    private Timestamp startTimestamp = null;
    private Timestamp endTimestamp = null;
    private Integer distance = null;
    private Float averageSpeed = null;
    private Float startAltitude = null;
    private Float endAltitude = null;
    private Float altitudeGained = null;
    private Float altitudeLost = null;
    private Float startLatitude = null;
    private Float startLongitude = null;
    private Float endLatitude = null;
    private Float endLongitude = null;
    private Integer logInterval = null;
    private Integer logDetail = null;
    private Float maxSpeedToPoint = null;
    private Float minAltitude = null;
    private Float maxAltitude = null;
    //current or last GPS altitude recorded
    private Float currentAltitude = null;
    private String timezone = null;
    private Integer gmtHourOffset = null;
    private Integer gmtMinuteOffset = null;

    private boolean mSelectable = true;

    public LocationExerciseRecord() {
        super();
    }

    public LocationExerciseRecord(Long _id, Long locationId, Long exerciseId,
                                  String description, Timestamp startTimestamp, Timestamp endTimestamp,
                                  Integer distance, Float averageSpeed, Float startAltitude,
                                  Float endAltitude, Float altitudeGained, Float altitudeLost,
                                  Float startLatitude, Float startLongitude, Float endLatitude,
                                  Float endLongitude, Integer logInterval, Integer logDetail, Float maxSpeedToPoint,
                                  Float minAltitude, Float maxAltitude, Float currentAltitude,
                                  String timezone, Integer gmtHourOffset, Integer gmtMinuteOffset) {
        super();
        this._id = _id;
        this.locationId = locationId;
        this.exerciseId = exerciseId;
        this.description = description;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.distance = distance;
        this.averageSpeed = averageSpeed;
        this.startAltitude = startAltitude;
        this.endAltitude = endAltitude;
        this.altitudeGained = altitudeGained;
        this.altitudeLost = altitudeLost;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.logInterval = logInterval;
        this.logDetail = logDetail;
        this.maxSpeedToPoint = maxSpeedToPoint;
        this.minAltitude = minAltitude;
        this.maxAltitude = maxAltitude;
        this.currentAltitude = currentAltitude;
        this.timezone = timezone;
        this.gmtHourOffset = gmtHourOffset;
        this.gmtMinuteOffset = gmtMinuteOffset;
    }

    public boolean isSelectable() {
        return mSelectable;
    }

    public void setSelectable(boolean selectable) {
        mSelectable = selectable;
    }


    public Long getLocationId() {
        return locationId;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public Long getExerciseId() {
        return exerciseId;
    }


    public Timestamp getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Timestamp startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Timestamp getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Timestamp endTimeStamp) {
        this.endTimestamp = endTimeStamp;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Float getAverageSpeed() {
        return averageSpeed;
    }

    public Float getStartAltitude() {
        return startAltitude;
    }

    public void setStartAltitude(Float startAltitude) {
        this.startAltitude = startAltitude;
    }

    public Float getEndAltitude() {
        return endAltitude;
    }

    public void setEndAltitude(Float endAltitude) {
        this.endAltitude = endAltitude;
    }

    public Float getAltitudeGained() {
        return altitudeGained;
    }

    public void setAltitudeGained(Float altitudeGained) {
        this.altitudeGained = altitudeGained;
    }

    public Float getAltitudeLost() {
        return altitudeLost;
    }

    public void setAltitudeLost(Float altitudeLost) {
        this.altitudeLost = altitudeLost;
    }

    public Float getStartLatitude() {
        return startLatitude;
    }


    public Float getStartLongitude() {
        return startLongitude;
    }



    public Float getEndLatitude() {
        return endLatitude;
    }


    public Float getEndLongitude() {
        return endLongitude;
    }



    public Integer getLogInterval() {
        return logInterval;
    }

    public void setLogInterval(Integer logInterval) {
        this.logInterval = logInterval;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public void setAverageSpeed(Float averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public void setStartLatitude(Float startLatitude) {
        this.startLatitude = startLatitude;
    }

    public void setStartLongitude(Float startLongitude) {
        this.startLongitude = startLongitude;
    }

    public void setEndLatitude(Float endLatitude) {
        this.endLatitude = endLatitude;
    }

    public void setEndLongitude(Float endLongitude) {
        this.endLongitude = endLongitude;
    }

    public Integer getLogDetail() {
        return logDetail;
    }

    public void setLogDetail(Integer logDetail) {
        this.logDetail = logDetail;
    }

    public Float getMaxSpeedToPoint() {
        return maxSpeedToPoint;
    }

    public void setMaxSpeedToPoint(Float maxSpeedToPoint) {
        this.maxSpeedToPoint = maxSpeedToPoint;
    }

    public Float getMinAltitude() {
        return minAltitude;
    }

    public void setMinAltitude(Float minAltitude) {
        this.minAltitude = minAltitude;
    }

    public Float getMaxAltitude() {
        return maxAltitude;
    }

    public void setMaxAltitude(Float maxAltitude) {
        this.maxAltitude = maxAltitude;
    }

    public Float getCurrentAltitude() {
        return currentAltitude;
    }

    public void setCurrentAltitude(Float currentAltitude) {
        this.currentAltitude = currentAltitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Integer getGmtHourOffset() {
        return gmtHourOffset;
    }

    public void setGmtHourOffset(Integer gmtHourOffset) {
        this.gmtHourOffset = gmtHourOffset;
    }

    public Integer getGmtMinuteOffset() {
        return gmtMinuteOffset;
    }

    public void setGmtMinuteOffset(Integer gmtMinuteOffset) {
        this.gmtMinuteOffset = gmtMinuteOffset;
    }

    public static final Parcelable.Creator<LocationExerciseRecord> CREATOR = new Parcelable.Creator<LocationExerciseRecord>() {

        public LocationExerciseRecord createFromParcel(Parcel src) {
            return new LocationExerciseRecord(src);
        }

        public LocationExerciseRecord[] newArray(int size) {
            return new LocationExerciseRecord[size];
        }

    };

    @Override
    public int compareTo(LocationExerciseRecord other) {
        return (int) ((this._id) - (other.get_id()));
    }

    private LocationExerciseRecord(Parcel src) {
        readFromParcel(src);
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (_id != null) {
            dest.writeInt(1);
            dest.writeLong(_id);
        } else {
            dest.writeInt(0);
        }

        if (locationId != null) {
            dest.writeInt(1);
            dest.writeLong(locationId);
        } else {
            dest.writeInt(0);
        }

        if (exerciseId != null) {
            dest.writeInt(1);
            dest.writeLong(exerciseId);
        } else {
            dest.writeInt(0);
        }

        if (description != null) {
            dest.writeInt(1);
            dest.writeString(description);
        } else {
            dest.writeInt(0);
        }

        if (startTimestamp != null) {
            dest.writeInt(1);
            dest.writeLong(startTimestamp.getTime());
        } else {
            dest.writeInt(0);
        }

        if (endTimestamp != null) {
            dest.writeInt(1);
            dest.writeLong(endTimestamp.getTime());
        } else {
            dest.writeInt(0);
        }

        if (distance != null) {
            dest.writeInt(1);
            dest.writeInt(distance);
        } else {
            dest.writeInt(0);
        }

        if (averageSpeed != null) {
            dest.writeInt(1);
            dest.writeFloat(averageSpeed);
        } else {
            dest.writeInt(0);
        }

        if (startAltitude != null) {
            dest.writeInt(1);
            dest.writeFloat(startAltitude);
        } else {
            dest.writeInt(0);
        }

        if (endAltitude != null) {
            dest.writeInt(1);
            dest.writeFloat(endAltitude);
        } else {
            dest.writeInt(0);
        }
        if (altitudeGained != null) {
            dest.writeInt(1);
            dest.writeFloat(altitudeGained);
        } else {
            dest.writeInt(0);
        }
        if (altitudeLost != null) {
            dest.writeInt(1);
            dest.writeFloat(altitudeLost);
        } else {
            dest.writeInt(0);
        }
        if (startLatitude != null) {
            dest.writeInt(1);
            dest.writeFloat(startLatitude);
        } else {
            dest.writeInt(0);
        }
        if (startLongitude != null) {
            dest.writeInt(1);
            dest.writeFloat(startLongitude);
        } else {
            dest.writeInt(0);
        }
        if (endLatitude != null) {
            dest.writeInt(1);
            dest.writeFloat(endLatitude);
        } else {
            dest.writeInt(0);
        }
        if (endLongitude != null) {
            dest.writeInt(1);
            dest.writeFloat(endLongitude);
        } else {
            dest.writeInt(0);
        }
        if (logInterval != null) {
            dest.writeInt(1);
            dest.writeInt(logInterval);
        } else {
            dest.writeInt(0);
        }
        if (logDetail != null) {
            dest.writeInt(1);
            dest.writeInt(logDetail);
        } else {
            dest.writeInt(0);
        }
        if (maxSpeedToPoint != null) {
            dest.writeInt(1);
            dest.writeFloat(maxSpeedToPoint);
        } else {
            dest.writeInt(0);
        }
        if (minAltitude != null) {
            dest.writeInt(1);
            dest.writeFloat(minAltitude);
        } else {
            dest.writeInt(0);
        }
        if (maxAltitude != null) {
            dest.writeInt(1);
            dest.writeFloat(maxAltitude);
        } else {
            dest.writeInt(0);
        }

        if (currentAltitude != null) {
            dest.writeInt(1);
            dest.writeFloat(currentAltitude);
        } else {
            dest.writeInt(0);
        }

        if (timezone != null){
            dest.writeInt(1);
            dest.writeString(timezone);
        } else {
            dest.writeInt(0);
        }

        if (gmtHourOffset != null){
            dest.writeInt(1);
            dest.writeInt(gmtHourOffset);
        } else {
            dest.writeInt(0);
        }

        if (gmtMinuteOffset != null){
            dest.writeInt(1);
            dest.writeInt(gmtMinuteOffset);
        } else {
            dest.writeInt(0);
        }


    }

    // make sure order of reads same as writes
    public void readFromParcel(Parcel src) {
        if (1 == src.readInt()) {
            _id = src.readLong();
        }

        if (1 == src.readInt()) {
            locationId = src.readLong();
        }

        if (1 == src.readInt()) {
            exerciseId = src.readLong();
        }

        if (1 == src.readInt()) {
            description = src.readString();
        }
        if (1 == src.readInt()) {
            startTimestamp = new Timestamp(src.readLong());
        }
        if (1 == src.readInt()) {
            endTimestamp = new Timestamp(src.readLong());
        }
        if (1 == src.readInt()) {
            distance = src.readInt();
        }
        if (1 == src.readInt()) {
            averageSpeed = src.readFloat();
        }
        if (1 == src.readInt()) {
            startAltitude = src.readFloat();
        }
        if (1 == src.readInt()) {
            endAltitude = src.readFloat();
        }
        if (1 == src.readInt()) {
            altitudeGained = src.readFloat();
        }
        if (1 == src.readInt()) {
            altitudeLost = src.readFloat();
        }
        if (1 == src.readInt()) {
            startLatitude = src.readFloat();
        }
        if (1 == src.readInt()) {
            startLongitude = src.readFloat();
        }
        if (1 == src.readInt()) {
            endLatitude = src.readFloat();
        }
        if (1 == src.readInt()) {
            endLongitude = src.readFloat();
        }
        if (1 == src.readInt()) {
            logInterval = src.readInt();
        }
        if (1 == src.readInt()) {
            logDetail = src.readInt();
        }
        if (1 == src.readInt()) {
            maxSpeedToPoint = src.readFloat();
        }
        if (1 == src.readInt()) {
            minAltitude = src.readFloat();
        }
        if (1 == src.readInt()) {
            maxAltitude = src.readFloat();
        }
        if (1 == src.readInt()) {
            currentAltitude = src.readFloat();
        }
        if (1 == src.readInt()){
            timezone = src.readString();
        }
        if (1 == src.readInt()){
            gmtHourOffset = src.readInt();
        } else {
            gmtHourOffset = 0;
        }

        if (1 == src.readInt()){
            gmtMinuteOffset = src.readInt();
        } else {
            gmtMinuteOffset = 0;
        }

    }

    public int describeContents() {
        // nothing special
        return 0;
    }

}
