package com.mutualidad.modulo_credito.Utils;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.*;
import java.io.InputStream;

public class Report {

    public void mostrarReporte() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Ruta al archivo .jrxml
                String jrxmlFile = "Reports/desembolso.jrxml";
                InputStream jrxmlStream = Class.class.getResourceAsStream(jrxmlFile);
                JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, new JREmptyDataSource());
                JasperViewer viewer = new JasperViewer(jasperPrint, false);
                viewer.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error al generar el reporte", e);
            }
        });
    }



}
