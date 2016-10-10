package com.eris.demo.nosql;

import android.content.Context;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.util.ThreadUtils;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.eris.R;
import com.amazonaws.models.nosql.UserDataDO;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DemoNoSQLTableUserData extends DemoNoSQLTableBase {
    private static final String LOG_TAG = DemoNoSQLTableUserData.class.getSimpleName();

    /** Inner classes use this value to determine how many results to retrieve per service call. */
    private static final int RESULTS_PER_RESULT_GROUP = 40;

    /** Removing sample data removes the items in batches of the following size. */
    private static final int MAX_BATCH_SIZE_FOR_DELETE = 50;

    /********* Primary Get Query Inner Classes *********/

    public class DemoGetWithPartitionKey extends DemoNoSQLOperationBase {
        private UserDataDO result;
        private boolean resultRetrieved = true;

        private DemoGetWithPartitionKey(final Context context) {
            super(context.getString(R.string.nosql_operation_get_by_partition_text),
                String.format(context.getString(R.string.nosql_operation_example_get_by_partition_text),
                    "userId", AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID()));
        }

        /* Blocks until result is retrieved, should be called in the background. */
        @Override
        public boolean executeOperation() throws AmazonClientException {
            // Retrieve an item by passing the partition key using the object mapper.
            result = mapper.load(UserDataDO.class, AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID());

            if (result != null) {
                resultRetrieved = false;
                return true;
            }
            return false;
        }

        @Override
        public List<DemoNoSQLResult> getNextResultGroup() {
            if (resultRetrieved) {
                return null;
            }
            final List<DemoNoSQLResult> results = new ArrayList<>();
            results.add(new DemoNoSQLUserDataResult(result));
            resultRetrieved = true;
            return results;
        }

        @Override
        public void resetResults() {
            resultRetrieved = false;
        }

    }

    /* ******** Secondary Named Index Query Inner Classes ******** */



    public class DemoSuperiorsQueryWithPartitionKeyAndSortKeyCondition extends DemoNoSQLOperationBase {

        private PaginatedQueryList<UserDataDO> results;
        private Iterator<UserDataDO> resultsIterator;
        DemoSuperiorsQueryWithPartitionKeyAndSortKeyCondition (final Context context) {
            super(
                context.getString(R.string.nosql_operation_title_index_query_by_partition_and_sort_condition_text),
                context.getString(R.string.nosql_operation_example_index_query_by_partition_and_sort_condition_text,
                    "orginization", "demo-orginization-3",
                    "orgSuperior", "demo-orgSuperior-500000"));
        }

        public boolean executeOperation() {
            // Perform a query using a partition key and sort key condition.
            final UserDataDO itemToFind = new UserDataDO();
            itemToFind.setOrginization("demo-orginization-3");
            final Condition sortKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.LT.toString())

                .withAttributeValueList(new AttributeValue().withS("demo-orgSuperior-500000"));
            // Perform get using Partition key and sort key condition
            DynamoDBQueryExpression<UserDataDO> queryExpression = new DynamoDBQueryExpression<UserDataDO>()
                .withHashKeyValues(itemToFind)
                .withRangeKeyCondition("orgSuperior", sortKeyCondition)
                .withConsistentRead(false);
            results = mapper.query(UserDataDO.class, queryExpression);
            if (results != null) {
                resultsIterator = results.iterator();
                if (resultsIterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<DemoNoSQLResult> getNextResultGroup() {
            return getNextResultsGroupFromIterator(resultsIterator);
        }

        @Override
        public void resetResults() {
            resultsIterator = results.iterator();
        }
    }

    public class DemoSuperiorsQueryWithPartitionKeyOnly extends DemoNoSQLOperationBase {

        private PaginatedQueryList<UserDataDO> results;
        private Iterator<UserDataDO> resultsIterator;

        DemoSuperiorsQueryWithPartitionKeyOnly(final Context context) {
            super(
                context.getString(R.string.nosql_operation_title_index_query_by_partition_text),
                context.getString(R.string.nosql_operation_example_index_query_by_partition_text,
                    "orginization", "demo-orginization-3"));
        }

        public boolean executeOperation() {
            // Perform a query using a partition key and filter condition.
            final UserDataDO itemToFind = new UserDataDO();
            itemToFind.setOrginization("demo-orginization-3");

            // Perform get using Partition key
            DynamoDBQueryExpression<UserDataDO> queryExpression = new DynamoDBQueryExpression<UserDataDO>()
                .withHashKeyValues(itemToFind)
                .withConsistentRead(false);
            results = mapper.query(UserDataDO.class, queryExpression);
            if (results != null) {
                resultsIterator = results.iterator();
                if (resultsIterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<DemoNoSQLResult> getNextResultGroup() {
            return getNextResultsGroupFromIterator(resultsIterator);
        }

        @Override
        public void resetResults() {
            resultsIterator = results.iterator();
        }
    }

    public class DemoSuperiorsQueryWithPartitionKeyAndFilterCondition extends DemoNoSQLOperationBase {

        private PaginatedQueryList<UserDataDO> results;
        private Iterator<UserDataDO> resultsIterator;

        DemoSuperiorsQueryWithPartitionKeyAndFilterCondition (final Context context) {
            super(
                context.getString(R.string.nosql_operation_title_index_query_by_partition_and_filter_text),
                context.getString(R.string.nosql_operation_example_index_query_by_partition_and_filter_text,
                    "orginization","demo-orginization-3",
                    "currentIncidentId", "demo-currentIncidentId-500000"));
        }

        public boolean executeOperation() {
            // Perform a query using a partition key and filter condition.
            final UserDataDO itemToFind = new UserDataDO();
            itemToFind.setOrginization("demo-orginization-3");

            // Use an expression names Map to avoid the potential for attribute names
            // colliding with DynamoDB reserved words.
            final Map <String, String> filterExpressionAttributeNames = new HashMap<>();
            filterExpressionAttributeNames.put("#currentIncidentId", "currentIncidentId");

            final Map<String, AttributeValue> filterExpressionAttributeValues = new HashMap<>();
            filterExpressionAttributeValues.put(":MincurrentIncidentId",
                new AttributeValue().withS("demo-currentIncidentId-500000"));

            // Perform get using Partition key and sort key condition
            DynamoDBQueryExpression<UserDataDO> queryExpression = new DynamoDBQueryExpression<UserDataDO>()
                .withHashKeyValues(itemToFind)
                .withFilterExpression("#currentIncidentId > :MincurrentIncidentId")
                .withExpressionAttributeNames(filterExpressionAttributeNames)
                .withExpressionAttributeValues(filterExpressionAttributeValues)
                .withConsistentRead(false);
            results = mapper.query(UserDataDO.class, queryExpression);
            if (results != null) {
                resultsIterator = results.iterator();
                if (resultsIterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<DemoNoSQLResult> getNextResultGroup() {
            return getNextResultsGroupFromIterator(resultsIterator);
        }

        @Override
        public void resetResults() {
            resultsIterator = results.iterator();
        }
    }

    public class DemoSuperiorsQueryWithPartitionKeySortKeyAndFilterCondition extends DemoNoSQLOperationBase {

        private PaginatedQueryList<UserDataDO> results;
        private Iterator<UserDataDO> resultsIterator;

        DemoSuperiorsQueryWithPartitionKeySortKeyAndFilterCondition (final Context context) {
            super(
                context.getString(R.string.nosql_operation_title_index_query_by_partition_sort_condition_and_filter_text),
                context.getString(R.string.nosql_operation_example_index_query_by_partition_sort_condition_and_filter_text,
                    "orginization", "demo-orginization-3",
                    "orgSuperior", "demo-orgSuperior-500000",
                    "currentIncidentId", "demo-currentIncidentId-500000"));
        }

        public boolean executeOperation() {
            // Perform a query using a partition key, sort condition, and filter.
            final UserDataDO itemToFind = new UserDataDO();
            itemToFind.setOrginization("demo-orginization-3");
            final Condition sortKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.LT.toString())
                .withAttributeValueList(new AttributeValue().withS("demo-orgSuperior-500000"));

            // Use a map of expression names to avoid the potential for attribute names
            // colliding with DynamoDB reserved words.
            final Map<String, String> filterExpressionAttributeNames = new HashMap<>();
            filterExpressionAttributeNames.put("#currentIncidentId", "currentIncidentId");

            final Map<String, AttributeValue> filterExpressionAttributeValues = new HashMap<>();
            filterExpressionAttributeValues.put(":MincurrentIncidentId",
                new AttributeValue().withS("demo-currentIncidentId-500000"));

            // Perform get using Partition key and sort key condition
            DynamoDBQueryExpression<UserDataDO> queryExpression = new DynamoDBQueryExpression<UserDataDO>()
                .withHashKeyValues(itemToFind)
                .withRangeKeyCondition("orgSuperior", sortKeyCondition)
                .withFilterExpression("#currentIncidentId > :MincurrentIncidentId")
                .withExpressionAttributeNames(filterExpressionAttributeNames)
                .withExpressionAttributeValues(filterExpressionAttributeValues)
                .withConsistentRead(false);
            results = mapper.query(UserDataDO.class, queryExpression);
            if (results != null) {
                resultsIterator = results.iterator();
                if (resultsIterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<DemoNoSQLResult> getNextResultGroup() {
            return getNextResultsGroupFromIterator(resultsIterator);
        }

        @Override
        public void resetResults() {
            resultsIterator = results.iterator();
        }
    }

    /********* Scan Inner Classes *********/

    public class DemoScanWithFilter extends DemoNoSQLOperationBase {

        private PaginatedScanList<UserDataDO> results;
        private Iterator<UserDataDO> resultsIterator;

        DemoScanWithFilter(final Context context) {
            super(context.getString(R.string.nosql_operation_title_scan_with_filter),
                String.format(context.getString(R.string.nosql_operation_example_scan_with_filter),
                    "currentIncidentId", "demo-currentIncidentId-500000"));
        }

        @Override
        public boolean executeOperation() {
            // Use an expression names Map to avoid the potential for attribute names
            // colliding with DynamoDB reserved words.
            final Map <String, String> filterExpressionAttributeNames = new HashMap<>();
            filterExpressionAttributeNames.put("#currentIncidentId", "currentIncidentId");

            final Map<String, AttributeValue> filterExpressionAttributeValues = new HashMap<>();
            filterExpressionAttributeValues.put(":MincurrentIncidentId",
                new AttributeValue().withS("demo-currentIncidentId-500000"));
            final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("#currentIncidentId > :MincurrentIncidentId")
                .withExpressionAttributeNames(filterExpressionAttributeNames)
                .withExpressionAttributeValues(filterExpressionAttributeValues);

            results = mapper.scan(UserDataDO.class, scanExpression);
            if (results != null) {
                resultsIterator = results.iterator();
                if (resultsIterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<DemoNoSQLResult> getNextResultGroup() {
            return getNextResultsGroupFromIterator(resultsIterator);
        }

        @Override
        public boolean isScan() {
            return true;
        }

        @Override
        public void resetResults() {
            resultsIterator = results.iterator();
        }
    }

    public class DemoScanWithoutFilter extends DemoNoSQLOperationBase {

        private PaginatedScanList<UserDataDO> results;
        private Iterator<UserDataDO> resultsIterator;

        DemoScanWithoutFilter(final Context context) {
            super(context.getString(R.string.nosql_operation_title_scan_without_filter),
                context.getString(R.string.nosql_operation_example_scan_without_filter));
        }

        @Override
        public boolean executeOperation() {
            final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            results = mapper.scan(UserDataDO.class, scanExpression);
            if (results != null) {
                resultsIterator = results.iterator();
                if (resultsIterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<DemoNoSQLResult> getNextResultGroup() {
            return getNextResultsGroupFromIterator(resultsIterator);
        }

        @Override
        public boolean isScan() {
            return true;
        }

        @Override
        public void resetResults() {
            resultsIterator = results.iterator();
        }
    }

    /**
     * Helper Method to handle retrieving the next group of query results.
     * @param resultsIterator the iterator for all the results (makes a new service call for each result group).
     * @return the next list of results.
     */
    private static List<DemoNoSQLResult> getNextResultsGroupFromIterator(final Iterator<UserDataDO> resultsIterator) {
        if (!resultsIterator.hasNext()) {
            return null;
        }
        List<DemoNoSQLResult> resultGroup = new LinkedList<>();
        int itemsRetrieved = 0;
        do {
            // Retrieve the item from the paginated results.
            final UserDataDO item = resultsIterator.next();
            // Add the item to a group of results that will be displayed later.
            resultGroup.add(new DemoNoSQLUserDataResult(item));
            itemsRetrieved++;
        } while ((itemsRetrieved < RESULTS_PER_RESULT_GROUP) && resultsIterator.hasNext());
        return resultGroup;
    }

    /** The DynamoDB object mapper for accessing DynamoDB. */
    private final DynamoDBMapper mapper;

    public DemoNoSQLTableUserData() {
        mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
    }

    @Override
    public String getTableName() {
        return "User_Data";
    }

    @Override
    public String getPartitionKeyName() {
        return "Artist";
    }

    public String getPartitionKeyType() {
        return "String";
    }

    @Override
    public String getSortKeyName() {
        return null;
    }

    public String getSortKeyType() {
        return "";
    }

    @Override
    public int getNumIndexes() {
        return 1;
    }

    @Override
    public void insertSampleData() throws AmazonClientException {
        Log.d(LOG_TAG, "Inserting Sample data.");
        final UserDataDO firstItem = new UserDataDO();

        firstItem.setUserId(AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID());
        firstItem.setCurrentIncidentId(
            DemoSampleDataGenerator.getRandomSampleString("currentIncidentId"));
        firstItem.setHeartbeatRecord(DemoSampleDataGenerator.getSampleNumberSet());
        firstItem.setIncidentSubordinates(DemoSampleDataGenerator.getSampleStringSet());
        firstItem.setIncidentSuperior(
            DemoSampleDataGenerator.getRandomSampleString("incidentSuperior"));
        firstItem.setLocation(DemoSampleDataGenerator.getSampleNumberSet());
        firstItem.setName(
            DemoSampleDataGenerator.getRandomSampleString("name"));
        firstItem.setOrgSubordinates(DemoSampleDataGenerator.getSampleStringSet());
        firstItem.setOrgSuperior(
            DemoSampleDataGenerator.getRandomSampleString("orgSuperior"));
        firstItem.setOrginization(DemoSampleDataGenerator.getRandomPartitionSampleString("orginization"));
        AmazonClientException lastException = null;

        try {
            mapper.save(firstItem);
        } catch (final AmazonClientException ex) {
            Log.e(LOG_TAG, "Failed saving item : " + ex.getMessage(), ex);
            lastException = ex;
        }

        final UserDataDO[] items = new UserDataDO[SAMPLE_DATA_ENTRIES_PER_INSERT-1];
        for (int count = 0; count < SAMPLE_DATA_ENTRIES_PER_INSERT-1; count++) {
            final UserDataDO item = new UserDataDO();
            item.setUserId(DemoSampleDataGenerator.getRandomSampleString("userId"));
            item.setCurrentIncidentId(DemoSampleDataGenerator.getRandomSampleString("currentIncidentId"));
            item.setHeartbeatRecord(DemoSampleDataGenerator.getSampleNumberSet());
            item.setIncidentSubordinates(DemoSampleDataGenerator.getSampleStringSet());
            item.setIncidentSuperior(DemoSampleDataGenerator.getRandomSampleString("incidentSuperior"));
            item.setLocation(DemoSampleDataGenerator.getSampleNumberSet());
            item.setName(DemoSampleDataGenerator.getRandomSampleString("name"));
            item.setOrgSubordinates(DemoSampleDataGenerator.getSampleStringSet());
            item.setOrgSuperior(DemoSampleDataGenerator.getRandomSampleString("orgSuperior"));
            item.setOrginization(DemoSampleDataGenerator.getRandomPartitionSampleString("orginization"));

            items[count] = item;
        }
        try {
            mapper.batchSave(Arrays.asList(items));
        } catch (final AmazonClientException ex) {
            Log.e(LOG_TAG, "Failed saving item batch : " + ex.getMessage(), ex);
            lastException = ex;
        }

        if (lastException != null) {
            // Re-throw the last exception encountered to alert the user.
            throw lastException;
        }
    }

    @Override
    public void removeSampleData() throws AmazonClientException {

        final UserDataDO itemToFind = new UserDataDO();
        itemToFind.setUserId(AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID());

        final DynamoDBQueryExpression<UserDataDO> queryExpression = new DynamoDBQueryExpression<UserDataDO>()
            .withHashKeyValues(itemToFind)
            .withConsistentRead(false)
            .withLimit(MAX_BATCH_SIZE_FOR_DELETE);

        final PaginatedQueryList<UserDataDO> results = mapper.query(UserDataDO.class, queryExpression);

        Iterator<UserDataDO> resultsIterator = results.iterator();

        AmazonClientException lastException = null;

        if (resultsIterator.hasNext()) {
            final UserDataDO item = resultsIterator.next();

            // Demonstrate deleting a single item.
            try {
                mapper.delete(item);
            } catch (final AmazonClientException ex) {
                Log.e(LOG_TAG, "Failed deleting item : " + ex.getMessage(), ex);
                lastException = ex;
            }
        }

        final List<UserDataDO> batchOfItems = new LinkedList<UserDataDO>();
        while (resultsIterator.hasNext()) {
            // Build a batch of books to delete.
            for (int index = 0; index < MAX_BATCH_SIZE_FOR_DELETE && resultsIterator.hasNext(); index++) {
                batchOfItems.add(resultsIterator.next());
            }
            try {
                // Delete a batch of items.
                mapper.batchDelete(batchOfItems);
            } catch (final AmazonClientException ex) {
                Log.e(LOG_TAG, "Failed deleting item batch : " + ex.getMessage(), ex);
                lastException = ex;
            }

            // clear the list for re-use.
            batchOfItems.clear();
        }


        if (lastException != null) {
            // Re-throw the last exception encountered to alert the user.
            // The logs contain all the exceptions that occurred during attempted delete.
            throw lastException;
        }
    }

    private List<DemoNoSQLOperationListItem> getSupportedDemoOperations(final Context context) {
        List<DemoNoSQLOperationListItem> noSQLOperationsList = new ArrayList<DemoNoSQLOperationListItem>();
            noSQLOperationsList.add(new DemoGetWithPartitionKey(context));

        noSQLOperationsList.add(new DemoNoSQLOperationListHeader(
            context.getString(R.string.nosql_operation_header_secondary_queries, "superiors")));

        noSQLOperationsList.add(new DemoSuperiorsQueryWithPartitionKeyOnly(context));
        noSQLOperationsList.add(new DemoSuperiorsQueryWithPartitionKeyAndFilterCondition(context));
        noSQLOperationsList.add(new DemoSuperiorsQueryWithPartitionKeyAndSortKeyCondition(context));
        noSQLOperationsList.add(new DemoSuperiorsQueryWithPartitionKeySortKeyAndFilterCondition(context));
        noSQLOperationsList.add(new DemoNoSQLOperationListHeader(
            context.getString(R.string.nosql_operation_header_scan)));
        noSQLOperationsList.add(new DemoScanWithoutFilter(context));
        noSQLOperationsList.add(new DemoScanWithFilter(context));
        return noSQLOperationsList;
    }

    @Override
    public void getSupportedDemoOperations(final Context context,
                                           final SupportedDemoOperationsHandler opsHandler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<DemoNoSQLOperationListItem> supportedOperations = getSupportedDemoOperations(context);
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        opsHandler.onSupportedOperationsReceived(supportedOperations);
                    }
                });
            }
        }).start();
    }
}
