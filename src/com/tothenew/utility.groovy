package com.tothenew

//notifier........................................................................................................
def notifier(){
   wrap([$class: 'BuildUser']) {
     stage('sending notification'){
        currentBuild.displayName = "#" + currentBuild.number + ", Started by ${env.BUILD_USER}" + ", from Branch: ${env.branch_name}"
        env.use=env.BUILD_USER
         sh 'echo $use'
         //notifyStarted()
         }
       }
   }


//git clone............................................................................................................
def Git_clone(String git_url,String credential_git){ 
withCredentials([sshUserPrivateKey(credentialsId: "${credential_git}", keyFileVariable: 'SSH_KEY')]) {
                    sh """
                        eval `ssh-agent`
                        ssh-add $SSH_KEY
                        ssh-keyscan github.com >> ~/.ssh/known_hosts
                        git clone "${git_url}" .
                    """
                }
  
}

//Gain access from aws...........................................................................................................................
 def Gaining_access(){
       sh '`aws sts assume-role --role-arn arn:aws:iam::388606509852:role/Shared-Jenkins-Role --role-session-name nitin > op`'
      env.AWS_SECRET_ACCESS_KEY = sh(script:'cat op | grep SecretAccessKey | awk \'{print $2}\' | sed -s \'s/"//g; s/,//g\'', returnStdout: true).trim()
      env.AWS_ACCESS_KEY_ID = sh(script:'cat op | grep AccessKeyId | awk \'{print $2}\' | sed -s \'s/"//g; s/,//g\'', returnStdout: true).trim()
      env.AWS_SESSION_TOKEN = sh(script:'cat op | grep SessionToken | awk \'{print $2}\' | sed -s \'s/"//g; s/,//g\'', returnStdout: true).trim()
 }


//ECR LOGIN......................................................................................................................................
def Ecr_login(String region,String docker_registry){
 sh """
   
   aws ecr get-login-password --region "${region}"| docker login --username AWS --password-stdin "${docker_registry}"

"""
}



//BUILD IMAGE.........................................................................................................................................
def Build_Docker_Image(String docker_repo){
 sh """
 sudo docker build -t $docker_repo .
 
 """
}

//PUSH IMAGE TO ECR........................................................................................................................................
def Push_Image_to_ECR(String docker_repo,String docker_registry){
sh """
docker tag $docker_repo:$tag $docker_repo:$tag
docker push $docker_repo:$tag

"""
}
//IMAGE CLEANUP............................................................................................................................................
def Image_cleanup(String docker_repo,String tag){
 sh """ 
  echo removing $docker_repo and $docker_repo:$tag'
   docker rmi $docker_repo $docker_repo:$tag'
 sh"""
}

//update helm...............................................................................................................................................
 def Helm_update(String devops_git_url,String service_name){
  env.new_docker_image=docker_repo+":"+env.tag
      service = split
      env.new_docker_image=docker_repo+":"+env.tag
       sh """

          rm -rf *
          
          git clone -b "$devops_branch_name" "$devops_git_url"
          cd devops
          sed -E -i 's/($service_name:).*/$service_name:$tag/' 'service/values.yaml'
          git add *
          git commit -m "adding"
          git push origin kubernetes    
          
          """
 }
