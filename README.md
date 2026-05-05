# Jenkins Shared Library

This repository contains a reusable Jenkins pipeline template used to standardize CI/CD workflows across multiple microservices.

---

## Features

* Reusable pipeline logic
* Supports multiple services
* Docker build and push
* Automated deployment
* Dynamic configuration

---

## Usage

Example usage inside a Jenkinsfile:

```groovy
@Library('shared-lib') _

pipelineTemplate(
    repoUrl: 'https://github.com/ReemNabil1/petclinic-service-a',
    imageName: 'reemnabil/service-a',
    imageTag: 'latest',
    containerName: 'service-a-container',
    port: "9090"
)
```

---

## Pipeline Stages

1. Clean Workspace
2. Clone Repository
3. Build (Maven)
4. Test
5. Verify JAR
6. Build Docker Image
7. Push to Docker Hub
8. Deploy Container

---

## Structure

```
vars/
  pipelineTemplate.groovy
```

---

## Benefits

* Reduces duplication across pipelines
* Easy to scale for multiple services
* Centralized pipeline management
* Clean and maintainable CI/CD setup
