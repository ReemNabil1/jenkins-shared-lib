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
            PORT           = params.PORT
        }

        stages {

            stage('Clone Repository') {
                steps {
                    echo "Cloning repo..."
                    git url: REPO_URL, branch: 'main'
                }
            }

            stage('Build') {
                steps {
                    echo "Building project..."
                    sh 'mvn clean package'
                }
            }

            stage('Test') {
                steps {
                    echo "Running tests..."
                    sh 'mvn test'
                }
            }

            stage('Docker Build') {
                steps {
                    echo "Building Docker image..."
                    sh "docker build -t $IMAGE_NAME:$IMAGE_TAG ."
                }
            }

            stage('Docker Login & Push') {
                steps {
                    withCredentials([usernamePassword(
                        credentialsId: 'docker-creds',
                        usernameVariable: 'USER',
                        passwordVariable: 'PASS'
                    )]) {
                        sh "echo $PASS | docker login -u $USER --password-stdin"
                        sh "docker push $IMAGE_NAME:$IMAGE_TAG"
                    }
                }
            }

            stage('Deploy') {
                steps {
                    echo "Deploying container..."

                    sh """
                        docker stop $CONTAINER_NAME || true
                        docker rm $CONTAINER_NAME || true
                        docker run -d --name $CONTAINER_NAME -p $PORT:8080 $IMAGE_NAME:$IMAGE_TAG
                    """
                }
            }
        }

        post {
            success {
                echo "✅ Pipeline SUCCESS - $IMAGE_NAME deployed on port $PORT"
            }

            failure {
                echo "❌ Pipeline FAILED - check logs"
            }

            always {
                echo "🚀 Pipeline finished"
            }
        }
    }
}
