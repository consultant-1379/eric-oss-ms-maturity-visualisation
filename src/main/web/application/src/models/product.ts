///
/// COPYRIGHT Ericsson 2021
///
///
///
/// The copyright to the computer program(s) herein is the property of
///
/// Ericsson Inc. The programs may be used and/or copied only with written
///
/// permission from Ericsson Inc. or in accordance with the terms and
///
/// conditions stipulated in the agreement/contract under which the
///
/// program(s) have been supplied.
///


export interface Products {
  jobNotification: string[];
  productWiseMetadata: ProductWiseMetadata[];
}

export interface ProductWiseMetadata {
  product: string;
  totalJobs: string;
  metaData: MetaData[];
}

export interface MetaData {
  section: string;
  totalJobs: string;
}

export interface ProductMap {
  [key: string]: string
}

export interface ProductAndJobs {
  [key: string]: string[]
}