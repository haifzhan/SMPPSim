SMPPSim official website tutorial:
http://www.seleniumsoftware.com/user-guide.htm#quick


REQUIERD SOFTWARE:
1. java environment java 1.6.x or later.
2. maven 2.x or 3.x

QUICK START
1. create smppsim.jar file using "mvn clean install" under folder SMPPSim
2. Put the smppsim.jar file into folder Simulator.
3. Change the configuration file smppsim.props  in folder conf
4. sudo startsmppsim.sh

MODIFICATION:
1. mute unncessary logging information.
2. add a logger to record received OP,CP and counters in class StandardProtocolHandler.java file.
