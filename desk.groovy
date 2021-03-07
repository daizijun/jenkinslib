#!groovy

@Library('jenkinslib') _

def tools = new org.devops.tools()
def mytools = new org.devops.ToolsColors()

String workspace = "/opt/jenkins/workspace"

hello()

pipeline{
agent { node { label "master"       //指定运行此流水线的节点
                customWorkspace "{workspace}"   //指定运行工作目录
}}

environment { 
    activeEnv = 'dev'
}

options {
    timestamps()        //日志会有时间
    skipDefaultCheckout()       //删除隐式checkout scm语句
    disableConcurrentBuilds()       //禁止并行
    timeout(time: 1, unit: 'HOURS')         //流水线超时设置1h
}

parameters { string(name: 'DEPLOY_ENV', defaultValue: 'staging', description: '') }

//流水线的阶段
stages{

    //阶段1 获取代码
    stage("CheckOut"){
        steps{
            script{
                input id: 'Test', message: '我们是否要继续', ok: '是，继续吧', parameters: [choice(choices: ['a', 'b'], name: 'test')], submitter: 'alice,bob'
                println("获取代码")
                mytools.PrintMes("获取代码",'green')
                
                tools.PrintMes("this is my lib!")
            }
            echo activeEnv
        }
    }
    
    //阶段2 构建
    stage('Parallel Stage') {
    failFast true
    parallel {
        stage("Build01"){
            steps{
                script{
                    println("运行构建")
                    mytools.PrintMes("运行构建",'green')
                    mvnHome = tool "m2"
                    println(mvnHome)
                
                    sh "${mvnHome}/bin/mvn --version"
                }
                    echo "test parameters参数${params.DEPLOY_ENV}"
            }
        }   
        stage("Build02"){
            steps{
                script{
                    println("运行构建")
                    mytools.PrintMes("运行构建",'green')
                    mvnHome = tool "m2"
                    println(mvnHome)
                
                    sh "${mvnHome}/bin/mvn --version"
                    
                }
                    echo "test parameters参数${params.DEPLOY_ENV}"
            }
        }
    }
    }
    
    stage('Example'){
        when { environment name: 'DEPLOY_ENV', value: 'staging' }
        
        input {
            message "Should we continue?"
            ok "Yes, we should."
            submitter "alice,bob"
            parameters {
                string(name: 'PERSON', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
            }
        }

        //局部变量
        environment { 
            AN_ACCESS_KEY = credentials('test-token') 
        }
        steps {
            echo "Hello, ${PERSON}, nice to meet you."
            sh 'printenv'
        }
    }
}


post {
    always{
        script{
            println("流水线结束后，经常做的事情")
        }
        echo 'I will always say Hello again!'
    }
        
    success{
        script{
            println("流水线成功后，要做的事情")
        }
        
    }
    failure{
        script{
            println("流水线失败后，要做的事情")
        }
    }
        
    aborted{
        script{
            println("流水线取消后，要做的事情")
        }
        
    }
}
}
