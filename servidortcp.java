package kinectviewerapp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Blob;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;

public class servidortcp {

    public static void main(String[] args) {

        ServerSocket servidor = null;
        Socket sc = null;
        DataInputStream in;
        DataOutputStream out;
        final int PUERTO = 5000;
        Blob blob = null;

        try {
            servidor = new ServerSocket(PUERTO);
            System.out.println("Servidor iniciado");
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Conexion establecida");

            while (true) {

                sc = servidor.accept();
                System.out.println("Cliente conectado");

                in = new DataInputStream(sc.getInputStream());
                out = new DataOutputStream(sc.getOutputStream());

                InputStream inputStream = sc.getInputStream();

                System.out.println(inputStream);
                String mensaje = in.readUTF();

                java.sql.Connection conexion = DriverManager.getConnection("jdbc:mysql://localhost:3306/virt06bd?useUnicode=true&useJDBCCompliantTimezneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "root", "");
                //java.sql.Connection conexion = DriverManager.getConnection("jdbc:mysql://localhost:3306/virt06bd?useUnicode=true&useJDBCCompliantTimezneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "root", "root");

                if (conexion != null) {
                    System.out.println("Conexion satisfactoria\n");

                }

                String datoCasa, datoUbicacion, datoFecha, datoCamara, datoFotoUrl, datoId;
                String[] parts = mensaje.split("-");
                datoCasa = parts[0];
                datoUbicacion = parts[1];
                datoFecha = parts[2];
                datoCamara = parts[3];
                datoFotoUrl = parts[4];
                datoId = parts[5];

                byte[] imageAr = new byte[62100];
                inputStream.read(imageAr);

                BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageAr));
                System.out.println("Received " + image.getHeight() + "x" + image.getWidth() + ": " + System.currentTimeMillis());
                ImageIO.write(image, "jpg", new File("Copia" + datoId + ".png"));

                System.out.println("Casa: " + datoCasa + "\nUbicacion: " + datoUbicacion
                        + "\nFecha: " + datoFecha + "\nCamara: " + datoCamara + "\n" + "Foto: " + datoFotoUrl.toString());

                System.out.println("Llega antes de insert 2\n");
                System.out.println("Id que llega= "+datoId);

                if (datoId.equals("Original")) {
                    System.out.println("Se corre la primera consulta");
                    String query = "INSERT INTO Datos(NombreCasa,Ubicacion,Fecha,NoCamara) values ('" + datoCasa + "','" + datoUbicacion + "','" + datoFecha + "','" + datoCamara + "')";
                    Statement stmt = conexion.createStatement();
                    stmt.executeUpdate(query);
                    blob = new SerialBlob(imageAr);
//                FileInputStream is = new FileInputStream("Z:\\");
                    PreparedStatement st = conexion.prepareStatement("UPDATE Datos SET FotoOrg = ? WHERE Fecha= ?");
                    st.setBlob(1, blob);
                    st.setString(2, datoFecha);
                    st.execute();
                    st.close();
                } else if (datoId.equals("ConFiltro")) {
                    System.out.println("Se corre la segunda consulta");
                    blob = new SerialBlob(imageAr);
//                FileInputStream is = new FileInputStream("Z:\\");
                    PreparedStatement st = conexion.prepareStatement("UPDATE Datos SET FotoFil = ? WHERE Fecha= ?");
                    st.setBlob(1, blob);
                    st.setString(2, datoFecha);
                    st.execute();
                    st.close();
                }
                else{
                    System.out.println("No entra :'v");
                }

                out.writeUTF("Datos recibidos");

                inputStream.close();
                
                sc.close();

                System.out.println("Cliente desconectado");

            }

        } catch (IOException ex) {
            Logger.getLogger(servidortcp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(servidortcp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(servidortcp.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
