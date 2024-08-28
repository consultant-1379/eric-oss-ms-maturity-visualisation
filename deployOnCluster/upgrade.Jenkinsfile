#!/usr/bin/env groovy

def bob = "bob/bob -r \${WORKSPACE}/deployOnCluster/upgrade.yaml"
def K8S_CLUSTER_ID= "hahn130"
def RESOURCE_NAME= "eric-oss-ms-maturity-visualisation"
pipeline {
    agent {
        label SLAVE
    }

    environment {
        HELM_CHART_REPO_CREDS = credentials('mmt-baseline-credentials')
        HELM_CHART_REPO_USER = "${HELM_CHART_REPO_CREDS_USR}"
        HELM_CHART_REPO_KEY = "${HELM_CHART_REPO_CREDS_PSW}"
        KUBECONFIG = "${WORKSPACE}/.kube/config"
    }

    stages {
        stage('Cleaning Git Repo') {
            steps {
                sh 'git clean -xdff'
                sh 'git submodule sync'
                sh 'git submodule update --init --recursive'
            }
        }
        stage('Run Helm Upgrade') {
            steps {
		        echo "Inject kubernetes config file (${K8S_CLUSTER_ID}) based on the Lockable Resource name: ${RESOURCE_NAME}"
                configFileProvider([configFile(fileId: "${K8S_CLUSTER_ID}", targetLocation: "${env.KUBECONFIG}")]) {}
                sh "${bob} run-helm-upgrade"
            }
        }
    }
}

