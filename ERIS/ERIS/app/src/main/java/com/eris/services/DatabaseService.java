package com.eris.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.models.nosql.UserDataDO;

import com.eris.classes.Responder;
import com.google.android.gms.maps.model.LatLng;

public class DatabaseService extends Service {

    /*
     * Final Members
     */
    private final DynamoDBMapper mapper;  // Object mapper for accessing DynamoDB

    /*
     * Private Members
     */

    /**
     * Constructor for DatabaseService
     */
    public DatabaseService() {

        mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();

        Log.d("service", "DatabaseService created");
    }

    /**
     *******************************************************************************
     * Database service methods
     *******************************************************************************
     */

    @Override
    public void onCreate() {
        super.onCreate();
        // TODO 44444444 look into creating a separate thread to do all network communications
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null; // No need to bind with this service yet.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("service", "Database Service STARTED!");

        // If the OS runs out of memory, START_STICKY tells the OS to start this service back up
        // again once enough memory has been freed.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Find in the system database the responder with the given userID.
     *
     * @param userID The ID string for the user whose data should be retrieved
     * @return A Responder object with the requested user's data as found
     * in the system database; null if no results were found for the given userID
     */
    public Responder getResponderData(String userID) {
        final UserDataDO targetUser = new UserDataDO();
        targetUser.setUserId(userID);

        final DynamoDBQueryExpression<UserDataDO> queryExpr = new DynamoDBQueryExpression<UserDataDO>()
                .withHashKeyValues(targetUser)
                .withConsistentRead(false)
                .withLimit(1);

        final PaginatedQueryList<UserDataDO> resultList = mapper.query(UserDataDO.class, queryExpr);
        UserDataDO foundUser = resultList.get(0);

        if (foundUser == null) {  // 44444444 needed?
            return null;
        }

        return new Responder(
                foundUser.getUserId(),
                foundUser.getName(), // 44444444 need to fix this, split name
                foundUser.getName(), // 44444444 need to fix this, split name
                foundUser.getCurrentIncidentId(),
                Float.parseFloat(foundUser.getHeartbeatRecord().get(0)),
                new LatLng(Double.parseDouble(foundUser.getLatitude()),
                        Double.parseDouble(foundUser.getLongitude())),
                "77"
        );
    }
}
