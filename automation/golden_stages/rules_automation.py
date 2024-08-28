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
import re
import yaml
import logging
import ruamel.yaml
import subprocess

class AddRules:

    def __init__(self):
        logging.basicConfig()
        self.LOG = logging.getLogger(__name__)
        self.LOG.setLevel(logging.DEBUG)

    def getJenkinsData(self, pipelineType):
        if pipelineType  == "publish":
            path = f'{os.getcwd()}/eric-oss-ms-maturity-visualisation/oss-common-ci-utils/dsl/jenkinsFiles/common_publish.Jenkinsfile'
        elif pipelineType == "pcr":
            path = f'{os.getcwd()}/eric-oss-ms-maturity-visualisation/oss-common-ci-utils/dsl/jenkinsFiles/common_precodereview.Jenkinsfile'
        with open(path, 'r') as file:
            jenkinsfile_content = file.read()
        stage_names = re.findall(r"stage\('(.*?)'\)", jenkinsfile_content)
        return stage_names

    def getYamlData(self, pipelineType):
        yaml_file_path = f'{os.getcwd()}/eric-oss-ms-maturity-visualisation/src/main/resources/config/stages.yaml'
        with open(yaml_file_path, 'r') as file:
            yaml_content = file.read()
        data = yaml.safe_load(yaml_content)
        if pipelineType == "publish":
            names = [item['name'] for item in data['stages.standards'][0]['publish']]
        elif pipelineType == "pcr":
            names = [item['name'] for item in data['stages.standards'][0]['preCodeReview']]
        return names

    def newStages(self, pipelineType):
        jenkinStages = self.getJenkinsData(pipelineType)
        yamlStages = self.getYamlData(pipelineType)
        newStageList = []
        for stage in jenkinStages:
            if stage not in yamlStages:
                newStageList.append(stage)
        self.LOG.info(f"new {pipelineType} stages:{newStageList}")
        return newStageList

    def getBobRules(self, pipelineType):
        if pipelineType == "publish":
            file_path = f'{os.getcwd()}/eric-oss-ms-maturity-visualisation/oss-common-ci-utils/dsl/jenkinsFiles/common_publish.Jenkinsfile'
        elif pipelineType == "pcr":
            file_path = f'{os.getcwd()}/eric-oss-ms-maturity-visualisation/oss-common-ci-utils/dsl/jenkinsFiles/common_precodereview.Jenkinsfile'
        
        with open(file_path, "r") as file:
            jenkins_script = file.read()
        
        lines = jenkins_script.split('\n')
        current_stage = None
        stage_commands = {}
        stage_regex = re.compile(r'\s*stage\((["\'])(.*?)\1\)\s*{')
        sh_regex = re.compile(r'\s*sh\s+["\'](.*?)["\']')
        retry_mechanism_regex = re.compile(r'\s*ci_pipeline_scripts.retryMechanism\(["\'](.*?)["\']')
        
        inside_post = False
        for line in lines:
            stage_match = stage_regex.match(line)
            
            if stage_match:
                current_stage = stage_match.group(2)
                stage_commands[current_stage] = []
                inside_post = False  # Reset the inside_post flag for each new stage
            elif current_stage is not None and not inside_post:
                sh_match = sh_regex.match(line)
                retry_match = retry_mechanism_regex.match(line)
                
                if "post" in line:
                    inside_post = True
                
                if sh_match:
                    sh_command = sh_match.group(1)
                    if '${bob}' in sh_command:
                        if sh_command.split(" ")[1][0] != "-":
                            stage_commands[current_stage].append(f'{sh_command}"')
                elif retry_match:
                    retry_command = retry_match.group(1)
                    stage_commands[current_stage].append(f'{retry_command}"')
        
        self.LOG.info(f"{pipelineType} bob rules: \n{stage_commands}")
        return stage_commands

    def allPcrPubRules(self):
        publishRules = self.getBobRules("publish")
        pcrRules = self.getBobRules("pcr")
        pubNewStages = self.newStages("publish")
        pcrNewStages = self.newStages("pcr")

        if len(pubNewStages) != 0 or len(pcrNewStages) != 0:
            # combine pubNewStages and pcrNewStages to get unique stages list
            combinedStages = list(set(pubNewStages + pcrNewStages))
            self.LOG.info(f"\n\ncombination of both new publish and pcr stages:\n {combinedStages}")
            # update publishRules dictionary with only the key-value pairs that exist in lst   
            publishRules = {key: value for key, value in publishRules.items() if key in pubNewStages}
            # update pcrRules dictionary with only the key-value pairs that exist in lst   
            pcrRules = {key: value for key, value in pcrRules.items() if key in pcrNewStages}
            # combine publishRules and pcrRules dictionaries to get unique key with combined values
            overallRules = {}
            for key in publishRules.keys() | pcrRules.keys():
                # Use set union to ensure all unique values are included
                combined_values = list(set(publishRules.get(key, []) + pcrRules.get(key, [])))
                overallRules[key] = combined_values
        else:
            self.LOG.info("No new stages are added")
        self.LOG.info(f"Combinatin of pcr & publish Bob Rules :\n {overallRules}")
        return overallRules

    def wantedBobRules(self):
        overallRules = self.allPcrPubRules()
        # Create a new dictionary by getting data from output dictionary for keys that not have empty list values
        avilableRules = {key: value for key, value in overallRules.items() if value}
        
        bobRules = {}
        for key, values in avilableRules.items():
            yaml_key = f'rules-{key.lower().replace(" ", "-")}:'
            yaml_values = []
            for value in values:
                rule_name = value.split(" ")[1]
                rule_name = rule_name[:-1] if rule_name.endswith('"') else rule_name
                yaml_values.append(f'Bob Rule: {rule_name}')
            bobRules[yaml_key] = yaml_values
        self.LOG.info(f"Bob rules which need to be updated in stage.yaml file:\n {bobRules}")
        return bobRules
    
    def addBobRules(self):
        bobRules = self.wantedBobRules()
        yaml_file_path = os.path.join(os.getcwd(), 'eric-oss-ms-maturity-visualisation/src/main/resources/config/stages.yaml')
        yaml = ruamel.yaml.YAML()
        with open(yaml_file_path, 'r') as file:
            data = yaml.load(file)
        # get existing rules from stage.yaml file
        dataRules= []
        for rule in data:
            dataRules.append(rule)
        # remove rules from bobRules which are already present in yaml file
        # For this first get list of rules which are already exists to delete
        delUnwantedRules = []
        for key in bobRules:
            rule = str(key).split(':')[0]
            if rule in dataRules:
                delUnwantedRules.append(key)
        # Now delte existed rules from bobRules dictionary
        # after deleting, the bobRules dictionary having only wanted rules which need to be updated in yaml file
        for rule in delUnwantedRules:
            if rule in bobRules:
                del bobRules[rule]

        if len(bobRules) != 0:
            with open(yaml_file_path, 'r') as file:
                original_data = file.read()
                lines = original_data.split('\n')[:58]
                data_to_store = '\n'.join(lines)
            list1 = [(k, v) for k, v in data.items()]
            # Find the index of 'kar'
            index = next(i for i, kv in enumerate(list1) if kv[0] == 'stages.standards')
            # Insert dic2 items into list1 at the right position
            list1[index:index] = bobRules.items()
            # Convert list1 back to a dictionary
            data = dict(list1)
            yaml_string = ruamel.yaml.round_trip_dump(data)
            for key in bobRules:
                if key in yaml_string:
                    yaml_string = str(yaml_string).replace(f"'{key}':",f'\n{key}')
            with open(yaml_file_path, 'w') as file:
                    file.write(data_to_store+"\n")
            with open(yaml_file_path, 'a') as file:
                    file.write(yaml_string)
        else:
            self.LOG.info("There is no rules to be added")

if __name__ == "__main__":
    '''
    this subprocess will clone the repo
    '''
    subprocess.call(['sh','automation/golden_stages/gerritClone.sh'])
    rules = AddRules()
    rules.addBobRules()