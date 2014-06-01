# Questlicious Mod Dev Setup

This guide assumes you have Minecraft and Eclipse installed already.

1. Clone this repo into a new directory

```
git clone https://github.com/DougHamil/Questlicious.git
```

2. Download the [Lord of the Rings Minecraft Mod](http://adf.ly/2667786/lotr-beta-171)

3. Move the LOTR minecraft mod jar file to the _external_ directory and name it _lotr.jar_

4. Do the one-time setup for forge mod development by going into your questlicious directory and executing:

```
./gradlew setupDecompWorkspace --refresh-dependencies
```

Once it is complete, run the following command to setup an Eclipse workspace for the project:

```
./gradlew eclipse
```

5. Open up Eclipse and open up the questlicious directory as a workspace.
To verify everything is set up correctly, press the run button in Eclipse and minecraft should start.
