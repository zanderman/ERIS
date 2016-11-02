package com.eris.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.models.nosql.UserDataDO;

import com.eris.classes.Incident;
import com.eris.classes.Responder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


//Uses the local service model.
public class DatabaseService extends Service {

    private static final String TAG = DatabaseService.class.getSimpleName();

    public static final String CALLING_METHOD_IDENTIFIER = "calling_method_identifier";
    public static final String ERROR_STATUS = "error_status";
    public static final String DATA = "data";
    public static final String DATABASE_SERVICE_ACTION = "android.intent.action.database.service";
    public static final String BROADCAST_ACTION_DATABASE_INCIDENT_RESPONDERS = "broadcast_action_database_incident_responders";

    /*
     * Final Members
     */
    private final IBinder databaseServiceBinder = new DatabaseServiceBinder();
    private final DynamoDBMapper mapper;  // Object mapper for accessing DynamoDB
    private LocalBroadcastManager localBroadcastManager;

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
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        //TODO does this work?
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return databaseServiceBinder;
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
     * Find in the system database the responder with the given userId.
     *
     * @param userId The ID string for the user whose data should be retrieved
     * @param callingMethodIdentifier A unique request ID to identify this call
     * @return A Responder object with the requested user's data as found
     * in the system database; null if no results were found for the given userId
     * You must wait for a broadcast to come back with the unique callingMethodIdentifier
     * that you provided.  If the data in this is null, this method failed.
     *
     * The extras returned are in the form:
     * Responder DATA,
     * String ERROR_STATUS
     * String CALLING_METHOD_IDENTIFIER;
     */
    public void getResponderData(final String userId, final String callingMethodIdentifier) {
        if (userId == null) {
            throw new IllegalArgumentException("superior cannot be null");
        } else if (callingMethodIdentifier == null) {
            throw new IllegalArgumentException("callingMethodInfo cannot be null");
        }
        (new Thread(new GetResponderDataThread(userId, callingMethodIdentifier))).start();
    }

    private class GetResponderDataThread implements Runnable {
        String callingMethodIdentifier;
        String userId;

        public GetResponderDataThread(String userId, String callingMethodIdentifier) {
            this.callingMethodIdentifier = callingMethodIdentifier;
            this.userId = userId;
        }

        public void run() {
            Intent intent = new Intent();
            intent.setAction(DATABASE_SERVICE_ACTION);
            intent.putExtra(CALLING_METHOD_IDENTIFIER, callingMethodIdentifier);


            final UserDataDO targetUser = new UserDataDO();
            targetUser.setUserId(userId);

            final DynamoDBQueryExpression<UserDataDO> queryExpr = new DynamoDBQueryExpression<UserDataDO>()
                    .withHashKeyValues(targetUser)
                    .withConsistentRead(false)
                    .withLimit(1);

            final PaginatedQueryList<UserDataDO> resultList = mapper.query(UserDataDO.class, queryExpr);

            if (resultList.size() < 1) {
                intent.putExtra(ERROR_STATUS, Responder.QUERY_FAILED);
                sendBroadcast(intent);
                Log.d(TAG, "Sent broadcast for responder not found.");
                return;
            }

            UserDataDO foundUser = resultList.get(0);
            //No just broadcast this with the rID.
            intent.putExtra(ERROR_STATUS, Responder.NO_ERROR);
            Responder r = new Responder(foundUser.getUserId(), foundUser.getName(),  foundUser.getOrganization(),
                    foundUser.getHeartbeatRecord(), foundUser.getOrgSuperior(),
                    foundUser.getOrgSubordinates(), foundUser.getLatitude(), foundUser.getLongitude(),
                    foundUser.getCurrentIncidentId(), foundUser.getOrgSuperior(),
                    foundUser.getOrgSubordinates());
            intent.putExtra(DATA, r);
            sendBroadcast(intent);
            Log.d(TAG, "Sent broadcast for responder found.");
        }
    }

    //TODO if people want to call this by ID, might put that in here also.
    public void getOrgSubordinates(Responder superior, String callingMethodIdentifier) {
        if (superior == null) {
            throw new IllegalArgumentException("superior cannot be null");
        } else if (callingMethodIdentifier == null) {
            throw new IllegalArgumentException("callingMethodInfo cannot be null");
        }
        (new Thread(new GetOrgSubordinatesDataThread(superior, callingMethodIdentifier))).start();
    }

    private class GetOrgSubordinatesDataThread implements Runnable {
        String callingMethodIdentifier;
        Responder superior;

        public GetOrgSubordinatesDataThread(Responder superior, String callingMethodIdentifier) {
            this.callingMethodIdentifier = callingMethodIdentifier;
            this.superior = superior;
        }

        @Override
        public void run() {
            Responder orgSubordinates[] = new Responder[superior.getOrgSubordinates().size()];
            Intent intent = new Intent();
            intent.setAction(DATABASE_SERVICE_ACTION);
            intent.putExtra(CALLING_METHOD_IDENTIFIER, callingMethodIdentifier);
            //There may be errors, but I don't think this should say so.
            intent.putExtra(ERROR_STATUS, Responder.NO_ERROR);
            int i = 0;

            for (String subordinateId : superior.getOrgSubordinates()) {
                //TODO this gets remade a bunch, will this be a problem?
                final UserDataDO targetUser = new UserDataDO();
                targetUser.setUserId(subordinateId);
                final DynamoDBQueryExpression<UserDataDO> queryExpr = new DynamoDBQueryExpression<UserDataDO>()
                        .withHashKeyValues(targetUser)
                        .withConsistentRead(false)
                        .withLimit(1);
                final PaginatedQueryList<UserDataDO> resultList = mapper.query(UserDataDO.class, queryExpr);

                if (resultList.size() > 0) {
                    UserDataDO foundUser = resultList.get(0);
                    Responder subordinate = new Responder(foundUser.getUserId(), foundUser.getName(),  foundUser.getOrganization(),
                            foundUser.getHeartbeatRecord(), foundUser.getOrgSuperior(),
                            foundUser.getOrgSubordinates(), foundUser.getLatitude(), foundUser.getLongitude(),
                            foundUser.getCurrentIncidentId(), foundUser.getOrgSuperior(),
                            foundUser.getOrgSubordinates());
                    orgSubordinates[i] = subordinate;
                } else {
                    Log.e(TAG, "Failed to find responder " + subordinateId);
                    List<String> emptyList = new ArrayList<String>();
                    Responder subordinate = new Responder(subordinateId, "unknown", superior.getOrganization(),
                            emptyList, "unknown",
                            emptyList, "unknown", "unknown",
                            "unknown", superior.getUserID(),
                            emptyList);
                    orgSubordinates[i] = subordinate;
                }
                i++;
            }
            intent.putExtra(DATA, orgSubordinates);
            sendBroadcast(intent);
        }
    }

    /**
     *
     * @param sceneId
     * @param callingMethodIdentifier
     *
     * Sends out a broadcast with the following fields:
     * DATA: an array of responders.  Need to be unparcled first.
     * ERROR_STATUS: a string with error codes.
     * CALLING_METHOD_IDENTIFIER: the string you passed in.
     */
    public void getRespondersByIncident(String sceneId, String callingMethodIdentifier) {
        if (sceneId == null) {
            throw new IllegalArgumentException("sceneId cannot be null");
        } else if (callingMethodIdentifier == null) {
            throw new IllegalArgumentException("callingMethodInfo cannot be null");
        }
        //(new Thread(new GetOrgRespondersDataThread(dept.getName(), callingMethodIdentifier))).start();
    }

    private class GetRespondersByIncidentDataThread implements Runnable {
        String callingMethodIdentifier;
        String sceneId;

        public GetRespondersByIncidentDataThread(String sceneId, String callingMethodIdentifier) {
            this.callingMethodIdentifier = callingMethodIdentifier;
            this.sceneId = sceneId;
        }

        @Override
        public void run() {
            //Return an array in DATA.
        }
    }


    /*
    We don't hash for this key.  We need to do that before this method works.
    public void getOrgResponders(Incident.Department dept, String callingMethodIdentifier) {
        if (dept == null) {
            throw new IllegalArgumentException("dept cannot be null");
        } else if (callingMethodIdentifier == null) {
            throw new IllegalArgumentException("callingMethodInfo cannot be null");
        }
        (new Thread(new GetOrgRespondersDataThread(dept.getName(), callingMethodIdentifier))).start();
    }
    */

    private class GetOrgRespondersDataThread implements Runnable {
        String callingMethodIdentifier;
        String department;

        public GetOrgRespondersDataThread(String department, String callingMethodIdentifier) {
            this.callingMethodIdentifier = callingMethodIdentifier;
            this.department = department;
        }

        @Override
        public void run() {
            ArrayList<Responder> responders = new ArrayList<>();
            Responder responder;
            UserDataDO foundUser;
            Intent intent = new Intent();
            intent.setAction(DATABASE_SERVICE_ACTION);
            intent.putExtra(CALLING_METHOD_IDENTIFIER, callingMethodIdentifier);

            PaginatedQueryList<UserDataDO> results;
            Iterator<UserDataDO> resultsIterator;
            final UserDataDO itemToFind = new UserDataDO();
            itemToFind.setOrganization(department);

            DynamoDBQueryExpression<UserDataDO> queryExpression = new DynamoDBQueryExpression<UserDataDO>()
                    .withHashKeyValues(itemToFind)
                    .withConsistentRead(false);
            results = mapper.query(UserDataDO.class, queryExpression);
            if (results != null) {
                resultsIterator = results.iterator();
                if (resultsIterator.hasNext()) {
                    while(resultsIterator.hasNext()) {
                        foundUser = resultsIterator.next();
                        responder = new Responder(foundUser.getUserId(), foundUser.getName(),  foundUser.getOrganization(),
                                foundUser.getHeartbeatRecord(), foundUser.getOrgSuperior(),
                                foundUser.getOrgSubordinates(), foundUser.getLatitude(), foundUser.getLongitude(),
                                foundUser.getCurrentIncidentId(), foundUser.getOrgSuperior(),
                                foundUser.getOrgSubordinates());
                        responders.add(responder);
                    }
                }
                intent.putExtra(ERROR_STATUS, Responder.NO_ERROR);
                intent.putExtra(DATA, responders.toArray(new Responder[responders.size()]));
            } else {
                //TODO this should be a different error, like no_things_found.
                intent.putExtra(ERROR_STATUS, Responder.QUERY_FAILED);
            }
            sendBroadcast(intent);
        }
    }



    public void getAllResponders(String callingMethodIdentifier) {
        if (callingMethodIdentifier == null) {
            throw new IllegalArgumentException("callingMethodInfo cannot be null");
        }
        (new Thread(new GetAllRespondersDataThread(callingMethodIdentifier))).start();
    }

    private class GetAllRespondersDataThread implements Runnable {
        String callingMethodIdentifier;
        private PaginatedScanList<UserDataDO> results;
        private Iterator<UserDataDO> resultsIterator;
        ArrayList<Responder> responders = new ArrayList<Responder>();
        Responder responder;
        UserDataDO foundUser;
        Intent intent = new Intent();

        public GetAllRespondersDataThread(String callingMethodIdentifier) {
            this.callingMethodIdentifier = callingMethodIdentifier;
        }

        @Override
        public void run() {
            final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            results = mapper.scan(UserDataDO.class, scanExpression);
            if (results != null) {
                resultsIterator = results.iterator();
                if (resultsIterator.hasNext()) {
                    while(resultsIterator.hasNext()) {
                        foundUser = resultsIterator.next();
                        responder = new Responder(foundUser.getUserId(), foundUser.getName(),  foundUser.getOrganization(),
                                foundUser.getHeartbeatRecord(), foundUser.getOrgSuperior(),
                                foundUser.getOrgSubordinates(), foundUser.getLatitude(), foundUser.getLongitude(),
                                foundUser.getCurrentIncidentId(), foundUser.getOrgSuperior(),
                                foundUser.getOrgSubordinates());
                        responders.add(responder);
                    }
                }
                intent.putExtra(ERROR_STATUS, Responder.NO_ERROR);
                intent.putExtra(DATA, responders.toArray(new Responder[responders.size()]));
            } else {
                intent.putExtra(ERROR_STATUS, Responder.QUERY_FAILED);
            }
            sendBroadcast(intent);
        }
    }

    /**
     *
     * @param callingMethodIdentifier
     *
     * Sends out a broadcast with the following fields:
     * DATA: an array of incidents.  Need to be unparcled first.
     * ERROR_STATUS: a string with error codes.
     * CALLING_METHOD_IDENTIFIER: the string you passed in.
     */
    public void getAllIncidents(String callingMethodIdentifier) {

    }

    public void pushResponderData(Responder responder) {

    }

    //public void pushIncidentData(Incident incident) {}

    //public void getDepartmentIncidents(Incident.Department dept) {}

    public class DatabaseServiceBinder extends Binder {
        public DatabaseService getService() {
            return DatabaseService.this;
        }
    }
}
