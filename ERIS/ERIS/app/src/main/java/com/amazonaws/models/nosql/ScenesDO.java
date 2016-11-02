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
    private List<String> _assignedOrginizations;
    private String _description;
    private String _latitude;
    private String _longitude;

    @DynamoDBHashKey(attributeName = "sceneId")
    @DynamoDBAttribute(attributeName = "sceneId")
    public String getSceneId() {
        return _sceneId;
    }

    public void setSceneId(final String _sceneId) {
        this._sceneId = _sceneId;
    }
    @DynamoDBAttribute(attributeName = "assigned_orginizations")
    public List<String> getAssignedOrginizations() {
        return _assignedOrginizations;
    }

    public void setAssignedOrginizations(final List<String> _assignedOrginizations) {
        this._assignedOrginizations = _assignedOrginizations;
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

}
