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
import ruamel.yaml
import subprocess
from rules_automation import AddRules


class AddStages:

    def __init__(self):
        logging.basicConfig()
        self.LOG = logging.getLogger(__name__)
        self.LOG.setLevel(logging.DEBUG)

    def orderStages(self, pipelineType):
        rs = AddRules()
        jenkinStages = rs.getJenkinsData(pipelineType)
        yamlStages = rs.getYamlData(pipelineType)
        for i in yamlStages:
            if i not in jenkinStages:
                nextStageIndex = yamlStages.index(i)+1
                if len(yamlStages) != nextStageIndex:
                    nextStageName = yamlStages[nextStageIndex]
                    if nextStageName in jenkinStages:
                        jenkinStageIndex = jenkinStages.index(nextStageName)
                        jenkinStages.insert(jenkinStageIndex, i)
                    else:
                        preStageIndex = yamlStages.index(i)-1
                        preStageName = yamlStages[preStageIndex]
                        jenkinStageIndex = jenkinStages.index(preStageName)
                        jenkinStages.insert(jenkinStageIndex+1, i)
                else:
                    jenkinStages.append(i)

        unwantedStages = []
        allRules = rs.allPcrPubRules()
        if len(allRules) != 0:
            for key in allRules:
                if allRules[key] == []:
                    unwantedStages.append(key)
        if len(unwantedStages) != 0:
            self.LOG.info(f"New {pipelineType} stages which doesn't have the bob rules\n {unwantedStages}")
        
        # now get the stage list which are added newly and having rules
        result1 = list(set(jenkinStages) - set(unwantedStages))
        finalResult = list(set(result1) - set(yamlStages))
        if len(finalResult)!= 0:
            self.LOG.info(f"New {pipelineType} stages which are having rules and should be added in stage.yaml file:\n {finalResult}")
        
        wantedStages = []
        if len(finalResult) != 0:
            for eachStage in jenkinStages:
                if eachStage not in unwantedStages:
                    wantedStages.append(eachStage)
            self.LOG.info(f"Order of {pipelineType} stages that should be in stage.yaml file:\n {wantedStages}")

        

        return wantedStages, yamlStages, finalResult

    def addStages(self):
        yaml_file_path = os.path.join(os.getcwd(), 'eric-oss-ms-maturity-visualisation/src/main/resources/config/stages.yaml')
        yaml = ruamel.yaml.YAML()

        with open(yaml_file_path, 'r') as file:
            data = yaml.load(file)

        pubList = []
        pcrList = []
        for i in ["publish", "pcr"]:
            if i == "publish":
                wantedStages, yamlStages, finalResult = self.orderStages(i)
                publish_list = data['stages.standards'][0]['publish']
                pubList = finalResult
            elif i == "pcr":
                wantedStages, yamlStages, finalResult = self.orderStages(i)
                publish_list = data['stages.standards'][0]['preCodeReview']
                pcrList = finalResult
            if len(finalResult) != 0:
                for stage in wantedStages:
                    if stage not in yamlStages:
                        stageIndex = wantedStages.index(stage)
                        self.LOG.info(f"stage : {stage}, index : {stageIndex}")
                        if ':' in stage:
                            rule = str(stage).lower().replace(":"," ").replace(" ", "-")
                        else:
                            rule = str(stage).lower().replace(" ", "-")
                        addData = {'name': f'{stage}', 'rules': f'rules-{rule}'}
                        publish_list.insert(stageIndex, addData)
                yaml_string = ruamel.yaml.round_trip_dump(data)
                with open(yaml_file_path, 'w') as file:
                    file.write(yaml_string)
            else:
                self.LOG.info("No new stages to be added")

        self.LOG.info(f"Below stages added newly in publish jenkins file: \n {pubList}")
        self.LOG.info(f"Below stages added newly in PCR jenkins file: \n {pcrList}")
        common_values = list(set(pubList + pcrList))
        uniqueStage = list(set(pubList) ^ set(pcrList))
        self.rulesFormat(common_values, uniqueStage)

    def rulesFormat(self, commonValues, uniqueStage):
        yaml_file_path = os.path.join(os.getcwd(), 'eric-oss-ms-maturity-visualisation/src/main/resources/config/stages.yaml')
        yaml = ruamel.yaml.YAML()
        with open(yaml_file_path, 'r') as file:
            data = yaml.load(file)
        yamlKeys = []
        for i in data:
            yamlKeys.append(i)
        yaml_string = ruamel.yaml.round_trip_dump(data)
        for stage in commonValues:
            if ':' in stage:
                rule = str(stage).lower().replace(":"," ").replace(" ", "-")
            else:
                rule = str(stage).lower().replace(" ", "-")
            if stage not in uniqueStage:
                if f'rules-{rule}' in yamlKeys:
                    yaml_string = str(yaml_string).replace(f"rules: rules-{rule}", f'rules: *rules-{rule}\n').replace(f"rules-{rule}:",f"rules-{rule}: &rules-{rule}")
            else:
                if f'rules-{rule}' in yamlKeys:
                    yaml_string = str(yaml_string).replace(f"rules: rules-{rule}", f'rules: *rules-{rule}\n').replace(f"rules-{rule}:",f"rules-{rule}: &rules-{rule}")
        with open(yaml_file_path, 'w') as file:
            file.write(yaml_string)

    def taskFormating(self):
        yaml_file_path = os.path.join(os.getcwd(), 'eric-oss-ms-maturity-visualisation/src/main/resources/config/stages.yaml')
        yaml = ruamel.yaml.YAML()
        with open(yaml_file_path, 'r') as file:
            data = yaml.load(file)
        yaml_string = ruamel.yaml.round_trip_dump(data)
        yaml_string = str(yaml_string).replace("'\n\n- 'Bob Task:", "'\n- 'Bob Task:")
        yaml_string = str(yaml_string).replace("'\n\n- 'Bob Rule:", "'\n- 'Bob Rule:")
        yaml_string = str(yaml_string).replace("\n\n","\n")
        yaml_string = str(yaml_string).replace("'\nstages.standards","'\n\nstages.standards").replace("rules-clean: &rules-clean","\nrules-clean: &rules-clean")
        yaml_string = str(yaml_string).replace("'\nrules-", "'\n\nrules-").replace("\n  - name:", "\n\n  - name:").replace("# File to configure ","\n# File to configure ")

        
        with open(yaml_file_path, 'w') as file:
            file.write(yaml_string)

if __name__ == "__main__":
    stage = AddStages()
    stage.addStages()
    stage.taskFormating()
    '''
    this subprocess will push the stage.yaml to oss-ms-maturity-visualisation
    '''
    subprocess.call(['sh','eric-oss-ms-maturity-visualisation/automation/golden_stages/pushYaml.sh'])
