package tk.roberthramirez.chisteboot;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Xml;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChisteService extends Service {
    private Thread worker = null;
    private final String url = "http://chistes.germangascon.com/aleatorio.php";
    private String XML_START_TAG = "chiste";
    private String mensaje = "n";
    public static final String CHANNEL_ID = "CH_01";
    public static final int ID_ALERTA_NOTIFICACION = 1;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(worker == null || !worker.isAlive()) {
            worker = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        InputStream stream = null;
                        try {
                            stream = downloadUrl(url);

                            //TODO pasar a metodo para que clase main pueda acceder a esto o hacer clase singleton
                            //MyXmlParser myXmlParser = new MyXmlParser();

                            XmlPullParser parser = Xml.newPullParser();
                            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                            parser.setInput(stream, null);
                            parser.nextTag();

                            parser.require(XmlPullParser.START_TAG, null, XML_START_TAG);
                            while (parser.next() != XmlPullParser.END_TAG){
                                if(parser.getEventType() != XmlPullParser.START_TAG) {
                                    continue;
                                }
                                String tagName = parser.getName();
                                if(tagName.equals("texto")) {
                                    mensaje = parser.getText();
                                }

                            }
                        } finally {
                            if (stream != null) {
                                stream.close();
                            }
                        }

                    } catch (IOException e) {
                        // Error de conexión
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                        // Error en los datos
                    }

                }
            });
            worker.start();
        }
        mostrarNotificacion();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private InputStream downloadUrl(String urlString) throws IOException {

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(500000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }

    private void mostrarNotificacion() {
        crearCanalNotificacion();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle("Chiste del dia")
                .setContentText(mensaje)
                .setContentInfo("4")
                .setTicker("Nuevo chiste del dia");
        //Creamos el PendingIntent
        Intent intent = new Intent (this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Asignamos el PendingIntent que será ejecutado al pulsar sobre la notificación
        mBuilder.setContentIntent(pendingIntent);
        //Finalmente mostrarmos la notificación
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID_ALERTA_NOTIFICACION, mBuilder.build());
    }

    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence nombre = "Mi canal";
            String descripcion = "Mi canal de notificación ";
            int importancia = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, nombre, importancia);
            channel.setDescription(descripcion);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
