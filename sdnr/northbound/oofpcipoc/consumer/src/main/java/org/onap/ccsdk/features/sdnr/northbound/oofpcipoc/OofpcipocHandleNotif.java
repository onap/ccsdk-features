/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.features.sdnr.northbound.oofpcipoc;

import java.io.Writer;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos.CellConfig;
import org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos.FAPServiceList;
import org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos.LTE;
import org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos.LTENeighborListInUseLTECell;
import org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos.NeighborListInUse;
import org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos.PayloadObject;
import org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos.RAN;
import org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos.RadioAccess;
import org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos.X0005b9Lte;

import org.opendaylight.mdsal.binding.api.DataBroker;

import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.rev190308.NbrlistChangeNotification;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.rev190308.NetconfConfigChange;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.rev190308.nbrlist.change.notification.*;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.rev190308.nbrlist.change.notification.fap.service.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Defines a base implementation for your listener. This class extends from a
 * helper class which provides storage for the most commonly used components of
 * the MD-SAL. Additionally the base class provides some basic logging and
 * initialization / clean up methods.
 *
 */
public class OofpcipocHandleNotif implements AutoCloseable {

	private static final Logger LOG = LoggerFactory.getLogger(OofpcipocHandleNotif.class);

	private static final String APPLICATION_NAME = "Oofpcipoc";

	private static final String TRUE = "true";
	private static final String FALSE = "false";
	private static final String UTF_8 = "UTF-8";

	private static final String PARAMETER_NAME = "parameter-name";
    private static final String STRING_VALUE = "string-value";

	private final ExecutorService executor;

	protected DataBroker dataBroker;

	private OofpcipocClient OofpcipocClient;

    private NotificationService notificationService;

    private Registration registration;

	public OofpcipocHandleNotif() {

		this.LOG.info("Creating listener for {}", APPLICATION_NAME);
		executor = Executors.newFixedThreadPool(1);
		this.dataBroker = null;
		this.OofpcipocClient = null;
	}
	
	public void setDataBroker(DataBroker dataBroker) {
		this.dataBroker = dataBroker;
	}
	
	public void setClient(OofpcipocClient OofpcipocClient) {
		this.OofpcipocClient = OofpcipocClient;
	}

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

	public void init() {
		LOG.info("Placeholder: Initializing listener  for {}", APPLICATION_NAME);
        this.registration = notificationService.registerListener(NbrlistChangeNotification.class,
                OofpcipocHandleNotif.this::onNbrlistChangeNotification);
	}

	@Override
	public void close() throws Exception {
		LOG.info("Closing listener for {}", APPLICATION_NAME);
		executor.shutdown();
        registration.close();
		LOG.info("Successfully closed listener for {}", APPLICATION_NAME);
	}

	public void onNbrlistChangeNotification(final NbrlistChangeNotification notification) {

		LOG.info("Reached onNbrlistChangeNotification");

		LOG.info("Number of FAPService Entries Changed {}", notification.getFapServiceNumberOfEntriesChanged());

		// START: Create RadioAccess payload object/string from the notification

		String payloadString = null;
		PayloadObject payloadObject = new PayloadObject();
		org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos.Payload payload = new org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos.Payload();
		RadioAccess radioAccess = new RadioAccess();
		List<FAPServiceList> fAPServiceList = new ArrayList<>();

		radioAccess.setFAPServiceNumberOfEntries(notification.getFapServiceNumberOfEntriesChanged().toString());
		Map<FapServiceKey, FapService> fapSvcs = notification.getFapService();

		for (Map.Entry<FapServiceKey,FapService> entry : fapSvcs.entrySet() ) {

			FapService fapSvc = entry.getValue();
			FAPServiceList fapServiceElement = new FAPServiceList();

			fapServiceElement.setAlias(fapSvc.getAlias());
			fapServiceElement.setX0005b9Lte(new X0005b9Lte(fapSvc.getPhyCellIdInUse().toString(), fapSvc.getPnfName()));

			List<LTENeighborListInUseLTECell> lTENeighborListInUseLTECell = new ArrayList<>();
			Map<LteRanNeighborListInUseLteCellChangedKey, LteRanNeighborListInUseLteCellChanged> lteRanElements = fapSvc.getLteRanNeighborListInUseLteCellChanged();
			for (Map.Entry<LteRanNeighborListInUseLteCellChangedKey, LteRanNeighborListInUseLteCellChanged> lteRanEntry : lteRanElements.entrySet()) {
				LteRanNeighborListInUseLteCellChanged lteRanElement = lteRanEntry.getValue(); 
				LTENeighborListInUseLTECell lTENeighborListInUseLTECellElement = new LTENeighborListInUseLTECell();
				lTENeighborListInUseLTECellElement.setAlias(lteRanElement.getCid());
				lTENeighborListInUseLTECellElement.setBlacklisted(lteRanElement.getBlacklisted().toString());
				lTENeighborListInUseLTECellElement.setCid(lteRanElement.getCid());
				lTENeighborListInUseLTECellElement.setEnable(TRUE);
				lTENeighborListInUseLTECellElement.setMustInclude(TRUE);
				lTENeighborListInUseLTECellElement.setPhyCellId(lteRanElement.getPhyCellId().toString());
				lTENeighborListInUseLTECellElement.setPlmnid(lteRanElement.getPlmnid());
				lTENeighborListInUseLTECellElement.setPnfName(lteRanElement.getPnfName());

				lTENeighborListInUseLTECell.add(lTENeighborListInUseLTECellElement);
			}

			NeighborListInUse neighborListInUse = new NeighborListInUse();
			neighborListInUse.setLTECellNumberOfEntries(
					String.valueOf(fapSvc.getLteRanNeighborListInUseLteCellChanged().size()));
			neighborListInUse.setLTENeighborListInUseLTECell(lTENeighborListInUseLTECell);

			CellConfig cellConfig = new CellConfig();
			LTE lTE = new LTE();
			RAN rAN = new RAN();

			rAN.setCellIdentity(fapSvc.getCid());
			rAN.setNeighborListInUse(neighborListInUse);

			lTE.setRAN(rAN);
			cellConfig.setLTE(lTE);
			fapServiceElement.setCellConfig(cellConfig);

			fAPServiceList.add(fapServiceElement);
		}
		radioAccess.setFAPServiceList(fAPServiceList);
		payload.setRadioAccess(radioAccess);
		payloadObject.setPayload(payload);

		ObjectMapper mapper = new ObjectMapper();

		try {
			payloadString = mapper.writeValueAsString(payloadObject.getPayload());
			LOG.info("Stringified Payload Object::" + payloadString + "\n" + "\n");
		} catch (JsonProcessingException jsonProcessingException) {
			LOG.error("Error while processing Payload Object", jsonProcessingException);
			return;
		}

		// END: Create RadioAccess payload object/string from the notification

		// START: Build RPC message and invoke RPC

		String rpcMessageBody = null;

		try {
			rpcMessageBody = buildHandleNbrlistChangeNotifRPCMsg(radioAccess, payloadString, "/opt/onap/ccsdk/data/properties/rpc-message-sliapi-execute.vt");
			LOG.debug("rpc message body::" + rpcMessageBody);
			invokeRPC(rpcMessageBody);
		} catch (Exception e) {
			LOG.error("Unable to build rpc message body::", e);
			return;
		}
	}
	private void invokeRPC(String rpcMsgbody) {
		try {
            String odlUrlBase = "http://sdnc.onap:8282/restconf/operations"; //using cluster SDNC URL
            String odlUser = "admin";
            String odlPassword = "Kp8bJ4SXszM0WXlhak3eHlcse2gAw84vaoGGmJvUy2U";
            String sdncEndpoint = " SLI-API:execute-graph";

            if ((odlUrlBase != null) && (odlUrlBase.length() > 0)) {
                SdncOdlConnection conn = SdncOdlConnection.newInstance(odlUrlBase + "/" + sdncEndpoint, odlUser, odlPassword);

                conn.send("POST", "application/json", rpcMsgbody);
            } else {
                LOG.info("POST message body would be:\n" + rpcMsgbody);
            }
        } catch (Exception e) {
            LOG.error("Unable to process message", e);
        }
	}

	private String buildHandleNbrlistChangeNotifRPCMsg(RadioAccess radioAccess, String payloadString, String templatePath) throws IOException {
		VelocityEngine velocityEngine = new VelocityEngine();
		velocityEngine.init();

		String SLI_PARAMETERS = "sli_parameters";
		String HANDLE_NBRLIST_CHANGE_NOTIF = "handle-nbrlist-change-notif";
		String HANDLE_NBRLIST_CHANGE_NOTIF_INPUT = HANDLE_NBRLIST_CHANGE_NOTIF+"-input.";
		String HANDLE_NBRLIST_CHANGE_NOTIF_INPUT_FAP_SERVICE = HANDLE_NBRLIST_CHANGE_NOTIF_INPUT+"fap-service";
		String FAP_SERVICE_NO_OF_ENTRIES_CHANGED= "fap-service-number-of-entries-changed";

		JSONArray sliParametersArray = new JSONArray();

		VelocityContext context = new VelocityContext();
		context.put("rpc_name", "handle-nbrlist-change-notif");

		sliParametersArray.put(new JSONObject().put(PARAMETER_NAME, HANDLE_NBRLIST_CHANGE_NOTIF_INPUT+FAP_SERVICE_NO_OF_ENTRIES_CHANGED)
    			.put(STRING_VALUE, radioAccess.getFAPServiceNumberOfEntries()));

		int count = 0;

		for(FAPServiceList fapServiceListElement: radioAccess.getFAPServiceList()) {

			String prefix = HANDLE_NBRLIST_CHANGE_NOTIF_INPUT_FAP_SERVICE+"["+count+"].";

			sliParametersArray.put(new JSONObject().put(PARAMETER_NAME, prefix+"alias")
	    			.put(STRING_VALUE, fapServiceListElement.getAlias()));
			sliParametersArray.put(new JSONObject().put(PARAMETER_NAME, prefix+"cid")
	    			.put(STRING_VALUE, fapServiceListElement.getAlias()));
			sliParametersArray.put(new JSONObject().put(PARAMETER_NAME, prefix+"phy-cell-id-in-use")
	    			.put(STRING_VALUE, fapServiceListElement.getX0005b9Lte().getPhyCellIdInUse()));
			sliParametersArray.put(new JSONObject().put(PARAMETER_NAME, prefix+"pnf-name")
	    			.put(STRING_VALUE, fapServiceListElement.getX0005b9Lte().getPnfName()));

			sliParametersArray.put(new JSONObject().put(PARAMETER_NAME, prefix+"lte-cell-number-of-entries")
	    			.put(STRING_VALUE, fapServiceListElement.getCellConfig().getLTE().getRAN().getNeighborListInUse().getLTECellNumberOfEntries()));

			int lteNbrListInUseCount = 0;
			for(LTENeighborListInUseLTECell lTENeighborListInUseLTECellElement: fapServiceListElement.getCellConfig().getLTE().getRAN().getNeighborListInUse().getLTENeighborListInUseLTECell()) {
				String lteNbrListPrefix = "lte-ran-neighbor-list-in-use-lte-cell-changed["+lteNbrListInUseCount+"].";
				sliParametersArray.put(new JSONObject().put(PARAMETER_NAME, prefix+lteNbrListPrefix+"plmnid")
		    			.put(STRING_VALUE, lTENeighborListInUseLTECellElement.getPlmnid()));
				sliParametersArray.put(new JSONObject().put(PARAMETER_NAME, prefix+lteNbrListPrefix+"cid")
		    			.put(STRING_VALUE, lTENeighborListInUseLTECellElement.getCid()));
				sliParametersArray.put(new JSONObject().put(PARAMETER_NAME, prefix+lteNbrListPrefix+"phy-cell-id")
		    			.put(STRING_VALUE, lTENeighborListInUseLTECellElement.getPhyCellId()));
				sliParametersArray.put(new JSONObject().put(PARAMETER_NAME, prefix+lteNbrListPrefix+"pnf-name")
		    			.put(STRING_VALUE, lTENeighborListInUseLTECellElement.getPnfName()));
				sliParametersArray.put(new JSONObject().put(PARAMETER_NAME, prefix+lteNbrListPrefix+"blacklisted")
		    			.put(STRING_VALUE, lTENeighborListInUseLTECellElement.getBlacklisted()));
				lteNbrListInUseCount++;
			}

			count++;
		}

		sliParametersArray.put(new JSONObject().put(PARAMETER_NAME, HANDLE_NBRLIST_CHANGE_NOTIF_INPUT+"payload")
    			.put(STRING_VALUE, payloadString));

		context.put(SLI_PARAMETERS, sliParametersArray);

		Writer writer = new StringWriter();
		velocityEngine.mergeTemplate(templatePath, UTF_8, context, writer);
		writer.flush();

		return writer.toString();
	}

	public void onNetconfConfigChange(final NetconfConfigChange notification) {

	}

}
