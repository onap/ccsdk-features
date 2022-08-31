# Connect

The 'Connect' application on OpenDaylight provides up-to-date connectivity information about the network nodes exposing a NETCONF/YANG interface. It automatically displays new Nodes and their connection status. Usually, the NETCONF servers of the Nodes mount themselves. If necessary, they can be mounted manually by right-clicking on the row representing a node and selecting the 'mount' action. For better understanding of alarms and status, a connection status log lists all the connection status changes of OpenDaylight mount points.

## Views

The graphical user interface is divided into two sections.

### Nodes

Nodes are network functions with a NETCONF/YANG management and control interface. A table view shows all configured and connected NETCONF Servers of the SDN-R cluster. This view also allows to manually configure/mount a node via the '+' button. The SDN controller will start connecting to the NETCONF server.

Nodes can be marked as 'required'. If a node is required, it will stay available even if disconnected. If a node is not required, it will be deleted once disconnected.

By right-clicking on a row representing a node, an action menu opens. The menu allows to mount, unmount, view the details, edit and remove the node. Additionally, it links to several applications like Fault and Configure, which will be filtered to display information relevant to the selected node.

### Connection Status Log

The log lists the connection status changes between SDN Controller and NETCONF servers (devices, Network Elements, network functions).