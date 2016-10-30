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
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.models.nosql.UserDataDO;

import com.eris.classes.Incident;
import com.eris.classes.Responder;
import com.eris.fragments.IncidentDatabaseFragment;

import java.util.Iterator;


//Uses the local service model.
public class DatabaseService extends Service {

    private static final String TAG = DatabaseService.class.getSimpleName();

    public static final String CALLING_METHOD_INFO = "calling_method_info";
    public static final String ERROR_STATUS = "error_status";
    public static final String DATA = "data";
    public static final String DATABASE_SERVICE_ACTION = "android.intent.action.database.service";

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
     * Find in the system database the responder with the given userID.
     *
     * @param userID The ID string for the user whose data should be retrieved
     * @param requestID A unique request ID to identify this call
     * @return A Responder object with the requested user's data as found
     * in the system database; null if no results were found for the given userID
     * You must wait for a broadcast to come back with the unique requestID
     * that you provided.  If the data in this is null, this method failed.
     */
    public void getResponderData(final String userID, final String requestID) {
        (new Thread(new GetResponderDataThread(userID, requestID))).start();
    }

    private class GetResponderDataThread implements Runnable {
        String callingMethodInfo;
        String userID;

        public GetResponderDataThread(String userID, String callingMethodInfo) {
            this.callingMethodInfo = callingMethodInfo;
            this.userID = userID;
        }

        public void run() {
            Intent intent = new Intent();
            //TODO this is wrong.  there needs to be one for thing calling/ID, and one for the
            //method called.
            intent.setAction(DATABASE_SERVICE_ACTION);
            intent.putExtra(CALLING_METHOD_INFO, callingMethodInfo);


            final UserDataDO targetUser = new UserDataDO();
            targetUser.setUserId(userID);

            final DynamoDBQueryExpression<UserDataDO> queryExpr = new DynamoDBQueryExpression<UserDataDO>()
                    .withHashKeyValues(targetUser)
                    .withConsistentRead(false)
                    .withLimit(1);

            final PaginatedQueryList<UserDataDO> resultList = mapper.query(UserDataDO.class, queryExpr);

            if (resultList.size() < 1) {
                intent.putExtra(ERROR_STATUS, Responder.RESPONDER_NOT_FOUND);
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


    public Responder[] getSubordinates(Responder superior) {
        return null;
    }

    public Responder[] getDepartmentResponders(Incident.Department dept) {
        PaginatedQueryList<UserDataDO> results;
        Iterator<UserDataDO> resultsIterator;
        final UserDataDO itemToFind = new UserDataDO();
        itemToFind.setOrganization(dept.getName());

        DynamoDBQueryExpression<UserDataDO> queryExpression = new DynamoDBQueryExpression<UserDataDO>()
                .withHashKeyValues(itemToFind)
                .withConsistentRead(false);
        results = mapper.query(UserDataDO.class, queryExpression);
        if (results != null) {
            resultsIterator = results.iterator();
            if (resultsIterator.hasNext()) {
                //return all the things in the iterator.
            }
        }

        return null;
    }

    public Responder[] getAllResponders() {
        //private PaginatedQueryList<UserDataDO> results;
        //private Iterator<UserDataDO> resultsIterator;
        //Don't
        return null;
    }

    public Incident[] getAllIncidents() {
        return null;
    }

    public Incident[] getDepartmentIncidents(Incident.Department dept) {
        return null;
    }

    public class DatabaseServiceBinder extends Binder {
        public DatabaseService getService() {
            return DatabaseService.this;
        }
    }
}
