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

import subprocess
from updatecsv import updatecsv
from datetime import date
import logging, os
from deletecsv import deletecsv
from addDeleteApp import application
LOG=logging.getLogger(__name__)
LOG.setLevel(logging.DEBUG)
'''
this is the main function where execution starts
'''
def jobCsvData():
    path=os.getcwd()
    path = path+'/eric-oss-ms-maturity-visualisation/src/main/resources/config/jobs.csv'
    femjobs = []
    with open(path, 'r') as fp:
        femjobs = fp.readlines()
    return femjobs

if __name__ == '__main__':
    '''
    this subprocess will clone the repo
    '''
    subprocess.call(['sh','automation/gerrittest.sh'])

    update= updatecsv()
    data = update.read_fems()
    flag = False
    for line in data:
       print(line)
       flag = update.fetch_job_created(line,flag)

    csvdata = jobCsvData()
    delete = deletecsv()
    data = delete.read_fems()
    for line in data:
        delete.fetch_job_deleted(line, csvdata)

    deletedData = jobCsvData()
    appDel = application()
    finalflag, finalData = appDel.addDeleteApplications(deletedData)
    if finalflag == True or flag == True :
        '''
        this subprocess will push the deleted job file to gerrit repo
        '''
        subprocess.call(['sh','automation/push.sh'])
    else :
        LOG.info("No new jobs are created on "+str(date.today()))



