package org.onap.ccsdk.features.sdnr.wt.dataprovider.impl;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMaintenanceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMediatorServerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMediatorServerOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateNetworkElementConnectionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMaintenanceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMediatorServerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMediatorServerOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteNetworkElementConnectionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadCmlogListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadCmlogListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadConnectionlogListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadConnectionlogListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadEventlogListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadEventlogListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadFaultcurrentListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadFaultcurrentListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadFaultlogListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadFaultlogListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadGuiCutThroughEntryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadGuiCutThroughEntryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadInventoryDeviceListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadInventoryDeviceListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadInventoryListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadInventoryListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadMaintenanceListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadMaintenanceListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadMediatorServerListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadMediatorServerListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadNetworkElementConnectionListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadNetworkElementConnectionListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mDeviceListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mDeviceListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mLtpListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mLtpListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hDeviceListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hDeviceListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hLtpListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hLtpListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadStatusInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadStatusOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadTlsKeyEntryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadTlsKeyEntryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMaintenanceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMediatorServerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMediatorServerOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateNetworkElementConnectionOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public interface DataProviderService {

    ListenableFuture<RpcResult<ReadFaultcurrentListOutput>> readFaultcurrentList(
            ReadFaultcurrentListInput input);

    ListenableFuture<RpcResult<ReadFaultlogListOutput>> readFaultlogList(ReadFaultlogListInput input);

    ListenableFuture<RpcResult<ReadCmlogListOutput>> readCmlogList(ReadCmlogListInput input);

    ListenableFuture<RpcResult<ReadMaintenanceListOutput>> readMaintenanceList(ReadMaintenanceListInput input);

    ListenableFuture<RpcResult<ReadMediatorServerListOutput>> readMediatorServerList(
            ReadMediatorServerListInput input);

    ListenableFuture<RpcResult<ReadNetworkElementConnectionListOutput>> readNetworkElementConnectionList(
            ReadNetworkElementConnectionListInput input);

    ListenableFuture<RpcResult<ReadPmdata15mListOutput>> readPmdata15mList(ReadPmdata15mListInput input);

    ListenableFuture<RpcResult<ReadPmdata24hListOutput>> readPmdata24hList(ReadPmdata24hListInput input);

    ListenableFuture<RpcResult<ReadStatusOutput>> readStatus(ReadStatusInput input);

    ListenableFuture<RpcResult<ReadInventoryListOutput>> readInventoryList(ReadInventoryListInput input);

    ListenableFuture<RpcResult<ReadInventoryDeviceListOutput>> readInventoryDeviceList(
            ReadInventoryDeviceListInput input);

    ListenableFuture<RpcResult<ReadPmdata15mLtpListOutput>> readPmdata15mLtpList(
            ReadPmdata15mLtpListInput input);

    ListenableFuture<RpcResult<ReadPmdata15mDeviceListOutput>> readPmdata15mDeviceList(
            ReadPmdata15mDeviceListInput input);

    ListenableFuture<RpcResult<ReadPmdata24hLtpListOutput>> readPmdata24hLtpList(
            ReadPmdata24hLtpListInput input);

    ListenableFuture<RpcResult<ReadPmdata24hDeviceListOutput>> readPmdata24hDeviceList(
            ReadPmdata24hDeviceListInput input);

    ListenableFuture<RpcResult<ReadConnectionlogListOutput>> readConnectionlogList(
            ReadConnectionlogListInput input);

    ListenableFuture<RpcResult<CreateNetworkElementConnectionOutput>> createNetworkElementConnection(
            CreateNetworkElementConnectionInput input);

    ListenableFuture<RpcResult<UpdateNetworkElementConnectionOutput>> updateNetworkElementConnection(
            UpdateNetworkElementConnectionInput input);

    ListenableFuture<RpcResult<DeleteNetworkElementConnectionOutput>> deleteNetworkElementConnection(
            DeleteNetworkElementConnectionInput input);

    ListenableFuture<RpcResult<DeleteMediatorServerOutput>> deleteMediatorServer(
            DeleteMediatorServerInput input);

    ListenableFuture<RpcResult<CreateMediatorServerOutput>> createMediatorServer(
            CreateMediatorServerInput input);

    ListenableFuture<RpcResult<CreateMaintenanceOutput>> createMaintenance(CreateMaintenanceInput input);

    ListenableFuture<RpcResult<DeleteMaintenanceOutput>> deleteMaintenance(DeleteMaintenanceInput input);

    ListenableFuture<RpcResult<UpdateMediatorServerOutput>> updateMediatorServer(
            UpdateMediatorServerInput input);

    ListenableFuture<RpcResult<UpdateMaintenanceOutput>> updateMaintenance(UpdateMaintenanceInput input);

    ListenableFuture<RpcResult<ReadEventlogListOutput>> readEventlogList(ReadEventlogListInput input);

    ListenableFuture<RpcResult<ReadGuiCutThroughEntryOutput>> readGuiCutThroughEntry(
            ReadGuiCutThroughEntryInput input);

    ListenableFuture<RpcResult<ReadTlsKeyEntryOutput>> readTlsKeyEntry(ReadTlsKeyEntryInput input);
}
