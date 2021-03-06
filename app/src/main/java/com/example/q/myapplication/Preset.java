package com.example.q.myapplication;

import java.util.ArrayList;

/**
 * Created by q on 2017-01-12.
 */

public class Preset {
    public static int PRESET_SIZE = 50;
    public static int OFFSET_SIZE = 0;
    ArrayList<AccelData> accelDatas;
    ArrayList<OrientData> orientDatas;
    int type;

    public Preset(ArrayList<AccelData> accelHistory, ArrayList<OrientData> orientHistory, int type) {
        accelDatas = new ArrayList<AccelData>
                (accelHistory.subList(accelHistory.size()-1-PRESET_SIZE-OFFSET_SIZE, accelHistory.size()-1-OFFSET_SIZE));
        orientDatas = new ArrayList<OrientData>
                (orientHistory.subList(orientHistory.size()-1-PRESET_SIZE-OFFSET_SIZE, orientHistory.size()-1-OFFSET_SIZE));
        this.type = type;
    }

    public double distance(ArrayList<AccelData> accelHistory, ArrayList<OrientData> orientHistory) {
        double distance = 0;
        for(int i=0 ; i<PRESET_SIZE ; i++) {
            distance += Math.pow((accelHistory.get(accelHistory.size()-1-i-OFFSET_SIZE).getX()
                    -accelDatas.get(PRESET_SIZE-1-i).getX()),2);
            distance += Math.pow((accelHistory.get(accelHistory.size()-1-i-OFFSET_SIZE).getY()
                    -accelDatas.get(PRESET_SIZE-1-i).getY()),2);
            distance += Math.pow((accelHistory.get(accelHistory.size()-1-i-OFFSET_SIZE).getZ()
                    -accelDatas.get(PRESET_SIZE-1-i).getZ()),2);

            distance += Math.pow((orientHistory.get(orientHistory.size()-1-i-OFFSET_SIZE).getAzimuth()
                                    -orientHistory.get(PRESET_SIZE-1-i).getAzimuth()),2)*10;
            distance += Math.pow((orientHistory.get(orientHistory.size()-1-i-OFFSET_SIZE).getPitch()
                    -orientHistory.get(PRESET_SIZE-1-i).getPitch()),2);
            distance += Math.pow((orientHistory.get(orientHistory.size()-1-i-OFFSET_SIZE).getRoll()
                    -orientHistory.get(PRESET_SIZE-1-i).getRoll()),2);
        }
        return distance;
    }
}
