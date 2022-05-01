package ch.skyfy16.fk;

import ch.skyfy.fk.config.data.SpawnLocation;
import ch.skyfy.fk.config.data.Square;
import ch.skyfy.fk.config.data.WaitingRoom;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Tests {

    public static class SpawnLoc{
        private String dimensionName;
        private double x;

        public SpawnLoc(String dimensionName, double x) {
            this.dimensionName = dimensionName;
            this.x = x;
        }
    }

    public static class MyConfig{
        public SpawnLoc spawnLoc;

        public MyConfig() {
            spawnLoc = new SpawnLoc("overworld", 10d);
        }
    }

    @Test
    public void serializeConfig(){
//        MyConfig myConfig = new MyConfig();

//        var myConfigType = TypeToken.of(MyConfig.class).getType();
        var myConfigType = TypeToken.of(WaitingRoom.class).getType();

        var waitingRoom = new WaitingRoom(
                new Square((short) 5, 0, -33, 0),
                new SpawnLocation("minecraft:overworld", 0, -33, 0, 69, 69)
        );

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        var destinationFile = new File("C:\\temp\\GsonTests\\MyConfig.json");

        try(var fileWriter = new FileWriter(destinationFile)){
            gson.toJson(waitingRoom, myConfigType, fileWriter);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Test
    public void deserializeConfig(){
        var myConfigType = TypeToken.of(MyConfig.class).getType();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        var targetFile = new File("C:\\temp\\GsonTests\\MyConfig.json");

        try(var fileReader = new FileReader(targetFile)){
            var config = gson.fromJson(fileReader, myConfigType);
            System.out.println();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
