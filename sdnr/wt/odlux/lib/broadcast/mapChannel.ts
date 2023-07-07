
const channel: BroadcastChannel = new BroadcastChannel("odlux_map");
const listeners: { [key: string]: ((data: any) => void)[] } = {};

channel.onmessage = (eventMessage: MessageEvent<any>) => {
  const { key, data } = eventMessage.data;
  if (listeners[key]) {
    listeners[key].forEach(listener => listener(data));
  }
};

export const sendMapMessage = (data: any, key: string) => {
  channel.postMessage({ key, data });
};

export const addMapMessageListener = (key: string, listener: (data: any) => void) => {
  if (!listeners[key]) {
    listeners[key] = [];
  }
  
  if (!listeners[key].find(l => l === listener)) {
    listeners[key].push(listener);
  }

  return () => {
    listeners[key] = listeners[key].filter(l => l !== listener);
  }
};

