#!/usr/bin/env groovy

pipeline {
    agent any
    tools {
        jdk "jdk-21"
    }
    stages {
        stage('Clean') {
            steps {
			    withCredentials([file(credentialsId: 'mod_build_secrets', variable: 'ORG_GRADLE_PROJECT_secretFile')]) {
					echo 'Cleaning Project'
					sh 'chmod +x gradlew'
					sh './gradlew clean'
				}
            }
        }
        stage('Build and Deploy') {
            steps {
				withCredentials([file(credentialsId: 'mod_build_secrets', variable: 'ORG_GRADLE_PROJECT_secretFile')]) {
					echo 'Building and Deploying to Maven'
					sh './gradlew build publish'
				}
			}
        }
        stage('Publish Mod') {
            when { buildingTag() }
            steps {
				withCredentials([file(credentialsId: 'mod_build_secrets', variable: 'ORG_GRADLE_PROJECT_secretFile')]) {
					echo "Publishing tag ${env.TAG_NAME} to CurseForge and Modrinth"
					sh './gradlew publishMod --no-configuration-cache'
				}
			}
        }
    }
    post {
        always {
            archive 'common/build/libs/**.jar, fabric/build/libs/**.jar, forge/build/libs/**.jar, neoforge/build/libs/**.jar'
        }
    }
}
