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

from ast import parse
import subprocess
import logging,sys,json,os,operator,requests,time
from urllib import response
from datetime import datetime, timedelta,date
import csv


logging.basicConfig(
            format="%(asctime)s %(name)s %(levelname)-8s %(message)s",
            datefmt="%Y-%m-%d %Hs:%M:%S",  level=logging.INFO )
logger = logging.getLogger()


class deletecsv:
    def __init__(self):
        self.username =os.environ.get('FUNCTIONAL_USER_USERNAME', None)
        self.password =os.environ.get('FUNCTIONAL_USER_PASSWORD', None)
    def read_fems(self):
        '''
    this function read the fem links from input file
        '''
        try:
            femlinks=open('automation/fem-links.csv','r')
            femlinks=femlinks.readlines()
            return femlinks
        except IOError as ie:
            raise IOError from ie
    def fetch_job_deleted(self,line,csvData):
        '''
    This function will fetch jobs deleted on particular day
    :return:flag
        '''
        try:
            line=line.strip()
            url='https://'+self.username+':'+self.password+'@'+line+'/jenkins/jobConfigHistory/api/json/?filter=deleted'
            response_API = requests.get(url)
            response = response_API.text
            parse_json = json.loads(response)
        except requests.exceptions.HTTPError as errh:
            logging.error(errh)
        except requests.exceptions.RequestException as err:
            logging.error(err)
        today = date.today()
        today=datetime.strftime(today , '%Y-%m-%d')
        job_list = []
        try:
            for itr in parse_json['configs']:
                dat = itr['date']
                dat =dat.split('_')[0]
                if (dat == today):
                    job_name = itr['job']
                    if(job_name.__contains__("_PreCodeReview") or job_name.__contains__("_Publish") or job_name.__contains__("Publish_Hybrid") or
                       job_name.__contains__("_release") or job_name.__contains__("_Release")):
                        name = job_name.split("_")
                        index = name.index("deleted")
                        new_string = '_'.join(name[:index+1])
                        job_list.append(new_string.split("_deleted")[0])
        except KeyError as keyerr:
            logging.error(f"There are no Job's available to delete")
        if(len(job_list) < 1):
            logging.info("no jobs available to remove from job.csv")
        else:
            femjobs = csvData
            deleted_jobs = []
            for i in job_list:
                for j in femjobs:
                    if j.__contains__(i) and j.__contains__(line):
                        jName = str(j).split(";")[1]
                        if jName == i:
                            deleted_jobs.append(j)
                            femjobs.remove(j)
            logging.info("below jobs are deleted from jobs.csv file")
            for i in deleted_jobs:
                logging.info(i)
            path=os.getcwd()
            path = path+'/eric-oss-ms-maturity-visualisation/src/main/resources/config/jobs.csv'
            with open(path,'w') as file:
                for jobcsv in femjobs:
                    file.write(jobcsv)
