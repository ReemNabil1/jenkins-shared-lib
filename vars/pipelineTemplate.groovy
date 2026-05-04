def call(Map config) {

    pipeline {
        agent any

        parameters {
            string(name: 'PORT', defaultValue: '8080', description: 'Application Port')
        }

        environment {
            IMAGE_NAME     = config.imageName
            IMAGE_TAG      = config.imageTag
            CONTAINER_NAME = config.containerName
            REPO_URL       = config.repoUrl
            PORT           = "${params.PORT}"
        }

        stages {

            stage('Build') {
                steps {
                    sh 'mvn clean package -DskipTests'
                }
            }

            stage('Test') {
                steps {
                    sh 'mvn test'
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
                        sh "echo $PASS | docker login -u $USER --password-stdin"
                        sh "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
                    }
                }
            }

            stage('Deploy') {
                steps {
                    sh """
                        docker stop ${CONTAINER_NAME} || true
                        docker rm ${CONTAINER_NAME} || true
                        docker run -d --name ${CONTAINER_NAME} -p ${PORT}:8080 ${IMAGE_NAME}:${IMAGE_TAG}
                    """
                }
            }
        }

        post {
            success {
                echo "SUCCESS: ${IMAGE_NAME}:${IMAGE_TAG} deployed on port ${PORT}"
            }
            failure {
                echo "FAILED pipeline execution"
            }
        }
    }
}
