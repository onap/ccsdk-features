export type NetworkElementConnection = {
  id?: string;
  nodeId: string;
  isRequired: boolean;
  host: string;
  port: number;
  username?: string;
  password?: string;
  webUri?: string;
  isWebUriUnreachable?: boolean;
  status?: "Connected" | "mounted" | "unmounted" | "Connecting" | "Disconnected" | "idle";
  coreModelCapability?: string;
  deviceType?: string;
  nodeDetails?: {
    availableCapabilites: {
      capabilityOrigin: string;
      capability: string;
    }[];
    unavailableCapabilities: {
      failureReason: string;
      capability: string;
    }[];
  }
}


export type UpdateNetworkElement = {
  id: string;
  isRequired?: boolean;
  username?: string;
  password?: string;
}

export type ConnectionStatus = {
  status: string
}