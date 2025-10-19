FROM actiontestscript/linux:3.5.6

ENV SUITE=suite
ENV PROJECT_FOLDER=/home/ats-user/ats-project
ENV CLASS_PATH=$ATS_HOME/libs/*:libs/*:target/classes

COPY libs $PROJECT_FOLDER/libs
COPY src $PROJECT_FOLDER/src
COPY .atsProjectProperties $PROJECT_FOLDER

RUN java -cp $ATS_HOME/libs/*:libs/* com.ats.generator.Generator -force -comp

ENTRYPOINT java -Dats-report=3 -cp $CLASS_PATH org.testng.TestNG -d /home/ats-output src/exec/${SUITE}.xml