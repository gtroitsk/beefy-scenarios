pipeline {
    agent any
    tools {
        // maven 'maven-3.5.4'
        jdk 'graalvm'
    }
    options {
        timestamps()
        ansiColor("xterm")
    }
    stages {
        stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    which java
                    ## echo "M2_HOME = ${M2_HOME}"
                    ## which mvn
                ''' 
            }
        }
        stage('Build') {
            steps {
                sh './mvnw -B clean install -DskipTests -DskipITs'
            }
        }
        stage('Test JVM') {
            steps {
                sh './mvnw -B clean verify'
            }
            post {
                always {
                    junit '**/target/surefire-reports/TEST*.xml'
                }
            }
        }
        stage('Test Native') {
            steps {
                sh 'GRAALVM_HOME=$JAVA_HOME ./mvnw -B clean verify -Dnative'
            }
            post {
                always {
                    junit '**/target/failsafe-reports/TEST*.xml'
                }
            }
        }
        stage('Results') {
            steps {
                sh 'du -cskh */target/* | grep -E "target/scenario|target/lib"'
                archiveArtifacts artifacts: '**/target/*-reports/TEST*.xml', fingerprint: false
            }
        }
    }
    post {
        always {
            deleteDir()
        }
    }
}