#!/usr/bin/env groovy

def bob = "bob/bob -r \${WORKSPACE}/deployOnCluster/install.yaml"
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
        stage('Config file provider') {
            steps {
			    configFileProvider([configFile(fileId: "${K8S_CLUSTER_ID}", targetLocation: "${env.KUBECONFIG}")]) {}
            }
        }
        stage('Create docker-hub secret for access to Dockerhub') {
            steps {
                sh "${bob} create-docker-hub-secret"
            }
        }
        stage('Create secret with Jenkins for app access to log data from pipelines') {
            steps {
                sh "${bob} create-jenkins-secret"
            }
        }
        stage('Run Helm Upgrade') {
            steps {
                sh "${bob} run-helm-upgrade"
            }
        }
        stage('Setup Ingress') {
            steps {
                sh "${bob} setup-ingress"
            }
        }
    }
}

