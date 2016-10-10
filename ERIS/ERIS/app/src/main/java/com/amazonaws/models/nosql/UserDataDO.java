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
    private List<String> _location;
    private String _name;
    private List<String> _orgSubordinates;
    private String _orgSuperior;
    private String _orginization;

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
    @DynamoDBAttribute(attributeName = "location")
    public List<String> getLocation() {
        return _location;
    }

    public void setLocation(final List<String> _location) {
        this._location = _location;
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
    @DynamoDBIndexRangeKey(attributeName = "orgSuperior", globalSecondaryIndexName = "superiors")
    public String getOrgSuperior() {
        return _orgSuperior;
    }

    public void setOrgSuperior(final String _orgSuperior) {
        this._orgSuperior = _orgSuperior;
    }
    @DynamoDBIndexHashKey(attributeName = "orginization", globalSecondaryIndexName = "superiors")
    public String getOrginization() {
        return _orginization;
    }

    public void setOrginization(final String _orginization) {
        this._orginization = _orginization;
    }

}
