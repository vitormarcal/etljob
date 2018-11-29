package br.com.vitormarcal.eltjob.core.processadores;


import br.com.vitormarcal.eltjob.core.FileObject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Slf4j
public class XlsxProcessador extends AbstractProcessador {

    private static final int LINHA_CABECALHO = 0;
    private static final int LINHA_PRIMEIRO_REGISTRO = 1;

    private static final String DIRETORIO_XLSX = "/xlsx";
    private static final String EXTENSAO_XLSX = ".xlsx";

    private final String sheetName;
    private Workbook workbook;
    private FileOutputStream outputStream;
    private Sheet sheet;
    private Row cabecalho;
    private int numeroLinhaRegistro;
    private CellStyle cellStyle;


    public XlsxProcessador(String fileName, String diretorioArquivo) {
        super(fileName + EXTENSAO_XLSX, diretorioArquivo + DIRETORIO_XLSX);
        this.sheetName = fileName;
        numeroLinhaRegistro = LINHA_PRIMEIRO_REGISTRO;
    }

    @Override
    protected boolean write(@NonNull FileObject fileObject) {
        final Map<String, String> mapa;
        try {
            mapa = fileObject.mapeiaFields();
            criaCabecalho(mapa.keySet());

            Iterator<Map.Entry<String, String>> iterator = mapa.entrySet().iterator();
            int indexCell = 0;
            Row row = sheet.createRow(numeroLinhaRegistro);
            do {
                Cell cell = row.createCell(indexCell);
                String value = iterator.next().getValue();
                if (NumberUtils.isParsable(value) && NumberUtils.isCreatable(value)) {
                    if (NumberUtils.isDigits(value)) {
                        cell.setCellValue(NumberUtils.createNumber(value).longValue());
                    } else {
                        cell.setCellValue(NumberUtils.createDouble(value));
                        cell.setCellStyle(cellStyle);
                    }
                } else {
                    cell.setCellValue(value);
                }
                indexCell++;
            } while (iterator.hasNext());
            numeroLinhaRegistro++;
            return true;
        } catch (IllegalAccessException | NoSuchMethodException | NumberFormatException | InvocationTargetException e) {
            throw new RuntimeException("Erro ao fazer o parse dos dados para o formato xlsx", e);
        }
    }

    private void criaCabecalho(Set<String> headers) {
        if (cabecalho == null) {
            cabecalho = sheet.createRow(LINHA_CABECALHO);
            criarCelulas(headers, cabecalho);
        }
    }

    private void criarCelulas(Set<String> celulas, Row linha) {
        Iterator<String> iterator = celulas.iterator();
        int index = 0;
        do {
            Cell cell = linha.createCell(index);
            cell.setCellValue(iterator.next());
            index++;
        } while (iterator.hasNext());
    }

    @Override
    protected boolean criarWriter() {
        if (workbook == null) {
            try {
                outputStream = new FileOutputStream(file);
                workbook = new SXSSFWorkbook(100);
                cellStyle = workbook.createCellStyle();
                cellStyle.setDataFormat(workbook.createDataFormat().getFormat("###0.00"));
                sheet = workbook.createSheet(sheetName);
                return true;
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Erro ao criar writer do xlsx", e);
            }
        } else {
            return false;
        }
    }

    @Override
    public void close() {
        try {
            if (this.outputStream != null && this.workbook != null) {
                this.workbook.write(this.outputStream);
                this.workbook.close();
                this.outputStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao fechar processador xlsx", e);
        }
    }
}
