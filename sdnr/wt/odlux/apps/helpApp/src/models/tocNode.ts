export type VersionInfo = {
  label: string,
  path: string,
  date: string
}

export type TocNode  = {
  label: string;
  versions: {
    [versionKey: string]: VersionInfo,
    current: VersionInfo
  };
  nodes?: TocNodeCollection;
}

export type TocNodeCollection = { [tocNodeKey: string]: TocNode };


export type TocTreeNode = {
  id: string;
  label: string;
  uri: string;
  nodes?: TocTreeNode[];
  disabled?: boolean;
}
