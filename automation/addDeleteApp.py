#!/usr/bin/env python3
#
# COPYRIGHT Ericsson 2021
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#

import os
import logging
import subprocess

logging.basicConfig(
            format="%(asctime)s %(name)s %(levelname)-8s %(message)s",
            datefmt="%Y-%m-%d %Hs:%M:%S",  level=logging.INFO )
logger = logging.getLogger()

class application:

    def __init__(self):
        self.username =os.environ.get('FUNCTIONAL_USER_USERNAME', None)
        self.password =os.environ.get('FUNCTIONAL_USER_PASSWORD', None)

    def gerritClone(self):
        subprocess.call(['sh','automation/gerritClone.sh'])
        logging.info("eiae-helmfile cloned successfully ")
        path = os.getcwd()
        path = path+'/eiae-helmfile/helmfile/build-environment/tags_true.yaml'
        total_apps = []
        with open(path, 'r') as fp:
            total_apps = fp.readlines()
        app = ["EIC", "OSS", "AUTO APPS", "BP"]
        for i in range(2,len(total_apps)):
            appName = total_apps[i].replace(" ","").split(":")[0]
            if appName != 'th':
                if appName == "appmgr":
                    appName = "APP MGR"
                    app.append(appName)
                else:
                    app.append(appName)
        apps = [each_app.upper() for each_app in app]
        os.system("rm -rf eiae-helmfile")
        return apps

    def addDeleteApplications(self, femjobs):
        apps = self.gerritClone()
        logging.info(f"Application List {apps}")
        jobsCsv = femjobs
        csvData = ["jenkinsUrl;jobName;appType;products\n"]
        productList = []
        rem_prod = []
        for i in range(1, len(jobsCsv)):
            products = jobsCsv[i].split(";")[-1].replace("\n","").split(",")
            productList.append(products)
            rem = jobsCsv[i].split(";")[:-1]
            prod = ""
            for each_prod in products:
                if each_prod in apps:
                    prod = prod+","+each_prod
                else:
                    if each_prod not in rem_prod:
                        rem_prod.append(each_prod)
            rem.append(prod[1:])
            jobData = ""
            for i in rem:
                jobData = jobData+";"+i
            if jobData[-1] != ";":
                csvData.append(jobData[1:]+"\n")
        logging.info(f"Removed application from job.csv file\n : {rem_prod}")
        logging.info("Checking for newly added Applications")
        uniqueProduct = set()
        for sublist in productList:
            uniqueProduct.update(sublist)
        finalProducts = list(uniqueProduct)
        addProd = []
        for each_prod in apps:
            if each_prod not in finalProducts:
                addProd.append(each_prod)
        for prod in addProd:
            n = f";;;{prod}\n"
            csvData.append(n)
        logging.info(f"Added application to job.csv file\n: {addProd}")
        flag = self.csvData(csvData)
        return flag

    def csvData(self, csvData):
        path = os.getcwd()
        path = path+'/eric-oss-ms-maturity-visualisation/src/main/resources/config/jobs.csv'
        finalData = []
        for i in csvData:
            if i not in finalData:
                finalData.append(i)
        with open(path,'w') as file:
            for each_data in finalData:
                file.write(each_data)
        return True, finalData
