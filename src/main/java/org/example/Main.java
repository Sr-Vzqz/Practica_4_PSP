package org.example;

import org.apache.commons.net.ftp.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    private static final String SERVIDOR = "127.0.0.1";
    private static final int PUERTO = 21;

    public static void main(String[] args) {
        try(Scanner sc = new Scanner(System.in)) {
            //Creación del cliente FTP y variable para comprobar si el login es válido
            FTPClient ftpClient = new FTPClient();
            boolean logValido = false;
            try {
                //Conexión al servidor
                while(!logValido) {
                    //Mientras no se haya logueado correctamente, se seguirá pidiendo usuario y contraseña
                    System.out.print("Introduce un nombre de usuario: ");
                    String nombreUsuario = sc.nextLine();
                    System.out.print("Introduce la contraseña (En caso anónimo, deja el campo vacío): ");
                    String contraseña = sc.nextLine();

                    ftpClient.connect(SERVIDOR, PUERTO);
                    logValido = ftpClient.login(nombreUsuario, contraseña);
                    if (logValido) {
                        System.out.println("Bienvenido, " + nombreUsuario);
                        menu(ftpClient, sc, nombreUsuario);
                        ftpClient.logout();
                    } else {
                        System.out.println("Error de conexión. Usuario o contraseña incorrectos.");
                    }
                }
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void menu(FTPClient ftpClient, Scanner sc, String nombreUsuario) throws IOException {
        while (true) {
            System.out.println("\n Elige una opción:");
            System.out.println("1. Listar archivos del servidor");
            System.out.println("2. Subir archivos");
            System.out.println("3. Descargar archivos");
            System.out.println("0. Salir");
            System.out.print("Opción: ");

            int opcion = sc.nextInt();
            sc.nextLine();
            switch (opcion) {
                case 1:
                    mostrarArchivos(ftpClient);
                    break;
                case 2:
                    if (!nombreUsuario.equalsIgnoreCase("anonimo")) { //Si el usuario no está logueado, no se le permite subir archivos
                        System.out.print("Nombre del archivo a subir: ");
                        String archivoSubir = sc.nextLine();
                        subirArchivos(ftpClient, archivoSubir);
                    } else {
                        System.err.println("Debes estar registrado para subir archivos");
                    }
                    break;
                case 3:
                    System.out.print("Nombre del archivo a descargar: ");
                    String archivoDescargar = sc.nextLine();
                    descargarArchivo(ftpClient, archivoDescargar);
                    break;
                case 0:
                    return;
                default:
                    System.err.println("Opción no válida");
            }
        }
    }

    private static void mostrarArchivos(FTPClient ftpClient) throws IOException {
        FTPFile[] archivos = ftpClient.listFiles();
        System.out.println("Archivos del servidor:");
        for (FTPFile archivo : archivos) {
            System.out.println("- " + archivo.getName());
        }
    }

    private static void descargarArchivo(FTPClient ftpClient, String remoteFilePath) throws IOException {
        File archivoLocal = new File(remoteFilePath);
        FileOutputStream fos = new FileOutputStream(archivoLocal);
        boolean descarga = ftpClient.retrieveFile(remoteFilePath, fos);
        fos.close();
        if (descarga) {
            System.out.println("Archivo descargado correctamente");
        } else {
            System.out.println("Error al descargar el archivo");
        }
    }

    private static void subirArchivos(FTPClient ftpClient, String localFilePath) {
        try {
            File archivo = new File(localFilePath);
            if (!archivo.exists()) {
                System.out.println("Fallo al subir el archivo: El archivo no existe");
                return;
            }
            FileInputStream fis = new FileInputStream(archivo);
            boolean subido = ftpClient.storeFile(archivo.getName(), fis);
            fis.close();
            if (subido) {
                System.out.println("Archivo subido");
            } else {
                System.out.println("Error al subir el archivo");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
