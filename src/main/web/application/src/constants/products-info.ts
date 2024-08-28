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

export const productsInfo: ProductInfo = {
    "ADC": {
        title: "Analysis Data Collection Dashboard",
        description: "ADC comes under DMI (DMI is separated by functional scope into DMM and ADC). ADC has SFTP filetrans, ENM file notification service, PM-counter service, 4gp event file-trans & processing, 5gp event file trans & processing."

    },
    "AUTO APPS": {
        title: "Auto Apps Dashboard",
        description: "Auto Apps pipeline uses the APP-MGR API endpoints of App Onboarding and App LCM to Onboard and Instantiate the auto apps onto EIC."
    },
    "CH": {
        title: "Configuration Handling Dashboard",
        description: "CH is the application containing Network Configuratin Management Proxy (NCMP), which is a common ONAP interface for handling network elements."
    },
    "DMM": {
        title: "Data Management & Movement Dashboard",
        description: "DMM is concerned with data movement, data discovery and data storage in \"raw\" form. It provides a backbone for other areas of the system to share data and communicate."
    },
    "OSS": {
        title: "OSS Dashboard",
        description: "The OSS portal is a web-based portal providing a single point of entry for GUI applications. It supports single sign-on providing a single pane of glass to the EIC ecosystem."
    },
    "BP": {
        title: "Base Platform Dashboard",
        description: "Base Platform is a flexible composition of IT services including security, messaging, storage, logging & telemetry, that can be used as a base-layer for application development."
    },
    "EIC": {
        title: "Ericsson Intelligent Controller",
        description: "EIC is an automation platform and an associated developer ecosystem that supports rApp development by Ericsson and third-party vendors."
    },
    "EAS": {
        title: "Ericsson Adaptation Support Dashboard",
        description: "Ericsson Adaptation is built from a group of services providing access to external ENM subsystems. It provides Ericsson-specific topology data adaptation which is stored in Network Topology and the related Ericsson-specific model information is presented in a compatible YANG format and also provides a network discovery functionality, which automatically exposes compatible network entities from the ENM subsystem through Network Configuration."
    },
    "APP MGR": {
        title: "Application Manager Dashboard",
        description: "Application Manager is a procedure used in Onboarding an rApp, uploads the rApp into EIC. The rApp is parsed and each of its artifacts is placed within the EIC services."
    },
    "PMH": {
        title: "Preliminary Stats Calculation Handling",
        description: ""
    },
    "ML": {
        title: "Model Life Cycle Manager",
        description: ""
    },
    "OS": {
        title: "",
        description: ""
    },
    "ALL": {
        title: "",
        description: ""
    }
}

export interface ProductInfo {
     [key: string]: ProductDetail
}

export interface ProductDetail{
    title: string;
    description: string;
}