type YangInfo = [string, (string | null | undefined)];

const cache: { [path: string]: string } = {

};

class YangService {

  public async getCapability(capability: string, version?: string) {
    const url = `/yang-schema/${capability}${version ? `/${version}` : ""}`;

    const cacheHit = cache[url];
    if (cacheHit) return cacheHit;

    const res = await fetch(url);
    const yangFile = res.ok && await res.text();
    if (yangFile !== false && yangFile !== null) {
      cache[url] = yangFile;
    }
    return yangFile;
  }
}

export const yangService = new YangService();
export default yangService;