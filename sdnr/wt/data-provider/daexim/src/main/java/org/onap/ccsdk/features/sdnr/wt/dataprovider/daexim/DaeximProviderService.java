package org.onap.ccsdk.features.sdnr.wt.dataprovider.daexim;

public interface DaeximProviderService {

	/**
	 * import data from file and write these to database
	 * @param filename
	 */
	void importData(String filename);
	/**
	 * export current data to file
	 * @param filename
	 */
	void exportData(String filename);
	
	/**
	 * clean up the database
	 */
	void clean();
}
