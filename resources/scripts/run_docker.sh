# you may need to run this with super user privileges
docker run -it --rm -u root -v "$PWD":/home/gradle/project -w /home/gradle/project gradle:6.1.1-jdk13 gradle run
