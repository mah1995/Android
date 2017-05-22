package com.example.mahdi.smartenvironnement;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;



public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private TextView temp;
    private TextView hum;
    private TextView air;
    private long x; // abscissa
    private double y; // Ordinate
    private long m ; 
    private long x1 = 1800000000; // value to compare with timestamp when drawing the graph


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);  
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("temperature").getRef(); // Database Reference for temperature
        DatabaseReference ref1= FirebaseDatabase.getInstance().getReference().child("airQuality").getRef(); // Database Reference for air quality
        DatabaseReference ref2= FirebaseDatabase.getInstance().getReference().child("humidity").getRef(); // Database Reference for humidity
        Query q = mDatabase.child("airQuality").orderByKey().limitToLast(10); // get the last 10 values of air quality to draw the graph
        temp = (TextView) findViewById(R.id.val_temp);
        hum = (TextView) findViewById(R.id.val_hum);
        air = (TextView) findViewById(R.id.val_air);



        Query lastQuery = ref.orderByKey().limitToLast(1); // Get the last temperature value in the database

        lastQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // when a child is added, we set the textview 
				String t = dataSnapshot.getValue().toString();
                String t2 = t.substring(t.indexOf("value=")+6,t.indexOf("}"));
                temp.setText(t2+'\u00B0'+"C");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        Query lastQuery1 = ref1.orderByKey().limitToLast(1);  // Get the last air quality value in the database

        lastQuery1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
				
				// when a child is added, we set the textview 

                String t = dataSnapshot.getValue().toString();
                String t2 = t.substring(t.indexOf("value=")+6,t.indexOf("}"));
                air.setText(t2+" ppm");
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); 


                if (Double.valueOf(t2)> 15) // if threshold is reached (15 ppm) we build an alert and send notification
                {
                    //notificationManager.cancelAll();

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Attention !")
                            .setMessage("Le niveau du CO est élevé !! : "+ t2 + " ppm")

                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();


                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this);
                    builder.setSmallIcon(android.R.drawable.ic_dialog_alert);
                    Intent intent = new Intent(MainActivity.this,MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
                    builder.setContentIntent(pendingIntent);
                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.abc));
                    builder.setContentTitle("Attention !");
                    builder.setContentText("Le niveau de CO est élevée : " + t2 + " ppm");
                    builder.setSubText("Consulter la qualité d'air");
                    builder.setAutoCancel(true);
                    notificationManager.notify(1, builder.build());

                    air.setTextColor(Color.RED);


                }

                else {
                    air.setTextColor(Color.BLACK);
                    

                }


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        Query lastQuery2 = ref2.orderByKey().limitToLast(1); // Get the last humidity value in the database

        lastQuery2.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String t = dataSnapshot.getValue().toString();
                String t2 = t.substring(t.indexOf("value=")+6,t.indexOf("}"));
                hum.setText(t2+" %");

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        final GraphView graph = (GraphView) findViewById(R.id.graph);
        final LineGraphSeries<DataPoint> series = new LineGraphSeries<>();


        q.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                x= Long.valueOf(dataSnapshot.getKey());
                if (x1>x)
                {
                    x1=x;
                }
                y = Double.valueOf(dataSnapshot.getValue().toString().substring(dataSnapshot.getValue().toString().indexOf("value=")+6,dataSnapshot.getValue().toString().indexOf("}")));
                series.appendData(new DataPoint(x-x1,y),true,10); // add data to the series to represent

                series.setThickness(7); // thickness of graph
                series.setColor(Color.RED);
                graph.addSeries(series);
                graph.getViewport().setXAxisBoundsManual(true); 
                graph.getViewport().scrollToEnd(); // scroll the graph till the end
                graph.setTitle("Variation de la qualité d'air");
                graph.getGridLabelRenderer().setHorizontalAxisTitle("Temps en secondes"); 
                graph.getGridLabelRenderer().setLabelFormatter(
                        new DefaultLabelFormatter() {
                            @Override
							// format the y labels
                            public String formatLabel(double value, boolean isValueX) {
                                if (isValueX) {
                                    // show normal x values
                                    return super.formatLabel(value, isValueX);
                                } else {
                                    // show currency for y values
                                    return super.formatLabel(value, isValueX) + " ppm";
                                }
                            }
                        }
                );

                graph.getViewport().setScrollable(true);
                graph.getViewport().setYAxisBoundsManual(true);
                graph.getViewport().setMinX(series.getLowestValueX());
                graph.getViewport().setMaxX(series.getHighestValueX());
                graph.getViewport().setMinY(0);
                graph.getViewport().setMaxY(400);


                }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

}
