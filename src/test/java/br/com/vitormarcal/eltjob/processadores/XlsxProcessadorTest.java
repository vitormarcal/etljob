package br.com.vitormarcal.eltjob.processadores;

import br.com.vitormarcal.eltjob.core.processadores.Processador;
import br.com.vitormarcal.eltjob.core.processadores.XlsxProcessador;
import br.com.vitormarcal.eltjob.helpers.MockPOJO;
import br.com.vitormarcal.eltjob.helpers.MockPojoFactory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class XlsxProcessadorTest {

    private final static String fileName = "teste";
    private String diretorioArquivo = "test/";
    private File diretorioBase;
    private File arquivo;

    @Before
    public void defineCaminhos() {
        diretorioArquivo = UUID.randomUUID() + "test/";
        diretorioBase = new File(diretorioArquivo);
        arquivo = new File(diretorioArquivo + "xlsx/" + fileName + ".xlsx");
    }

    @After
    public void deletaRecursosTemporarios() {
        if (diretorioBase.exists()) {
            Files.delete(diretorioBase);
        }
    }

    @Test
    public void givenFileObjectList_thenWritterXlsx() {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(true);

        process(mockPOJOList);

        assertTrue(arquivo.exists());
    }

    @Test
    public void givenXlsxAndFileObjectListThenHeaderEqualFieldName() {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(true);

        process(mockPOJOList);

        assertTrue(arquivo.exists());

        try (FileInputStream excelFile = new FileInputStream(arquivo)) {
            try (Workbook workbook = new XSSFWorkbook(excelFile)) {

                Sheet datatypeSheet = workbook.getSheetAt(0);
                assertNotNull(datatypeSheet);

                Iterator<Row> iterator = datatypeSheet.iterator();
                assertNotNull(iterator);

                Row currentRow = iterator.next();
                assertNotNull(currentRow);

                Iterator<Cell> cellIterator = currentRow.iterator();
                assertNotNull(cellIterator);
                assertTrue(cellIterator.hasNext());

                StringBuilder builder = new StringBuilder();
                while (cellIterator.hasNext()) {
                    Cell next = cellIterator.next();
                    builder.append(next.getStringCellValue()).append(";");
                }

                String expected = "nome;sobrenome;idade;cores;";

                assertEquals(expected, builder.toString());

            } catch (Exception e) {
                throw e;
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    @Test
    public void givenXlsxAndFileObjectListWithCollectionAttribute_whenIdenticalContent_thenCorrect() throws IOException {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(false);

        process(mockPOJOList);

        assertTrue(arquivo.exists());

        try (FileInputStream excelFile = new FileInputStream(arquivo)) {
            try (Workbook workbook = new XSSFWorkbook(excelFile)) {

                StringBuilder builder = getFirstOneObject(workbook);

                MockPOJO pojo = mockPOJOList.get(0);
                String cores = pojo.getCores().toString().subSequence(1, pojo.getCores().toString().length() - 1).toString();
                String expected = String.format("%s;%s;%s;%s;", pojo.getNome(), pojo.getSobrenome(), pojo.getIdade(), cores);

                assertEquals(expected, builder.toString());

            }
        }
    }


    @Test
    public void givenXlsxAndFileObjectListWithouCollectionAttribute_whenIdenticalContent_thenCorrect() throws IOException {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(true);

        process(mockPOJOList);

        assertTrue(arquivo.exists());

        try (FileInputStream excelFile = new FileInputStream(arquivo)) {
            try (Workbook workbook = new XSSFWorkbook(excelFile)) {

                StringBuilder builder = getFirstOneObject(workbook);

                MockPOJO pojo = mockPOJOList.get(0);
                String expected = String.format("%s;%s;%s;%s;", pojo.getNome(), pojo.getSobrenome(), pojo.getIdade(), "");

                assertEquals(expected, builder.toString());

            }
        }

    }

    private StringBuilder getFirstOneObject(Workbook workbook) {
        Sheet datatypeSheet = workbook.getSheetAt(0);
        assertNotNull(datatypeSheet);

        Iterator<Row> iterator = datatypeSheet.iterator();
        assertNotNull(iterator);

        iterator.next();//pula headers
        Row currentRow = iterator.next();
        assertNotNull(currentRow);

        Iterator<Cell> cellIterator = currentRow.iterator();
        assertNotNull(cellIterator);
        assertTrue(cellIterator.hasNext());

        StringBuilder builder = new StringBuilder();

        while (cellIterator.hasNext()) {
            Cell next = cellIterator.next();
            if (next.getCellType().equals(CellType.NUMERIC)) {
                int numericCellValue = Double.valueOf(next.getNumericCellValue()).intValue();
                builder.append(numericCellValue).append(";");
            } else {
                builder.append(next.getStringCellValue()).append(";");
            }
        }
        return builder;
    }


    private void process(List<MockPOJO> mockPOJOList) {
        try (Processador xlsx = new XlsxProcessador(fileName, diretorioArquivo)) {
            mockPOJOList.forEach(xlsx::writeLine);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


    @Test
    public void write() {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(10_000, true);
        try (Processador xlsx = new XlsxProcessador(fileName, diretorioArquivo)) {
            mockPOJOList.forEach(e -> assertTrue(xlsx.writeLine(e)));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        assertTrue(arquivo.exists());
    }

    @Test
    public void criarWriter() {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(10_000, true);
        try (Processador xlsx = new XlsxProcessador(fileName, diretorioArquivo)) {
            assertTrue(xlsx.inicializaWriter());
            mockPOJOList.forEach(e -> assertFalse(xlsx.inicializaWriter()));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        assertTrue(arquivo.exists());
    }

    @Test(expected = RuntimeException.class)
    public void close() throws Exception {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(5_000, true);

        Processador processador = null;

        try (Processador xlsx = new XlsxProcessador(fileName, diretorioArquivo)) {
            processador = xlsx;
            assertTrue(xlsx.inicializaWriter());
            mockPOJOList.forEach(e -> assertTrue(xlsx.writeLine(e)));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        assertTrue(arquivo.exists());
        processador.close();//stream já fechado, deve retornar JobProcessException
    }
}
