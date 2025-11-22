package com.example.proyecto_de_integracion;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AcercaDeActivity extends AppCompatActivity {

    private static final int REQ_WRITE_EXTERNAL = 100;

    private Button btnGenerarInforme;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference refUsuarios, refPublicaciones, refGastosMensuales;

    // Diálogo de carga
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acerca_de);

        // ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Acerca de VeciApp");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        btnGenerarInforme = findViewById(R.id.btnGenerarInforme);
        btnGenerarInforme.setVisibility(android.view.View.GONE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Espere por favor");
        progressDialog.setCanceledOnTouchOutside(false);

        auth = FirebaseAuth.getInstance();
        refUsuarios = FirebaseDatabase.getInstance().getReference("Usuarios");
        refPublicaciones = FirebaseDatabase.getInstance().getReference("Publicaciones");
        refGastosMensuales = FirebaseDatabase.getInstance().getReference("GastosMensuales");

        // Solo el admin (ej: admin@gmail.com) puede ver el botón
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null &&
                user.getEmail().equalsIgnoreCase("admin@gmail.com")) {
            btnGenerarInforme.setVisibility(android.view.View.VISIBLE);
        }

        // Pedir permiso de escritura solo para Android 9 o menor
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQ_WRITE_EXTERNAL);
            }
        }

        btnGenerarInforme.setOnClickListener(v -> generarInformeGestion());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Lee métricas desde Firebase y, cuando termina, llama a crearPdfInforme(...)
     */
    private void generarInformeGestion() {
        progressDialog.setMessage("Recolectando datos de VeciApp...");
        progressDialog.show();

        // 1) Contar usuarios
        refUsuarios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotUsuarios) {
                int totalUsuarios = (int) snapshotUsuarios.getChildrenCount();

                // 2) Contar publicaciones (activas / inactivas)
                refPublicaciones.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshotPub) {

                        final int[] totalPublicaciones = {0};
                        final int[] publicacionesActivas = {0};
                        final int[] publicacionesInactivas = {0};

                        for (DataSnapshot ds : snapshotPub.getChildren()) {
                            totalPublicaciones[0]++;
                            Boolean activo = ds.child("activo").getValue(Boolean.class);
                            if (activo == null || activo) {
                                publicacionesActivas[0]++;
                            } else {
                                publicacionesInactivas[0]++;
                            }
                        }

                        // 3) Contar meses de gastos y sumar total generado
                        refGastosMensuales.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshotGastosMensuales) {

                                int totalMesesGastos = (int) snapshotGastosMensuales.getChildrenCount();
                                double totalGastosAcumulados = 0;

                                for (DataSnapshot ds : snapshotGastosMensuales.getChildren()) {
                                    Double totalMes =
                                            ds.child("totalGastosEdificio").getValue(Double.class);
                                    if (totalMes != null) {
                                        totalGastosAcumulados += totalMes;
                                    }
                                }

                                progressDialog.dismiss();

                                crearPdfInforme(
                                        totalUsuarios,
                                        totalPublicaciones[0],
                                        publicacionesActivas[0],
                                        publicacionesInactivas[0],
                                        totalMesesGastos,
                                        totalGastosAcumulados
                                );
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                progressDialog.dismiss();
                                Toast.makeText(AcercaDeActivity.this,
                                        "Error al leer gastos: " + error.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(AcercaDeActivity.this,
                                "Error al leer publicaciones: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(AcercaDeActivity.this,
                        "Error al leer usuarios: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Construye el PDF tipo "one page report" con las métricas recibidas
     * y lo guarda en Descargas/InformesVeciApp (visible en la app de Archivos).
     */
    private void crearPdfInforme(int totalUsuarios,
                                 int totalPublicaciones,
                                 int publicacionesActivas,
                                 int publicacionesInactivas,
                                 int totalMesesGastos,
                                 double totalGastosAcumulados) {

        Locale localeCl = new Locale("es", "CL");
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(localeCl);
        String totalGastosStr = currencyFormat.format(totalGastosAcumulados);

        SimpleDateFormat sdfTitulo =
                new SimpleDateFormat("dd/MM/yyyy HH:mm", localeCl);
        String fechaGeneracion = sdfTitulo.format(new Date());

        SimpleDateFormat sdfFile =
                new SimpleDateFormat("yyyyMMdd_HHmmss", localeCl);
        String nombreArchivo = "Informe_Gestion_VeciApp_" + sdfFile.format(new Date()) + ".pdf";

        // Tamaño A4 aprox.
        int pageWidth = 595;
        int pageHeight = 842;

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        int colorVerdeHeader = Color.rgb(76, 175, 80);
        int colorDark = Color.rgb(33, 33, 33);
        int colorLine = Color.rgb(189, 189, 189);

        int marginLeft = 40;
        int marginRight = pageWidth - 40;

        int y = 60; // margen superior

        // ===== TÍTULO PRINCIPAL =====
        paint.setColor(Color.BLACK);
        paint.setTextSize(22f);
        paint.setFakeBoldText(true);
        canvas.drawText("Informe de gestión de VeciApp", marginLeft, y, paint);
        paint.setFakeBoldText(false);

        y += 26;
        paint.setTextSize(12f);
        canvas.drawText("Generado automáticamente – " + fechaGeneracion, marginLeft, y, paint);

        // ===== CINTA VERDE PROYECTO =====
        y += 28;
        paint.setColor(colorVerdeHeader);
        canvas.drawRect(0, y, pageWidth, y + 45, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(14f);
        paint.setFakeBoldText(true);
        canvas.drawText("Proyecto: VeciApp – Plataforma de gestión comunitaria", marginLeft, y + 28, paint);
        paint.setFakeBoldText(false);

        // ===== SEPARADOR =====
        y += 70;
        paint.setColor(colorLine);
        paint.setStrokeWidth(1.5f);
        canvas.drawLine(marginLeft, y, marginRight, y, paint);

        // ===== RESUMEN DE MÉTRICAS (TÍTULO BANDA OSCURA) =====
        y += 28;
        paint.setColor(colorDark);
        canvas.drawRect(0, y, pageWidth, y + 28, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(14f);
        paint.setFakeBoldText(true);
        canvas.drawText("1. Resumen de métricas del sistema", marginLeft, y + 20, paint);
        paint.setFakeBoldText(false);

        // ===== MÉTRICAS PRINCIPALES CON MÁS ESPACIO =====
        y += 44;
        paint.setColor(Color.BLACK);
        paint.setTextSize(13f);

        int xCol1 = marginLeft;
        int xCol2 = (pageWidth / 2) + 10;
        int lineSpacing = 30;  // AQUÍ le damos aire

        // Fila 1
        canvas.drawText("Usuarios registrados: " + totalUsuarios, xCol1, y, paint);
        canvas.drawText("Publicaciones totales: " + totalPublicaciones, xCol2, y, paint);

        // Fila 2
        y += lineSpacing;
        canvas.drawText("Publicaciones activas: " + publicacionesActivas, xCol1, y, paint);
        canvas.drawText("Publicaciones inactivas: " + publicacionesInactivas, xCol2, y, paint);

        // Fila 3
        y += lineSpacing;
        canvas.drawText("Meses con gastos generados: " + totalMesesGastos, xCol1, y, paint);

        // Fila 4 (solo una columna, bien separada)
        y += lineSpacing;
        canvas.drawText("Gastos acumulados: " + totalGastosStr, xCol1, y, paint);

        // Separador
        y += 30;
        paint.setColor(colorLine);
        canvas.drawLine(marginLeft, y, marginRight, y, paint);

        // ===== MÓDULOS DE VECIAPP =====
        y += 32;
        paint.setColor(colorDark);
        paint.setTextSize(14f);
        paint.setFakeBoldText(true);
        canvas.drawText("2. Módulos de VeciApp", marginLeft, y, paint);
        paint.setFakeBoldText(false);

        y += 24;
        drawBar(canvas, paint, marginLeft, y, marginRight,
                Color.rgb(129, 199, 132), "Módulo de gastos comunes – Generación y asignación de cobros");
        y += 34;

        drawBar(canvas, paint, marginLeft, y, marginRight,
                Color.rgb(100, 181, 246), "Módulo de marketplace – Publicaciones de productos y servicios");
        y += 34;

        drawBar(canvas, paint, marginLeft, y, marginRight,
                Color.rgb(255, 202, 40), "Módulo de usuarios – Registro y administración de residentes");
        y += 44;

        // Separador
        paint.setColor(colorLine);
        canvas.drawLine(marginLeft, y, marginRight, y, paint);

        // ===== GESTIÓN Y OPERACIÓN =====
        y += 34;
        paint.setColor(colorDark);
        paint.setTextSize(14f);
        paint.setFakeBoldText(true);
        canvas.drawText("3. Gestión y operación del sistema", marginLeft, y, paint);
        paint.setFakeBoldText(false);

        y += 26;
        paint.setColor(Color.BLACK);
        paint.setTextSize(12f);
        canvas.drawText("• Modelo de trabajo: desarrollo incremental con validaciones en cada módulo.", marginLeft, y, paint);

        y += 24;
        canvas.drawText("• Control de cambios: despliegues por versión y registro de ajustes en la BD.", marginLeft, y, paint);

        y += 24;
        canvas.drawText("• Monitoreo: métricas de usuarios, publicaciones y gastos comunes.", marginLeft, y, paint);

        // Separador
        y += 32;
        paint.setColor(colorLine);
        canvas.drawLine(marginLeft, y, marginRight, y, paint);

        // ===== CONCLUSIÓN =====
        y += 34;
        paint.setColor(colorDark);
        paint.setTextSize(14f);
        paint.setFakeBoldText(true);
        canvas.drawText("4. Conclusión general", marginLeft, y, paint);
        paint.setFakeBoldText(false);

        y += 26;
        paint.setColor(Color.BLACK);
        paint.setTextSize(12f);
        canvas.drawText("VeciApp se encuentra operativa, con módulos claves en uso dentro de la comunidad.", marginLeft, y, paint);

        y += 24;
        canvas.drawText("El administrador puede utilizar este informe como base para planificar nuevas mejoras", marginLeft, y, paint);

        y += 24;
        canvas.drawText("y evaluar la adopción real del sistema por parte de los residentes.", marginLeft, y, paint);

        document.finishPage(page);

        // ==== GUARDADO EN DESCARGAS (MISMO CÓDIGO QUE YA TENÍAS) ====
        OutputStream out = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS + "/InformesVeciApp");

                Uri uri = getContentResolver().insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (uri == null) {
                    document.close();
                    Toast.makeText(this,
                            "No se pudo crear el archivo en Descargas.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                out = getContentResolver().openOutputStream(uri);
                if (out == null) {
                    document.close();
                    Toast.makeText(this,
                            "No se pudo abrir el stream de salida.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                document.writeTo(out);
                out.flush();

                Toast.makeText(this,
                        "Informe generado en Descargas/InformesVeciApp\n(" + nombreArchivo + ")",
                        Toast.LENGTH_LONG).show();

            } else {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS);
                File carpetaInformes = new File(downloadsDir, "InformesVeciApp");
                if (!carpetaInformes.exists()) {
                    carpetaInformes.mkdirs();
                }
                File pdfFile = new File(carpetaInformes, nombreArchivo);

                out = new FileOutputStream(pdfFile);
                document.writeTo(out);
                out.flush();

                Toast.makeText(this,
                        "Informe generado en:\n" + pdfFile.getAbsolutePath(),
                        Toast.LENGTH_LONG).show();
            }

        } catch (IOException e) {
            Toast.makeText(this,
                    "Error al guardar PDF: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
            if (out != null) {
                try { out.close(); } catch (IOException ignored) {}
            }
        }
    }



    /**
     * Dibuja una barra horizontal de color con texto encima.
     */
    private void drawBar(Canvas canvas, Paint paint,
                         int left, int top, int right,
                         int colorBar, String texto) {

        int height = 18;
        int radius = 8;

        // Fondo gris
        paint.setColor(Color.rgb(224, 224, 224));
        canvas.drawRoundRect(left, top, right, top + height, radius, radius, paint);

        // Barra de color (80%)
        int anchoUtil = (int) (0.8f * (right - left));
        paint.setColor(colorBar);
        canvas.drawRoundRect(left, top, left + anchoUtil, top + height, radius, radius, paint);

        // Texto
        paint.setColor(Color.BLACK);
        paint.setTextSize(10f);
        paint.setFakeBoldText(false);
        canvas.drawText(texto, left + 6, top + height - 4, paint);
    }
}
