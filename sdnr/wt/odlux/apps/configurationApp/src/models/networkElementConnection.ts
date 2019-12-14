export type NetworkElementConnection = {
  id?: string;
  nodeId: string;
  host: string;
  port: number;
  username?: string;
  password?: string;
  isRequired?: boolean;
  status?: "connected" | "mounted" | "unmounted" | "connecting" | "disconnected" | "idle";
  coreModelCapability?: string;
  deviceType?: string;
  nodeDetails?: {
    availableCapabilities: string[];
    unavailableCapabilities: {
      failureReason: string;
      capability: string;
    }[];
  }
}
