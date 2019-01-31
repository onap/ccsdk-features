/*******************************************************************************
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.devicemanager.base.database;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Setup index in the database
 * @author herbert
 *
 */
public class IndexClientBuilder implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(IndexClientBuilder.class);

    /** Index name to be used */
    private final String index;
    /** Location of mapping data **/
    private String mappingSettingFileName = null;
    /** Location of configuration data **/
    private String modelDataDirectory = null;

	private final ScheduledExecutorService scheduler;
    private HtDatabaseClientAbstract client;
    private HtDatabaseNode databaseNode;


    // --- Construct and initialize

    public IndexClientBuilder(String index) {
    	this.index = index;
    	this.databaseNode = null;
    	this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    // Additional setter functions

    public IndexClientBuilder setMappingSettingJsonFileName(String jsonFileName) {
    	this.mappingSettingFileName = jsonFileName;
    	return(this);
    }

    public IndexClientBuilder setModelDataDirectory(String jsonDirectory) {
    	this.modelDataDirectory = jsonDirectory;
    	return(this);
    }

    public HtDatabaseClientAbstract create(HtDatabaseNode database) {
		LOG.info("Create {} start with node", this.getClass().getSimpleName() );
     	this.databaseNode = database;
		client = new HtDatabaseClientAbstract(index, database);
		setupIndex();
       	return client;
    }


    public void stop() {
	   this.scheduler.shutdown();
    }

    @Override
    public void close() throws Exception {
	   stop();
    }

    private void setupIndex() {
		if (! client.isExistsIndex()) {
			LOG.info("Index not existing ... create index");

			// Initialisation 1
			if (mappingSettingFileName != null) {
  				JSONObject indexconfigdata=Resources.getJSONFile(mappingSettingFileName);
    			client.doCreateIndexWithMapping(indexconfigdata);
			} else
				client.doCreateIndex();

			// Initialisation 2 - start asynchron initialization and let it run
   			scheduler.schedule(fillDatabase, 0, TimeUnit.SECONDS);
		}
    }

    private final Runnable fillDatabase = new Runnable() {
    	@Override
    	public void run() {
    		if (databaseNode != null) {
    			databaseNode.setInitializedTarget();
    		}
    		try { //Prevent ending task by exception
				if (modelDataDirectory != null) {
					LOG.info("... write initial data for index {}",index);
					List<JSONObject> dataList=Resources.getJSONFiles(modelDataDirectory, false);
					LOG.debug("received number of objects: {} of index {}", dataList.size(), index);
					for (JSONObject da: dataList) {
						client.doWriteJSONObject(da);
					}
					LOG.debug("wrote all objects for index {}", index);
				} else {
					LOG.info("No initial data for index {}",index);
				}
			} catch (Exception e) {
				LOG.warn("Problem during initialization of index "+index+" {}", e);
			}
    		if (databaseNode != null) {
    			databaseNode.setInitializedReached();
    		}
    	}
    };

    /*---------------------------------------------------------
     * static files
     */

    public static IndexClientBuilder getBuilder(String index) {
    	return new IndexClientBuilder(index);
    }


}
