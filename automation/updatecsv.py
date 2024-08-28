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
LOG=logging.getLogger(__name__)
class updatecsv:
    def __init__(self):
        logging.basicConfig()
        self.LOG = logging.getLogger(__name__)
        self.LOG.setLevel(logging.DEBUG)
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

    def fetch_job_created(self,line,flag):
        '''
    This function will fetch jobs created on particular day
    :return:flag
        '''
        try:
            line=line.strip()
            url='https://'+self.username+':'+self.password+'@'+line+'/jenkins/jobConfigHistory/api/json/?filter=created'
            response_API = requests.get(url)
            response = response_API.text
            parse_json = json.loads(response)
        except requests.exceptions.HTTPError as errh:
            self.LOG.error(errh)
        except requests.exceptions.RequestException as err:
            self.LOG.error(err)
        today = date.today()
        today=datetime.strftime(today , '%Y-%m-%d')
        try:
            for itr in parse_json['configs']:
                dat = itr['date']
                dat =dat.split('_')[0]
                if (dat == today):
                    job_name = itr['job']
                    if job_name.__contains__("_PreCodeReview") or job_name.__contains__("_Publish") or job_name.__contains__("_Release") :
                        self.LOG.info(job_name)
                        flag=self.fetch_attributes(job_name,line,dat,today,flag)
        except KeyError as keyerr:
            self.LOG.error(f"There are no Job's created")
        return flag
    def fetch_attributes(self,job_name,line,dat,today,flag):
        '''
        This function will fetch required attributes like fem link , tech stack, product name
        :return: flag
        '''
        try:
            url='https://'+self.username+':'+self.password+'@'+line+'/jenkins/api/json?tree=jobs[name,color,description,url]'
            response = requests.get(url)
            data = response.text
            parse_json = json.loads(data)
        except requests.exceptions.HTTPError as errh:
            self.LOG.error(errh)
        except requests.exceptions.RequestException as err:
            self.LOG.error(err)
        if "description" in str(parse_json):
            try:
                for itr in parse_json['jobs']:
                    desc =itr['description']
                    name = itr['name']
                    techstack=["java","python","ui","c++","c","golang","javascript"]
                    products=["EO","ADC","DMM","PF","TA","APP MGR","EIAP","CH","UDS","OSS","SO","EAS","AUTO APPS","BP"]
                    if name == job_name and desc !=" " :
                        desc=desc
                        ur=itr['url']
                        ur=ur.split('/job')[0]
                        tech= desc.split(' ')[0]
                        tech=tech.lower()
                        if tech in techstack:
                            product=desc.split('\n')[1]
                            product=product.split('<br>')[0]
                            product=product.split(':')[1]
                            product=product.upper()
                            product=product.lstrip()
                            product=product.replace(", ",",")
                            prod=product
                            prod=prod.split(',' or '\n')[0]
                            if prod in products:
                                if prod == "EIAP":
                                    product = "EIC"
                                pro=ur+'/;'+name+';'+tech+';'+product
                                LOG.info(pro)
                                flag=self.push(pro,flag)
                            else:
                                self.LOG.error(name+" does not contain product details")
                        else:
                            self.LOG.error(name+" does not contain tech stack")
            except KeyError as keyerr:
                self.LOG.error(f"There are no Job's created")
            except IndexError as err:
                self.LOG.error(f"description is not there for this job:"+name)
            return flag
        else:
            self.LOG.info(f"Job does not contain job description")

    def push(self,pro,flag):
        '''
        This function will append the new records to  jobs.csv file
        :return: flag
        '''
        try:
            path=os.getcwd()
            file = open(path+'/eric-oss-ms-maturity-visualisation/src/main/resources/config/jobs.csv', 'r')
            reader = csv.reader(file)
            key = pro
            key ="['"+key+";']"
            file = open(path+'/eric-oss-ms-maturity-visualisation/src/main/resources/config/jobs.csv', 'a')
            file.write("\n"+str(pro))
            flag = True
            file.close()
            return flag
        except FileNotFoundError as e:
            self.LOG.error(e)

