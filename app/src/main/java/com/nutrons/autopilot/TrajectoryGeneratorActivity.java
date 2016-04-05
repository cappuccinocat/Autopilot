package com.nutrons.autopilot;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.nutrons.autopilot.lib.trajectory.Path;
import com.nutrons.autopilot.lib.trajectory.PathGenerator;
import com.nutrons.autopilot.lib.trajectory.TrajectoryGenerator;
import com.nutrons.autopilot.lib.trajectory.WaypointSequence;
import com.nutrons.autopilot.lib.trajectory.io.TextFileSerializer;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class TrajectoryGeneratorActivity extends AppCompatActivity {
    private Button goHomeButton;
    private Button createAnotherButton;

    public static String joinPath(String path1, String path2) {
        File file1 = new File(path1);
        File file2 = new File(file1, path2);
        return file2.getPath();
    }

    private static boolean writeFile(String path, String data) {
        try {
            File file = new File(path);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(data);
            bw.close();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trajectory_generator);

        Intent intent = getIntent();
        double maxAccel = intent.getDoubleExtra("MaxAccel", 0.0);
        double maxJerk = intent.getDoubleExtra("MaxJerk", 0.0);
        double maxVel = intent.getDoubleExtra("MaxVel", 0.0);
        double kWheelbaseWidth = intent.getDoubleExtra("kWheelbaseWidth", 0.0);
        double[] waypointArrayX = intent.getDoubleArrayExtra("waypointArrayX");
        double[] waypointArrayY = intent.getDoubleArrayExtra("waypointArrayY");

        String pathName = intent.getStringExtra("pathName");
        String pathDescription = intent.getStringExtra("pathDescription");

        for(int i = 0; i<waypointArrayX.length; i++){
            waypointArrayX[i] = waypointArrayX[i] - waypointArrayX[0];
            waypointArrayY[i] = waypointArrayY[i] - waypointArrayY[0];
        }

        for(int i = 0; i<waypointArrayX.length; i++){
            System.out.println(waypointArrayX[i] + " - " + waypointArrayY[i]);
        }

        goHomeButton = (Button) findViewById(R.id.homeButton);
        goHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TrajectoryGeneratorActivity.this, MainActivity.class));
            }
        });

        createAnotherButton = (Button) findViewById(R.id.createAgainButton);
        createAnotherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TrajectoryGeneratorActivity.this, CreatePathActivity.class));
            }
        });

        File mirectory = this.getDir("NUTRONsCAT", MODE_PRIVATE);
        String directory = mirectory.getPath();

        TrajectoryGenerator.Config config = new TrajectoryGenerator.Config();

        config.dt = .01;
        config.max_acc = maxAccel;
        config.max_jerk = maxJerk;
        config.max_vel = maxVel;

        {
            config.dt = .01;
            config.max_acc = 8.0;
            config.max_jerk = 50.0;
            config.max_vel = 10.0;
            // Path name must be a valid Java class name.
            final String path_name = pathName;

            // Description of this auto mode path.
            WaypointSequence p = new WaypointSequence(10);

            for (int i = 0; i < waypointArrayX.length; i++) {
                p.addWaypoint(new WaypointSequence.Waypoint(waypointArrayX[i], waypointArrayY[i], 0));
            }

            Path path = PathGenerator.makePath(p, config,
                    kWheelbaseWidth, path_name);

            // Outputs to the directory supplied as the first argument.
            TextFileSerializer js = new TextFileSerializer();
            String serialized = js.serialize(path);
            //System.out.print(serialized);

            String fullpath = joinPath(directory, path_name + ".txt");

            if (!writeFile(fullpath, serialized)) {
                System.err.println(fullpath + " could not be written!!!!1");
                System.exit(1);
            } else {
                System.out.println("Wrote " + fullpath);
            }
        }
    }
}