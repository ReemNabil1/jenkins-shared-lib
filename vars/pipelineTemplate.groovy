def call(Map config) {

    pipeline {
        agent any

        parameters {
            string(name: 'PORT', defaultValue: '9090', description: 'App Port')
        }

        environment {
            IMAGE_NAME     = "${config.imageName}"
            IMAGE_TAG      = "${config.imageTag}"
            CONTAINER_NAME = "${config.containerName}"
            REPO_URL       = "${config.repoUrl}"
            PORT           = "${params.PORT}"
        }

        stages {

            stage('Clean Workspace') {
                steps {
                    sh 'rm -rf target/'
                }
            }

            stage('Build') {
                steps {
                    sh 'mvn clean package -DskipTests'
                    
                    sh 'ls -la target/'
                }
            }

            stage('Test') {
                steps {
                    sh 'mvn test -Dtest=!PostgresIntegrationTests'
                }
            }

            stage('Verify Jar') {
                steps {
                    sh 'test -f target/app.jar && echo "JAR exists"'
                    sh 'ls -lh target/app.jar'
                }
            }

            stage('Docker Build') {
                steps {
                    sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                }
            }

            stage('Push Image') {
                steps {
                    withCredentials([usernamePassword(
                        credentialsId: 'docker-creds',
                        usernameVariable: 'USER',
                        passwordVariable: 'PASS'
                    )]) {
                        sh "docker login -u $USER -p $PASS"
                        sh "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
                    }
                }
            }

            stage('Deploy') {
                steps {
                    sh "docker stop ${CONTAINER_NAME} || true"
                    sh "docker rm ${CONTAINER_NAME} || true"
                    sh "docker run -d --name ${CONTAINER_NAME} -p 9090:8080 ${IMAGE_NAME}:${IMAGE_TAG}"
                }
            }
        }

        post {
            success {
                echo "SUCCESS: ${IMAGE_NAME} deployed on port ${PORT}"
            }
            failure {
                echo "FAILED pipeline execution"
            }
        }
    }
}
