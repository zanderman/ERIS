package com.amazonaws.models.nosql;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "emergencyresponderin-mobilehub-146580548-User_Data")

public class UserDataDO {
    private String _userId;
    private String _currentIncidentId;
    private List<String> _heartbeatRecord;
    private List<String> _incidentSubordinates;
    private String _incidentSuperior;
    private String _latitude;
    private String _longitude;
    private String _name;
    private List<String> _orgSubordinates;
    private String _orgSuperior;
    private String _organization;

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }
    @DynamoDBAttribute(attributeName = "currentIncidentId")
    public String getCurrentIncidentId() {
        return _currentIncidentId;
    }

    public void setCurrentIncidentId(final String _currentIncidentId) {
        this._currentIncidentId = _currentIncidentId;
    }
    @DynamoDBAttribute(attributeName = "heartbeatRecord")
    public List<String> getHeartbeatRecord() {
        return _heartbeatRecord;
    }

    public void setHeartbeatRecord(final List<String> _heartbeatRecord) {
        this._heartbeatRecord = _heartbeatRecord;
    }
    @DynamoDBAttribute(attributeName = "incidentSubordinates")
    public List<String> getIncidentSubordinates() {
        return _incidentSubordinates;
    }

    public void setIncidentSubordinates(final List<String> _incidentSubordinates) {
        this._incidentSubordinates = _incidentSubordinates;
    }
    @DynamoDBAttribute(attributeName = "incidentSuperior")
    public String getIncidentSuperior() {
        return _incidentSuperior;
    }

    public void setIncidentSuperior(final String _incidentSuperior) {
        this._incidentSuperior = _incidentSuperior;
    }
    @DynamoDBAttribute(attributeName = "latitude")
    public String getLatitude() {
        return _latitude;
    }

    public void setLatitude(final String _latitude) {
        this._latitude = _latitude;
    }
    @DynamoDBAttribute(attributeName = "longitude")
    public String getLongitude() {
        return _longitude;
    }

    public void setLongitude(final String _longitude) {
        this._longitude = _longitude;
    }
    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return _name;
    }

    public void setName(final String _name) {
        this._name = _name;
    }
    @DynamoDBAttribute(attributeName = "orgSubordinates")
    public List<String> getOrgSubordinates() {
        return _orgSubordinates;
    }

    public void setOrgSubordinates(final List<String> _orgSubordinates) {
        this._orgSubordinates = _orgSubordinates;
    }
    @DynamoDBAttribute(attributeName = "orgSuperior")
    public String getOrgSuperior() {
        return _orgSuperior;
    }

    public void setOrgSuperior(final String _orgSuperior) {
        this._orgSuperior = _orgSuperior;
    }
    @DynamoDBAttribute(attributeName = "organization")
    public String getOrganization() {
        return _organization;
    }

    public void setOrganization(final String _organization) {
        this._organization = _organization;
    }

}
