package com.eris.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBSaveExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.models.nosql.ScenesDO;
import com.amazonaws.models.nosql.UserDataDO;

import com.eris.R;
import com.eris.classes.Incident;
import com.eris.classes.Responder;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;


//Uses the local service model.
public class DatabaseService extends Service {

    private static final String TAG = DatabaseService.class.getSimpleName();

    public static final String CALLING_METHOD_IDENTIFIER = "calling_method_identifier";
    public static final String ERROR_STATUS = "error_status";
    public static final String DATA = "data";
    public static final String DATABASE_SERVICE_ACTION = "android.intent.action.database.service";
//    public static final String BROADCAST_ACTION_DATABASE_INCIDENT_RESPONDERS = "broadcast_action_database_incident_responders";
//    public static final String BROADCAST_ACTION_DATABASE_INCIDENT_LIST = "broadcast_action_database_incident_list";

    /*
     * Final Members
     */
    private final IBinder databaseServiceBinder = new DatabaseServiceBinder();
    private final DynamoDBMapper mapper;  // Object mapper for accessing DynamoDB
    private LocalBroadcastManager localBroadcastManager;

    /*
     * Private Members
     */
    private Responder currentUser;
    private String savedCurrentUserIDToken;
    private BroadcastReceiver receiver;

    public CountDownLatch currentUserLatch;

    /**
     * Constructor for DatabaseService
     */
    public DatabaseService() {

        mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        //Get shared preferences and load current user.
        currentUser = null;

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
        currentUserLatch = new CountDownLatch(1);

        // Create an intent filter
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationService.BROADCAST_ACTION_LOCATION_UPDATE);

        // Create broadcast receiver object.
        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // Determine which broadcast was sent.
                switch ( intent.getAction() ) {

                    // Updated Location
                    case LocationService.BROADCAST_ACTION_LOCATION_UPDATE:
                        double latitude = intent.getDoubleExtra(LocationService.KEY_LOCATION_LATITUDE, 0.0);
                        double longitude = intent.getDoubleExtra(LocationService.KEY_LOCATION_LONGITUDE, 0.0);
                        updateCurrentUserLocation(latitude, longitude);
                        break;

                    // Unhandled broadcast.
                    default:
                        break;
                }
            }
        };
        this.registerReceiver(receiver, filter);

        SharedPreferences preferences = getSharedPreferences(
                getResources().getString(R.string.sharedpreferences_curr_user_account_info),
                Context.MODE_PRIVATE
        );
        savedCurrentUserIDToken = preferences.getString(
                getResources().getString(R.string.sharedpreferences_entry_userID), "");

        this.getResponderData(savedCurrentUserIDToken, "");
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
                    foundUser.getCurrentIncidentId(), foundUser.getIncidentSuperior(),
                    foundUser.getIncidentSubordinates());
            if (r.getUserID().equals(savedCurrentUserIDToken)) {
                currentUser = r;
                currentUserLatch.countDown();
                Log.d("currUser setting", "Current Logged-In User Set");
            }
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
                            foundUser.getCurrentIncidentId(), foundUser.getIncidentSuperior(),
                            foundUser.getIncidentSubordinates());
                    orgSubordinates[i] = subordinate;
                } else {
                    Log.e(TAG, "Failed to find responder " + subordinateId);
                    List<String> emptyList = new ArrayList<String>();
                    Responder subordinate = new Responder(subordinateId, "unknown", superior.getOrganization(),
                            emptyList, "unknown",
                            emptyList, "0.0", "0.0 ",
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
     * @param incidentId
     * @param callingMethodIdentifier
     *
     * Sends out a broadcast with the following fields:
     * DATA: an array of responders.  Need to be unparcled first.
     * ERROR_STATUS: a string with error codes.
     * CALLING_METHOD_IDENTIFIER: the string you passed in.
     */
    public void getRespondersByIncident(String incidentId, String callingMethodIdentifier) {
        if (incidentId == null) {
            throw new IllegalArgumentException("incidentId cannot be null");
        } else if (callingMethodIdentifier == null) {
            throw new IllegalArgumentException("callingMethodInfo cannot be null");
        }
        (new Thread(new GetRespondersByIncidentDataThread(incidentId, callingMethodIdentifier))).start();
    }

    private class GetRespondersByIncidentDataThread implements Runnable {
        String callingMethodIdentifier;
        String incidentId;

        public GetRespondersByIncidentDataThread(String incidentId, String callingMethodIdentifier) {
            this.callingMethodIdentifier = callingMethodIdentifier;
            this.incidentId = incidentId;
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
            itemToFind.setCurrentIncidentId(incidentId);

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
                                foundUser.getCurrentIncidentId(), foundUser.getIncidentSuperior(),
                                foundUser.getIncidentSubordinates());
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



    public void getOrgResponders(Incident.Department dept, String callingMethodIdentifier) {
        if (dept == null) {
            throw new IllegalArgumentException("dept cannot be null");
        } else if (callingMethodIdentifier == null) {
            throw new IllegalArgumentException("callingMethodInfo cannot be null");
        }
        (new Thread(new GetOrgRespondersDataThread(dept.getName(), callingMethodIdentifier))).start();
    }


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
                                foundUser.getCurrentIncidentId(), foundUser.getIncidentSuperior(),
                                foundUser.getIncidentSubordinates());
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
            intent.setAction(DATABASE_SERVICE_ACTION);
            intent.putExtra(CALLING_METHOD_IDENTIFIER, callingMethodIdentifier);
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
                                foundUser.getCurrentIncidentId(), foundUser.getIncidentSuperior(),
                                foundUser.getIncidentSubordinates());
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
        if (callingMethodIdentifier == null) {
            throw new IllegalArgumentException("callingMethodInfo cannot be null");
        }
        Log.d(TAG, "Started getting all incidents.");
        (new Thread(new GetAllIncidentsDataThread(callingMethodIdentifier))).start();
    }

    private class GetAllIncidentsDataThread implements Runnable {
        String callingMethodIdentifier;
        private PaginatedScanList<ScenesDO> results;
        private Iterator<ScenesDO> resultsIterator;
        ArrayList<Incident> incidents = new ArrayList<>();
        Incident incident;
        ScenesDO foundIncident;
        Intent intent = new Intent();

        public GetAllIncidentsDataThread(String callingMethodIdentifier) {
            this.callingMethodIdentifier = callingMethodIdentifier;
        }

        @Override
        public void run() {
            intent.setAction(DATABASE_SERVICE_ACTION);
            intent.putExtra(CALLING_METHOD_IDENTIFIER, callingMethodIdentifier);
            final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            results = mapper.scan(ScenesDO.class, scanExpression);
            if (results != null) {
                resultsIterator = results.iterator();
                if (resultsIterator.hasNext()) {
                    while(resultsIterator.hasNext()) {
                        foundIncident = resultsIterator.next();
                        //TODO add a thing to the scene.
                        incident = new Incident(foundIncident.getSceneId(), foundIncident.getDescription(),  foundIncident.getAddress(),
                                foundIncident.getLatitude(), foundIncident.getLongitude(),
                                foundIncident.getTime(), foundIncident.getTitle(), foundIncident.getAssignedOrganizations());
                        Log.d(TAG, "Assigned Orginizations: " + foundIncident.getAssignedOrganizations());
                        Log.d(TAG, foundIncident.toString());
                        incidents.add(incident);
                    }
                }
                //TODO should be seperate enum, or in this class, or in Incident.
                intent.putExtra(ERROR_STATUS, Responder.NO_ERROR);
                intent.putExtra(DATA, incidents.toArray(new Incident[incidents.size()]));
            } else {
                intent.putExtra(ERROR_STATUS, Responder.QUERY_FAILED);
            }
            sendBroadcast(intent);
        }
    }


    /**
     * This method will ignore the null fields, if any.
     * @param responder The Responder object
     */
    public void pushUpdatedResponderData(Responder responder) {
        if (responder == null) {
            throw new IllegalArgumentException("responder cannot be null");
        }
        (new Thread(new PushUpdatedResponderDataThread(responder))).start();
    }

    private class PushUpdatedResponderDataThread implements Runnable{
        Responder responder;

        public PushUpdatedResponderDataThread(Responder responder) {
            this.responder = responder;
        }

        @Override
        public void run() {
            final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
            final UserDataDO userData = new UserDataDO();

            if (responder.getUserID() == null) {
                throw new IllegalArgumentException("userId cannot be null");
            }

            userData.setUserId(responder.getUserID());
            userData.setCurrentIncidentId(responder.getSceneID());
            userData.setOrganization(responder.getOrganization());
            userData.setLongitude(responder.getLongitude());
            userData.setLatitude(responder.getLatitude());
            userData.setHeartbeatRecord(responder.getHeartrateRecord());
            userData.setIncidentSubordinates(responder.getIncidentSubordinates());
            userData.setIncidentSuperior(responder.getIncidentSuperior());
            userData.setName(responder.getName());
            userData.setOrgSubordinates(responder.getOrgSubordinates());
            userData.setOrgSuperior(responder.getOrgSuperior());

            try {
                mapper.save(userData, new DynamoDBMapperConfig(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES));
            } catch (final AmazonClientException e) {
                Log.e(TAG, "Failed saving item " + e.getMessage(), e);
            } catch (Exception e) {
                Log.e(TAG, "Other error: " + e.getMessage() + ", class: " + e.getClass());
                throw e;
            }
        }
    }

    public void pushNewResponderData(Responder responder) {
            if (responder == null) {
                throw new IllegalArgumentException("responder cannot be null");
            }
            (new Thread(new PushNewResponderDataThread(responder))).start();
        }

        private class PushNewResponderDataThread implements Runnable {
            Responder responder;

            public PushNewResponderDataThread(Responder responder) {
                this.responder = responder;
            }

            @Override
            public void run() {
                final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
                final UserDataDO userData = new UserDataDO();

                if (responder.getUserID() == null) {
                    throw new IllegalArgumentException("userId cannot be null");
                }
                if (responder.getSceneID() == null) {
                    throw new IllegalArgumentException("sceneId cannot be null");
                }
                if (responder.getOrganization() == null) {
                    throw new IllegalArgumentException("organization cannot be null");
                }
                if (responder.getLatitude() == null) {
                    throw new IllegalArgumentException("latitude cannot be null");
                }
                if (responder.getLongitude() == null) {
                    throw new IllegalArgumentException("longitude cannot be null");
                }
                if (responder.getHeartrateRecord() == null) {
                    throw new IllegalArgumentException("heartbeatRecord cannot be null");
                }
                if (responder.getIncidentSubordinates() == null) {
                    throw new IllegalArgumentException("incidentSubordinates cannot be null");
                }
                if (responder.getIncidentSuperior() == null) {
                    throw new IllegalArgumentException("userId cannot be null");
                }
                if (responder.getName() == null) {
                    throw new IllegalArgumentException("name cannot be null");
                }
                if (responder.getOrgSubordinates() == null) {
                    throw new IllegalArgumentException("orgSubordinates cannot be null");
                }
                if (responder.getOrgSuperior() == null) {
                    throw new IllegalArgumentException("orgSuperior cannot be null");
                }

                userData.setUserId(responder.getUserID());
                userData.setCurrentIncidentId(responder.getSceneID());
                userData.setOrganization(responder.getOrganization());
                userData.setLongitude(responder.getLongitude());
                userData.setLatitude(responder.getLatitude());
                userData.setHeartbeatRecord(responder.getHeartrateRecord());
                userData.setIncidentSubordinates(responder.getIncidentSubordinates());
                userData.setIncidentSuperior(responder.getIncidentSuperior());
                userData.setName(responder.getName());
                userData.setOrgSubordinates(responder.getOrgSubordinates());
                userData.setOrgSuperior(responder.getOrgSuperior());

                try {
                    mapper.save(userData);
                } catch (final AmazonClientException e) {
                    Log.e(TAG, "Failed saving item " + e.getMessage(), e);
                }
            }
        }


    public void pushUpdatedIncidentData(Incident incident) {
        if (incident == null) {
            throw new IllegalArgumentException("incident cannot be null");
        }
        (new Thread(new PushUpdatedIncidentDataThread(incident))).start();
    }

    public class PushUpdatedIncidentDataThread implements Runnable {
        Incident incident;

        public PushUpdatedIncidentDataThread(Incident incident) {
            this.incident = incident;
        }

        @Override
        public void run() {
            final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
            final ScenesDO incidentData = new ScenesDO();

            if (incident.getSceneId() == null) {
                throw new IllegalArgumentException("sceneId cannot be null");
            }

            incidentData.setSceneId(incident.getSceneId());
            incidentData.setAddress(incident.getAddress());
            incidentData.setLongitude(incident.getLongitude());
            incidentData.setLatitude(incident.getLatitude());
            incidentData.setAssignedOrganizations(incident.getOrganizations());
            incidentData.setDescription(incident.getDescription());
            incidentData.setTime(incident.getTime());
            incidentData.setTitle(incident.getTitle());

            try {
                mapper.save(incidentData, new DynamoDBMapperConfig(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES));
            } catch (final AmazonClientException e) {
                Log.e(TAG, "Failed saving item " + e.getMessage(), e);
            }
        }
    }

    /**
     * precondition: This item should not be in the database.
     * @param incident The incident to add to the incidents database.
     */
    public void pushNewIncidentData(Incident incident) {
        if (incident == null) {
            throw new IllegalArgumentException("incident cannot be null");
        }
        (new Thread(new PushNewIncidentDataThread(incident))).start();
    }

    public class PushNewIncidentDataThread implements Runnable {
        Incident incident;

        public PushNewIncidentDataThread(Incident incident) {
            this.incident = incident;
        }

        @Override
        public void run() {
            final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
            final ScenesDO incidentData = new ScenesDO();

            if (incident.getSceneId() == null) {
                throw new IllegalArgumentException("sceneId cannot be null");
            }
            if (incident.getAddress() == null) {
                throw new IllegalArgumentException("address cannot be null");
            }
            if (incident.getLatitude() == null) {
                throw new IllegalArgumentException("latitude cannot be null");
            }
            if (incident.getDescription() == null) {
                throw new IllegalArgumentException("description cannot be null");
            }
            if (incident.getLongitude() == null) {
                throw new IllegalArgumentException("longitude cannot be null");
            }
            if (incident.getOrganizations() == null) {
                throw new IllegalArgumentException("organizations cannot be null");
            }
            if (incident.getTime() == null) {
                throw new IllegalArgumentException("time cannot be null");
            }
            if (incident.getTitle() == null) {
                throw new IllegalArgumentException("title cannot be null");
            }

            incidentData.setSceneId(incident.getSceneId());
            incidentData.setAddress(incident.getAddress());
            incidentData.setLongitude(incident.getLongitude());
            incidentData.setLatitude(incident.getLatitude());
            incidentData.setAssignedOrganizations(incident.getOrganizations());
            incidentData.setDescription(incident.getDescription());
            incidentData.setTime(incident.getTime());
            incidentData.setTitle(incident.getTitle());

            try {
                mapper.save(incidentData);
            } catch (final AmazonClientException e) {
                Log.e(TAG, "Failed saving item " + e.getMessage(), e);
            }
        }
    }

    //public void getDepartmentIncidents(Incident.Department dept) {}

    public class DatabaseServiceBinder extends Binder {
        public DatabaseService getService() {
            return DatabaseService.this;
        }
    }

    /**
     * Get a reference to the Responder object representing the current logged in user.
     * You should expect this could be null if you call it too soon.
     * Really you should call it from a thread and wait for the countdownlatch to open.
     */
    public Responder getCurrentUser() {
        return currentUser;
    }

    /**
     * Ask the database service to update the current user object's location
     * and send a request to also update this data in the database.
     */
    private boolean updateCurrentUserLocation(double latitude, double longitude) {
        // Return false if the currentUser object has not yet been retrieved from the server
        if (currentUser == null) {
            return false;
            //Should this throw an error?
        }

        currentUser.setLocation(new LatLng(latitude, longitude));
        pushUpdatedResponderData(currentUser);
        return true;
    }

    /**
     * Ask the database service to update the current user object's superior and/or subordinates,
     * and send a request to also update this data in the database.
     */
//    private boolean updateCurrentUserSuperiorAndSubordinates(String superiorID, List<String> subordinateIDs) {
//        // Return false if the currentUser object has not yet been retrieved from the server
//        if (currentUser == null) {
//            return false;
//        }
//
//        if (superiorID != null) {
//            currentUser.setIncidentSuperior(superiorID);
//        }
//        if (subordinateIDs != null) {
//            currentUser.setIncidentSubordinates(subordinateIDs);
//        }
//        pushResponderData(currentUser);
//        return true;
//    }
}
