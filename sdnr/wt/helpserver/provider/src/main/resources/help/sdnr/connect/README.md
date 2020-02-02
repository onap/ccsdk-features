# Connect

The 'Connect' application on OpenDaylight provides up-to-date connectivity information about the wireless devices in the network. It automatically displays new network elements and their connection status. Despite the network elements usually automatically mount themselves, an additional small window allows manually mounting devices/mediators. For better understanding alarms and status, a connection status log lists all the connection status changes of OpenDaylight mount points.

## Views

The graphical user interfaces is divided in two sections.

### Network Elements

Network Elements are physical network functions (PNFs). A table view show configured and connected NetConf Servers to the SDN-R cluster. 
This view also offer to manually configure/mount the device with the '+' icon. The SDN controller will start connecting the NetConf server.

### Connection Status Log

The log lists the connections status changes between SDN Controller and NetConf servers (devices).
