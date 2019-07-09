package org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.json.JSONObject;

public interface Resources {
    List<URL> getFileURLs(String folder, String filter, boolean recursive)
            throws IOException;

    List<JSONObject> getJSONFiles(String folder, boolean recursive);

    JSONObject getJSONFile(String resFile);

    /**
     * Used for reading plugins from resource files /elasticsearch/plugins/head
     * /etc/elasticsearch-plugins /elasticsearch/plugins
     *
     * @param resFolder resource folder pointing to the related files
     * @param dstFolder destination
     * @param rootDirToRemove part from full path to remove
     * @return true if files could be extracted
     */
    boolean copyFolderInto(String resFolder, String dstFolder, String rootDirToRemove);

    boolean extractFileTo(String resFile, File oFile);

    boolean extractFileTo(URL u, File oFile);
}
