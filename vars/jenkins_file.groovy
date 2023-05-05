import com.tothenew.utility



def call(String region){
myObject = new utility()

pipeline{
    agent{
        label 'slave2'
    }
   
    environment{
      git_url='git@gitlab.intelligrape.net:bharti-axa/${service_name}.git'
      docker_registry = "388606509852.dkr.ecr.ap-south-1.amazonaws.com"
      docker_repo=  "${docker_registry}"+"/"+"${service_name}"
      devops_git_url='git@gitlab.intelligrape.net:bharti-axa/devops.git'
      //credential_git='jenkin-slave-git'
      
      //For Sonarqube use
      APPLICATION="${JOB_BASE_NAME}"
      
      
      region="ap-south-1"
      
      
    }
    
    
   
    stages{
      //notifier stage.........................
      stage('notifier'){
        steps{
            script{
                 myObject.Notifier()
                 }
           }
        }
    
    //clean workspace .........................
     stage('Clean WorkSpace'){
      steps{
         sh 'rm -rf *'
         sh 'rm -rf .git'
       }
     }
     
     
     //Git clone image...........................
     stage('Git Clone') { 
      // Get some code from a GitHub repository
       steps{
         script{
           git branch: "${branch}", url: git_url 
         //myObject.Git_clone("${git_url}","${credential_git}")
           }
         }
      }


     //Access gain from aws.............................

    stage('Gaining Access for deployment') {
      steps{
       script{
           myObject.Gaining_access()
            }
         }
    }
    
    //ECR LOgin....................................
    
  stage('ECR Login'){
      steps{
      script{
          myObject.Ecr_login("${region}","${docker_registry}")
          }
       } 
    }
    
    
    //Build Docker image.........
   
   stage('Build Docker Image') {
      steps{
      script{
          myObject.Build_Docker_Image("${docker_repo}")
        }
      }
   }
  
  
  
  //Push imahe to ECR....................................

  stage('Push Image to ECR'){
      steps{
        script{
       
           myObject.Push_Image_to_ECR("${docker_repo}","${docker_registry}")
         }
       }
  }
  
  //Image CLeanup...............................
   
stage('Image cleanup'){
     steps{
     script{
       myObject.Image_cleanup("${docker_repo}","${tag}")}
       }
      
  }



//helm update.........................................
   stage('helm update'){
     step{
         myObject.Helm_update("${devops_git_url}","${service_name}","${docker_repo}")
          
       }
      
      }
   }




}

}
