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

@DynamoDBTable(tableName = "emergencyresponderin-mobilehub-146580548-Scenes")

public class ScenesDO {
    private String _sceneId;
    private String _address;
    private List<String> _assignedOrganizations;
    private String _description;
    private String _latitude;
    private String _longitude;
    private String _time;
    private String _title;

    @DynamoDBHashKey(attributeName = "sceneId")
    @DynamoDBAttribute(attributeName = "sceneId")
    public String getSceneId() {
        return _sceneId;
    }

    public void setSceneId(final String _sceneId) {
        this._sceneId = _sceneId;
    }
    @DynamoDBAttribute(attributeName = "address")
    public String getAddress() {
        return _address;
    }

    public void setAddress(final String _address) {
        this._address = _address;
    }
    @DynamoDBAttribute(attributeName = "assigned_organizations")
    public List<String> getAssignedOrganizations() {
        return _assignedOrganizations;
    }

    public void setAssignedOrganizations(final List<String> _assignedOrganizations) {
        this._assignedOrganizations = _assignedOrganizations;
    }
    @DynamoDBAttribute(attributeName = "description")
    public String getDescription() {
        return _description;
    }

    public void setDescription(final String _description) {
        this._description = _description;
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
    @DynamoDBAttribute(attributeName = "time")
    public String getTime() {
        return _time;
    }

    public void setTime(final String _time) {
        this._time = _time;
    }
    @DynamoDBAttribute(attributeName = "title")
    public String getTitle() {
        return _title;
    }

    public void setTitle(final String _title) {
        this._title = _title;
    }

}
