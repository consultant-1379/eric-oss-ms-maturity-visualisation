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
import yaml
import logging
import ruamel.yaml
from rules_automation import AddRules

class AddTasks:

    def __init__(self):
        logging.basicConfig()
        self.LOG = logging.getLogger(__name__)
        self.LOG.setLevel(logging.DEBUG)

    def getTasks(self):
        ru = AddRules()
        bobRules = ru.wantedBobRules()
        file_path = f'{os.getcwd()}/eric-oss-ms-maturity-visualisation/oss-common-ci-utils/dsl/rulesetFiles/common_ruleset2.0.yaml'
        with open(file_path, 'r') as file:
            data = yaml.safe_load(file)
        for bobrule in bobRules:
            for eachRule in bobRules[bobrule]:
                if "Bob Rule:" in eachRule:
                    stageName = str(eachRule).split("Rule: ")[1]
                    if ":" in stageName:
                        stageName = stageName.split(":")[0]
                    if stageName in data['rules']:
                        for i in data['rules'][stageName]:
                            if 'task' in i:
                                newTask = f'Bob Task: {stageName}:{i["task"]}'
                                if newTask not in bobRules[bobrule]:
                                    bobRules[bobrule].append(newTask)
                            if 'rule' in i:
                                ruleStage = i['rule']
                                self.subTasks(ruleStage, data, bobRules[bobrule])
        self.LOG.info(f"Tasks which are added under Bob rules:\n {bobRules}")
        return bobRules
        
    def addTasks(self):
        bobRules = self.getTasks()
        yaml_file_path = os.path.join(os.getcwd(), 'eric-oss-ms-maturity-visualisation/src/main/resources/config/stages.yaml')
        yaml = ruamel.yaml.YAML()
        with open(yaml_file_path, 'r') as file:
            data = yaml.load(file)
        existRules = []
        for i in data:
            existRules.append(i)
        for rule in bobRules:
            exrule = str(rule).split(":")[0]
            if exrule in existRules:
                for i in bobRules[rule]:
                    if i not in data[exrule]:
                        data[exrule].append(i)
        yaml_string = ruamel.yaml.round_trip_dump(data)
        with open(yaml_file_path, 'w') as file:
            file.write(yaml_string)

    def subTasks(self, ruleStage, data, task_list):
        if ruleStage in data['rules']:
            for i in data['rules'][ruleStage]:
                if 'task' in i:
                    task_list.append(f'Bob Task: {ruleStage}:{i["task"]}')
                if 'rule' in i:
                    stage = i['rule']
                    self.subTasks(stage, data, task_list)
        return task_list



if __name__ == "__main__":
    task = AddTasks()
    task.addTasks()
