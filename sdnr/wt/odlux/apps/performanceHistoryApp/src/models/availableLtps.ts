  export type LtpIds = {
    key: string
  }

  export type Bucket<T>={ 
    buckets: T[]
  }

/**
 * Represents distinct available ltps using elasticsearch aggregations structure.
 */
  export type DistinctLtp = {
    "uuid-interface": Bucket<LtpIds>
  }
