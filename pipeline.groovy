node('maven') {
	def APP_NAME = "report-config-server"
   	// define commands
   	def mvnCmd = "mvn -s configuration/maven-cicd-settings.xml"
   	def CICD_PROJECT = "reportengine-cicd"
   	def DEV_PROJECT = "reportengine-dev"
   	def QA_PROJECT = "reportengine-qa"
   	def PROD_PROJECT = "reportengine-prod"
   	def PORT = 8080
   	def GIT_URL = "https://github.com/vargadan/${APP_NAME}.git"
   	def SKIP_TEST = "true"
 
   	stage ('Build') {
   		git branch: 'master', url: "${GIT_URL}"
   		sh "${mvnCmd} clean package -DskipTests=${SKIP_TEST} fabric8:build"
   	}
   	
   	def version = version()
   	
	stage ('Deploy') {
	   	// create build. override the exit code since it complains about exising imagestream
	   	//tag for version in CICD imagestream
	   	sh "oc tag ${CICD_PROJECT}/${APP_NAME}:latest ${CICD_PROJECT}/${APP_NAME}:${version}"
		envSetup(CICD_PROJECT, APP_NAME, 'latest')
	}

}

def envSetup(project, appName, version) {
	sh "oc delete buildconfig,deploymentconfig,service,routes -l app=${appName} -n ${project}"
   	sh "oc new-app ${appName}:${version} -n ${project}"
   	sh "oc expose svc ${appName} -n ${project}"
}

def version() {
  def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
  matcher ? matcher[0][1] : null
}