@ECHO off

:: ********************************Please set the following configuration*************
:: NOTE: DO NOT change variable names
:: Jenkins will set CONFIGS_DELIVERY_HOME environment variable if this batch is running in a Jenkins job
:: SET CONFIGS_DELIVERY_HOME=
:: Previous Jenkins job will predefine following value of variables
:: SET scriptLocation=
:: SET INITIAL_PROJECT_VERSION=
:: Following value of variables will get from jenkins
:: SET transferKey=
:: SET cipherUsername=
:: SET cipherPassword=
:: SET runnerURL=
:: SET jobName=
:: SET appName=[Phr,PPUI,Registration,Pcm,AdminPortal,TryPolicy,Dss,AdminPortalUI,PatientUser,EdgeServer,DiscoveryServer]
:: ***********************************************************************************

:: Declare variables start
SET logPath=%CONFIGS_DELIVERY_HOME%\%jobName%
SET logName=%logPath%\%date:~4,2%-%date:~7,2%-%date:~10,4%-status.log
:: Declare variables end

:: Start running script
CALL :run
PAUSE
EXIT

:: Declare methods start
: run
  CALL :checkAppsRunnerStatus
  GOTO :EOF

:checkAppsRunnerStatus
  CURL -I -k %runnerURL% >NUL 2>NUL
  IF %ERRORLEVEL% NEQ 0 (
    ECHO WARNING! The spring boot app runner is NOT starting.
  ) ELSE (
      CALL :selectApplicationToRun
  )
  GOTO :EOF

:selectApplicationToRun
  CALL :set%appName%Configs
  CALL :decryptAccountAuthentication
  CALL :startPostFile
  GOTO :EOF

:setPPUIConfigs
:: Following are Spring Boot Apps Runner parameters
  SET filePath=%CONFIGS_DELIVERY_HOME%\%jobName%\target\pp-ui.jar
  CALL :checkAppFile
  SET formDataGroupId=groupId=gov.samhsa.mhc
  SET formDataArtifactId=artifactId=pp-ui
  SET formDataVersion=version=%INITIAL_PROJECT_VERSION%
  SET formDataPackaging=packaging=jar
  SET formDataArgs=args={}
  SET formDataFile=file=@\"%filePath%\"
  GOTO :EOF

:setPhrConfigs
:: Following are Spring Boot Apps Runner parameters
  SET filePath=%CONFIGS_DELIVERY_HOME%\%jobName%\target\phr.jar
  CALL :checkAppFile
  SET formDataGroupId=groupId=gov.samhsa.mhc
  SET formDataArtifactId=artifactId=phr
  SET formDataVersion=version=%INITIAL_PROJECT_VERSION%
  SET formDataPackaging=packaging=jar
  SET formDataArgs=args={}
  SET formDataFile=file=@\"%filePath%\"
  GOTO :EOF

:setPcmConfigs
:: Following are Spring Boot Apps Runner parameters
  SET filePath=%CONFIGS_DELIVERY_HOME%\%jobName%\target\pcm.jar
  CALL :checkAppFile
  SET formDataGroupId=groupId=gov.samhsa.mhc
  SET formDataArtifactId=artifactId=pcm
  SET formDataVersion=version=%INITIAL_PROJECT_VERSION%
  SET formDataPackaging=packaging=jar
  SET formDataArgs=args={}
  SET formDataFile=file=@\"%filePath%\"
  GOTO :EOF

:setAdminPortalConfigs
:: Following are Spring Boot Apps Runner parameters
  SET filePath=%CONFIGS_DELIVERY_HOME%\%jobName%\target\admin-portal.jar
  CALL :checkAppFile
  SET formDataGroupId=groupId=gov.samhsa.adminportal
  SET formDataArtifactId=artifactId=admin-portal
  SET formDataVersion=version=%INITIAL_PROJECT_VERSION%
  SET formDataPackaging=packaging=jar
  SET formDataArgs=args={}
  SET formDataFile=file=@\"%filePath%\"
  GOTO :EOF

:setRegistrationConfigs
:: Following are Spring Boot Apps Runner parameters
  SET filePath=%CONFIGS_DELIVERY_HOME%\%jobName%\target\patient-registration.jar
  CALL :checkAppFile
  SET formDataGroupId=groupId=gov.samhsa.mhc
  SET formDataArtifactId=artifactId=patient-registration
  SET formDataVersion=version=%INITIAL_PROJECT_VERSION%
  SET formDataPackaging=packaging=jar
  SET formDataArgs=args={}
  SET formDataFile=file=@\"%filePath%\"
  GOTO :EOF

:setTryPolicyConfigs
:: Following are Spring Boot Apps Runner parameters
  SET filePath=%CONFIGS_DELIVERY_HOME%\%jobName%\target\try-policy.jar
  CALL :checkAppFile
  SET formDataGroupId=groupId=gov.samhsa.mhc
  SET formDataArtifactId=artifactId=try-policy
  SET formDataVersion=version=%INITIAL_PROJECT_VERSION%
  SET formDataPackaging=packaging=jar
  SET formDataArgs=args={}
  SET formDataFile=file=@\"%filePath%\"
  GOTO :EOF

:setDssConfigs
:: Following are Spring Boot Apps Runner parameters
  SET filePath=%CONFIGS_DELIVERY_HOME%\%jobName%\target\dss.jar
  CALL :checkAppFile
  SET formDataGroupId=groupId=gov.samhsa.mhc
  SET formDataArtifactId=artifactId=dss
  SET formDataVersion=version=%INITIAL_PROJECT_VERSION%
  SET formDataPackaging=packaging=jar
  SET formDataArgs=args={}
  SET formDataFile=file=@\"%filePath%\"
  GOTO :EOF

:setAdminPortalUIConfigs
:: Following are Spring Boot Apps Runner parameters
  SET filePath=%CONFIGS_DELIVERY_HOME%\%jobName%\target\admin-portal-ui.jar
  CALL :checkAppFile
  SET formDataGroupId=groupId=gov.samhsa.mhc
  SET formDataArtifactId=artifactId=admin-portal-ui
  SET formDataVersion=version=%INITIAL_PROJECT_VERSION%
  SET formDataPackaging=packaging=jar
  SET formDataArgs=args={}
  SET formDataFile=file=@\"%filePath%\"
  GOTO :EOF

:setPatientUserConfigs
:: Following are Spring Boot Apps Runner parameters
  SET filePath=%CONFIGS_DELIVERY_HOME%\%jobName%\target\patient-user.jar
  CALL :checkAppFile
  SET formDataGroupId=groupId=gov.samhsa.mhc
  SET formDataArtifactId=artifactId=patient-user
  SET formDataVersion=version=%INITIAL_PROJECT_VERSION%
  SET formDataPackaging=packaging=jar
  SET formDataArgs=args={}
  SET formDataFile=file=@\"%filePath%\"
  GOTO :EOF

:setEdgeServerConfigs
:: Following are Spring Boot Apps Runner parameters
  SET filePath=%CONFIGS_DELIVERY_HOME%\%jobName%\target\edge-server.jar
  CALL :checkAppFile
  SET formDataGroupId=groupId=gov.samhsa.mhc
  SET formDataArtifactId=artifactId=edge-server
  SET formDataVersion=version=%INITIAL_PROJECT_VERSION%
  SET formDataPackaging=packaging=jar
  SET formDataArgs=args={}
  SET formDataFile=file=@\"%filePath%\"
  GOTO :EOF

:setDiscoveryServerConfigs
:: Following are Spring Boot Apps Runner parameters
  SET filePath=%CONFIGS_DELIVERY_HOME%\%jobName%\target\discovery-server.jar
  CALL :checkAppFile
  SET formDataGroupId=groupId=gov.samhsa.mhc
  SET formDataArtifactId=artifactId=discovery-server
  SET formDataVersion=version=%INITIAL_PROJECT_VERSION%
  SET formDataPackaging=packaging=jar
  SET formDataArgs=args={}
  SET formDataFile=file=@\"%filePath%\"
  GOTO :EOF

:checkAppFile
  IF NOT EXIST %filePath% (
	ECHO WARNING! The file is NOT existing.
	SET ERRORLEVEL=1
	EXIT
  )
  GOTO :EOF

:decryptAccountAuthentication
  FOR /F %%i IN ('"ECHO %cipherUsername% | openssl enc -d -aes-256-cbc -a -salt -pass pass:%transferKey%"') DO SET plainUsername=%%i
  FOR /F %%i IN ('"ECHO %cipherPassword% | openssl enc -d -aes-256-cbc -a -salt -pass pass:%transferKey%"') DO SET plainPassword=%%i
  GOTO :EOF

:startPostFile
  SET formDataOptions=-F %formDataGroupId% -F %formDataArtifactId% -F %formDataVersion% -F %formDataPackaging% -F %formDataArgs% -F %formDataFile%
  CURL -u %plainUsername%:%plainPassword% -k -s -D %logName% %formDataOptions% %runnerURL%
  CALL :checkPostStatus
  GOTO :EOF

:checkPostStatus
  FOR /f %%f IN ('findstr /c:"HTTP/1.1 200" "%logName%"') DO SET/a totalSuccess+=1
  IF (%totalSuccess%) NEQ (1) (
      ECHO WARNING! Post app artifacts failed!
	  EXIT 3
  )
  GOTO :EOF
:: Declare methods end