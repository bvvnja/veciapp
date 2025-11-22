package com.example.proyecto_de_integracion;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CobranzaAdapter extends RecyclerView.Adapter<CobranzaAdapter.CobranzaViewHolder> {

    private Context context;
    private List<Cobranza> cobranzaList;
    private String uidUsuario;

    public CobranzaAdapter(Context context, List<Cobranza> cobranzaList, String uidUsuario) {
        this.context = context;
        this.cobranzaList = cobranzaList;
        this.uidUsuario = uidUsuario;
    }

    @Override
    public CobranzaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_cobranza, parent, false);
        return new CobranzaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CobranzaViewHolder holder, int position) {
        Cobranza cobranza = cobranzaList.get(position);

        holder.txtMesCobranza.setText(cobranza.getMesNombre() + " " + cobranza.getAnio());

        String estado = cobranza.getEstadoPago() != null ? cobranza.getEstadoPago() : "Pendiente";
        holder.txtEstadoCobranza.setText(estado);

        if ("Pagado".equalsIgnoreCase(estado)) {
            holder.txtEstadoCobranza.setTextColor(Color.parseColor("#4CAF50")); // verde
        } else {
            holder.txtEstadoCobranza.setTextColor(Color.parseColor("#D32F2F")); // rojo
        }

        holder.txtDeptoCobranza.setText("Depto " + cobranza.getNumeroDepto());
        holder.txtMontoCobranza.setText("$" + String.format("%,.2f", cobranza.getMonto()));
        holder.txtDescripcionCobranza.setText(cobranza.getDescripcion());

        boolean pagado = "Pagado".equalsIgnoreCase(estado);

        if (pagado) {
            holder.btnPagarCobranza.setText("Descargar comprobante");
        } else {
            holder.btnPagarCobranza.setText("Pagar ahora");
        }

        holder.btnPagarCobranza.setOnClickListener(v -> {
            if (!pagado) {
                // Ir a la pantalla de pago con detalles de ESTA cobranza
                Intent intent = new Intent(context, PagoTarjetaActivity.class);
                intent.putExtra("uidUsuario", uidUsuario);
                intent.putExtra("mesClave", cobranza.getMesClave());
                intent.putExtra("mesNombre", cobranza.getMesNombre());
                intent.putExtra("anio", cobranza.getAnio());
                intent.putExtra("monto", cobranza.getMonto());
                intent.putExtra("descripcion", cobranza.getDescripcion());
                intent.putExtra("numeroDepto", cobranza.getNumeroDepto());
                context.startActivity(intent);
            } else {
                generarComprobantePDF(cobranza);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cobranzaList.size();
    }

    public static class CobranzaViewHolder extends RecyclerView.ViewHolder {
        TextView txtMesCobranza, txtEstadoCobranza, txtDeptoCobranza,
                txtMontoCobranza, txtDescripcionCobranza;
        Button btnPagarCobranza;

        public CobranzaViewHolder(View itemView) {
            super(itemView);
            txtMesCobranza = itemView.findViewById(R.id.txtMesCobranza);
            txtEstadoCobranza = itemView.findViewById(R.id.txtEstadoCobranza);
            txtDeptoCobranza = itemView.findViewById(R.id.txtDeptoCobranza);
            txtMontoCobranza = itemView.findViewById(R.id.txtMontoCobranza);
            txtDescripcionCobranza = itemView.findViewById(R.id.txtDescripcionCobranza);
            btnPagarCobranza = itemView.findViewById(R.id.btnPagarCobranza);
        }
    }

    // ------------------------ PDF con IVA 19 % ------------------------
    private void generarComprobantePDF(Cobranza c) {
        PdfDocument pdfDocument = new PdfDocument();

        int pageWidth = 595;
        int pageHeight = 842;

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        Paint boxPaint = new Paint();

        // CABECERA: logo + datos Veciapp
        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icono_veciapp);
        if (logo != null) {
            int logoWidth = 90;
            int logoHeight = (int) (logo.getHeight() * (logoWidth / (float) logo.getWidth()));
            Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, logoWidth, logoHeight, true);
            canvas.drawBitmap(scaledLogo, 40, 30, null);
        }

        paint.setColor(Color.BLACK);
        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        canvas.drawText("Veciapp - Gastos Comunes", 150, 50, paint);

        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        int y = 70;
        canvas.drawText("Administración de Comunidad Vecinal", 150, y, paint);
        y += 15;
        canvas.drawText("Correo: soporte@veciapp.cl", 150, y, paint);
        y += 15;
        canvas.drawText("Sitio web: www.veciapp.cl", 150, y, paint);

        // CUADROS DATOS
        boxPaint.setStyle(Paint.Style.FILL);
        boxPaint.setColor(Color.rgb(240, 240, 240));

        RectF rectCliente = new RectF(30, 110, pageWidth / 2f - 10, 230);
        canvas.drawRoundRect(rectCliente, 8, 8, boxPaint);

        RectF rectFactura = new RectF(pageWidth / 2f + 10, 110, pageWidth - 30, 230);
        canvas.drawRoundRect(rectFactura, 8, 8, boxPaint);

        paint.setColor(Color.rgb(0, 86, 155));
        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        canvas.drawText("DATOS DEL RESIDENTE", rectCliente.left + 10, rectCliente.top + 20, paint);
        canvas.drawText("DATOS DEL PAGO", rectFactura.left + 10, rectFactura.top + 20, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(12);
        paint.setFakeBoldText(false);

        int yc = (int) rectCliente.top + 40;
        canvas.drawText("Nombre: " + (c.getNombres() != null ? c.getNombres() : "-"), rectCliente.left + 10, yc, paint);
        yc += 15;
        canvas.drawText("Depto: " + (c.getNumeroDepto() != null ? c.getNumeroDepto() : "-"), rectCliente.left + 10, yc, paint);
        yc += 15;
        canvas.drawText("Comunidad: Veciapp", rectCliente.left + 10, yc, paint);

        int yf = (int) rectFactura.top + 40;
        canvas.drawText("Comprobante: " + (c.getMesClave() != null ? c.getMesClave() : "-"), rectFactura.left + 10, yf, paint);
        yf += 15;
        canvas.drawText("Mes cobrado: " + c.getMesNombre() + " " + c.getAnio(), rectFactura.left + 10, yf, paint);
        yf += 15;
        canvas.drawText("Estado: Pagado", rectFactura.left + 10, yf, paint);

        // TABLA DETALLE
        int tablaTop = 260;
        int tablaLeft = 30;
        int tablaRight = pageWidth - 30;

        boxPaint.setColor(Color.rgb(230, 230, 230));
        RectF headerRect = new RectF(tablaLeft, tablaTop, tablaRight, tablaTop + 25);
        canvas.drawRect(headerRect, boxPaint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(12);
        paint.setFakeBoldText(true);

        int colDesc = tablaLeft + 10;
        int colPrecio = tablaLeft + 260;
        int colCant = tablaLeft + 360;
        int colTotal = tablaLeft + 430;

        int baseY = tablaTop + 17;
        canvas.drawText("Descripción", colDesc, baseY, paint);
        canvas.drawText("Precio", colPrecio, baseY, paint);
        canvas.drawText("Cantidad", colCant, baseY, paint);
        canvas.drawText("Total", colTotal, baseY, paint);

        paint.setFakeBoldText(false);

        int filaY = tablaTop + 45;
        String descripcion = c.getDescripcion() != null ? c.getDescripcion() : "Gastos comunes";
        canvas.drawText(descripcion, colDesc, filaY, paint);
        canvas.drawText("$" + String.format("%,.0f", c.getMonto()), colPrecio, filaY, paint);
        canvas.drawText("1", colCant, filaY, paint);
        canvas.drawText("$" + String.format("%,.0f", c.getMonto()), colTotal, filaY, paint);

        // SUBTOTAL / IVA 19 % / TOTAL (número cerrado)
        long subtotal = Math.round(c.getMonto());
        long iva = Math.round(subtotal * 0.19);
        long total = subtotal + iva;

        int resumenTop = filaY + 40;
        int labelX = tablaRight - 180;
        int valueX = tablaRight - 30;

        paint.setFakeBoldText(true);
        canvas.drawText("SUBTOTAL", labelX, resumenTop, paint);
        paint.setFakeBoldText(false);
        canvas.drawText("$" + String.format("%,d", subtotal), valueX, resumenTop, paint);

        resumenTop += 18;
        paint.setFakeBoldText(true);
        canvas.drawText("IVA 19%", labelX, resumenTop, paint);
        paint.setFakeBoldText(false);
        canvas.drawText("$" + String.format("%,d", iva), valueX, resumenTop, paint);

        resumenTop += 18;
        paint.setFakeBoldText(true);
        canvas.drawText("TOTAL", labelX, resumenTop, paint);
        paint.setFakeBoldText(false);
        canvas.drawText("$" + String.format("%,d", total), valueX, resumenTop, paint);

        // Nota al pie
        paint.setTextSize(10);
        paint.setColor(Color.DKGRAY);
        canvas.drawText("Comprobante generado automáticamente por Veciapp.", 30, pageHeight - 40, paint);

        pdfDocument.finishPage(page);

        String fileName = "comprobante_veciapp_" + (c.getMesClave() != null ? c.getMesClave() : "pago") + ".pdf";
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            fos.close();
            Toast.makeText(context,
                    "Comprobante guardado en Descargas como " + fileName,
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(context,
                    "Error al guardar PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } finally {
            pdfDocument.close();
        }
    }
}
