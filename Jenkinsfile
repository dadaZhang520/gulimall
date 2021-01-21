pipeline {
  agent {
    node {
      label 'maven'
    }
  }

  stages {
    stage('拉取代码') {
      steps {
        git(url: 'https://gitee.com/dadazhang520/gulimall.git', credentialsId: 'gitee-id', branch: 'master', changelog: true, poll: false)
        sh 'echo $PROJECT_NAME'
        container ('maven') {
          sh "mvn clean install -Dmaven.test.skip=true -gs `pwd`/mvn-settings.xml"
        }
      }
    }

    stage ('构建镜像 & 推送最新镜像') {
      steps {
        container ('maven') {
         sh 'mvn  -Dmaven.test.skip=true -gs `pwd`/mvn-settings.xml clean package'
         sh 'cd $PROJECT_NAME && docker build -f Dockerfile -t $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER .'
         withCredentials([usernamePassword(passwordVariable: 'DOCKER_PASSWORD',usernameVariable: 'DOCKER_USERNAME' ,credentialsId : "$DOCKER_CREDENTIAL_ID",)]) {
           sh 'echo "$DOCKER_PASSWORD" | docker login $REGISTRY -u "$DOCKER_USERNAME" --password-stdin'
           sh 'docker tag $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest'
           sh 'docker push $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest'
          }
        }
      }
    }

    stage('项目部署到k8s') {
      steps {
        input(id: '项目部署', message: '是否确定部署?')
        kubernetesDeploy(configs: "$PROJECT_NAME/deploy/**", enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
      }
    }

    stage('发布版本到gitee'){
      steps {
        container ('maven') {
          input(id: 'release-image-with-tag', message: '是否发布版本?')
          withCredentials([usernamePassword(credentialsId: "$GITEE_CREDENTIAL_ID", passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
            sh 'git config --global user.email "2835141003@qq.com" '
            sh 'git config --global user.name "dadazhang520" '
            sh 'git tag -a $PROJECT_VERSION -m "$PROJECT_VERSION" '
            sh 'git push http://$GIT_USERNAME:$GIT_PASSWORD@gitee.com/$GIT_ACCOUNT/gulimall.git --tags --ipv4'
          }
          sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSION '
          sh 'docker push $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSION '
        }
      }
    }
  }

  parameters {
    string(name:'PROJECT_NAME',defaultValue: '',description:'')
    string(name:'PROJECT_VERSION',defaultValue:'',description:'')
  }

  environment {
    DOCKER_CREDENTIAL_ID = 'dockerhub-id'
    GITEE_CREDENTIAL_ID = 'gitee-id'
    KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
    REGISTRY = 'docker.io'
    DOCKERHUB_NAMESPACE = 'dadazhang520'
    GIT_ACCOUNT = 'dadazhang520'
    SONAR_CREDENTIAL_ID='sonar-token'
    BRANCH_NAME='master'
  }
}