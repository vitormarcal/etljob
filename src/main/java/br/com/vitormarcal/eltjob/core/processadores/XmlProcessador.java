package br.com.vitormarcal.eltjob.core.processadores;

import br.com.vitormarcal.eltjob.core.Coluna;
import br.com.vitormarcal.eltjob.core.FileObject;
import br.com.vitormarcal.eltjob.core.ItemList;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtilsBean;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Optional;

@Slf4j
public class XmlProcessador extends AbstractProcessador {

    private static final String DADOS = "dados";
    private static final String ROOT_NAME = "xml";
    private static final String DEFAULT_NAME_SPACE_URI = "";
    private static final String DEFAULT_PREFIX = "";
    private static final String DIRETORIO_XML = "/xml";
    private static final String EXTENSAO_XML = ".xml";
    private static final int SEM_PROFUNDIDADE = 0;
    private static final int TAB_PRIMEIRO_NIVEL = 2;
    private static final int TAB_SEGUNDO_NIVEL = 3;
    private static final boolean NOVA_LINHA = true;
    private static final boolean MESMA_LINHA = false;


    private final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    private final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private final PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
    private final XMLEvent end = eventFactory.createDTD("\n");
    private final XMLEvent tab = eventFactory.createDTD("\t");
    private final XMLEvent NL = eventFactory.createDTD("&#xA;");

    private XMLEventWriter eventWriter;
    private BufferedWriter bufferedWriter;

    public XmlProcessador(String fileName, String diretorioArquivo) {
        super(fileName + EXTENSAO_XML, diretorioArquivo + DIRETORIO_XML);
    }

    @Override
    protected boolean write(@NonNull FileObject fileObject) {
        try {
            createField(fileObject, TAB_PRIMEIRO_NIVEL);
            return true;
        } catch (XMLStreamException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Erro ao criar nodes do xml", e);
        }

    }


    private void createField(Object object, int profundidade) throws XMLStreamException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        String name = getNameObject(object);

        StartElement configStartElement = eventFactory.createStartElement(DEFAULT_PREFIX,
                DEFAULT_NAME_SPACE_URI, name);

        indenta(profundidade, NOVA_LINHA);
        eventWriter.add(configStartElement);

        for (Field field : object.getClass().getDeclaredFields()) {
            if (isFieldSerializable(field)) {

                String nome = getFiedName(field);

                Optional optionalField = Optional.ofNullable(propertyUtilsBean.getProperty(object, field.getName()));

                if (optionalField.isPresent() && isCollectionAssignableFrom(field)) {
                    trataCollection(profundidade, field, nome, (Collection) optionalField.get());
                } else if (optionalField.isPresent() && isFileObjectAssignableFrom(optionalField.get())) {
                    createField(optionalField.get(), profundidade + TAB_PRIMEIRO_NIVEL);
                } else {
                    createNode(eventWriter, nome, optionalField.isPresent() ? optionalField.get().toString() : "", profundidade + TAB_PRIMEIRO_NIVEL);
                }

            }
        }

        indenta(profundidade, NOVA_LINHA);
        eventWriter.add(eventFactory.createEndElement(DEFAULT_PREFIX, DEFAULT_NAME_SPACE_URI, name));
    }

    private String getNameObject(Object object) {
        Coluna annotation = object.getClass().getAnnotation(Coluna.class);

        return annotation != null && !annotation.tabular().isEmpty()
                ? annotation.tabular()
                : object.getClass().getSimpleName().toLowerCase();
    }

    private boolean isFileObjectAssignableFrom(Object objectField) {
        return FileObject.class.isAssignableFrom((objectField.getClass()));
    }

    private boolean isFieldSerializable(Field field) {
        return !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers());
    }

    private String getFiedName(Field field) {
        String nome = field.getName();

        if (field.isAnnotationPresent(Coluna.class)) {
            Coluna annotation = field.getAnnotation(Coluna.class);
            nome = annotation.xml().isEmpty() ? nome : annotation.xml();
        }
        return nome;
    }

    private void trataCollection(int profundidade, Field field, String nome, Collection objectField) throws XMLStreamException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (objectField != null) {
            StartElement cStartElement = eventFactory.createStartElement(DEFAULT_PREFIX,
                    DEFAULT_NAME_SPACE_URI, nome);

            indenta(profundidade + TAB_PRIMEIRO_NIVEL, NOVA_LINHA);
            eventWriter.add(cStartElement);

            boolean assignableFrom = isAssignableFrom(field);

            for (Object o : objectField) {
                if (assignableFrom) {
                    createField(o, profundidade + TAB_SEGUNDO_NIVEL);
                } else {
                    createItemCollection(profundidade, field, nome, o);
                }

            }

            if (!objectField.isEmpty()) {
                indenta(profundidade + TAB_PRIMEIRO_NIVEL, objectField.isEmpty() ? MESMA_LINHA : NOVA_LINHA);
            }
            eventWriter.add(eventFactory.createEndElement(DEFAULT_PREFIX, DEFAULT_NAME_SPACE_URI, nome));

        }
    }

    private void createItemCollection(int profundidade, Field field, String nome, Object o) throws XMLStreamException {
        String e = (String) o;
        ItemList annotation = field.getAnnotation(ItemList.class);
        String itemName = annotation != null && !annotation.itemCollection().isEmpty() ? annotation.itemCollection() : nome;
        createNode(eventWriter, itemName, e != null ? e : "", profundidade + TAB_SEGUNDO_NIVEL);
    }

    private boolean isAssignableFrom(Field field) {
        ParameterizedType pt = (ParameterizedType) field.getGenericType();
        return FileObject.class.isAssignableFrom((Class<?>) pt.getActualTypeArguments()[0]);
    }

    private boolean isCollectionAssignableFrom(Field field) {
        return Collection.class.isAssignableFrom(field.getType());
    }

    private void indenta(int profundidade, boolean newLine) throws XMLStreamException {
        if (eventWriter != null) {
            if (newLine) eventWriter.add(end);
            while (profundidade != SEM_PROFUNDIDADE) {
                profundidade--;
                eventWriter.add(tab);
            }
        }
    }

    private void createNode(XMLEventWriter eventWriter, String name,
                            String value, int profundidade) throws XMLStreamException {

        // create Start node
        StartElement sElement = eventFactory.createStartElement(DEFAULT_PREFIX, DEFAULT_NAME_SPACE_URI, name);
        indenta(profundidade, NOVA_LINHA);
        eventWriter.add(sElement);

        String formattedValue = value.replaceAll("[\n\t\r]", "\n");
        String[] splits = formattedValue.split("\n");

        // create Content
        if (splits.length > 1) {
            witterContentWithLFCharacter(eventWriter, splits);
        } else {
            Characters characters = eventFactory.createCharacters(formattedValue);
            eventWriter.add(characters);
        }

        // create End node
        EndElement eElement = eventFactory.createEndElement(DEFAULT_PREFIX, DEFAULT_NAME_SPACE_URI, name);
        eventWriter.add(eElement);
    }

    private void witterContentWithLFCharacter(XMLEventWriter eventWriter, String[] splits) throws XMLStreamException {
        for (int i = 0; i < splits.length; i++) {
            if (!splits[i].isEmpty()) {
                Characters characters = eventFactory.createCharacters(splits[i]);
                eventWriter.add(characters);
                if (i + 1 < splits.length) {
                    eventWriter.add(NL);
                }
            }
        }
    }

    @Override
    protected boolean criarWriter() {
        if (eventWriter == null) {
            try {
                bufferedWriter = new BufferedWriter(new FileWriter(file));
                eventWriter = outputFactory
                        .createXMLEventWriter(bufferedWriter);

                StartDocument startDocument = eventFactory.createStartDocument();
                eventWriter.add(startDocument);
                eventWriter.add(end);

                abreTagXml();
                return true;
            } catch (IOException | XMLStreamException e) {
                log.error("Um erro aconteceu ao criar o writer do xml ", e);
            }

        }
        return false;
    }

    @Override
    public void close() {
        try {
            if (eventWriter != null) {
                fechaTagXml();
                fechaTagDocumento();
                eventWriter.close();
                bufferedWriter.close();
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void fechaTagDocumento() throws XMLStreamException {
        if (eventWriter != null) {
            EndDocument endDocument = eventFactory.createEndDocument();
            indenta(SEM_PROFUNDIDADE, MESMA_LINHA);
            eventWriter.add(endDocument);
        }
    }

    private void abreTagXml() throws XMLStreamException {
        StartElement xml = eventFactory.createStartElement(DEFAULT_PREFIX,
                DEFAULT_NAME_SPACE_URI, ROOT_NAME);
        eventWriter.add(xml);
        indenta(1, true);
        StartElement dados = eventFactory.createStartElement(DEFAULT_PREFIX,
                DEFAULT_NAME_SPACE_URI, DADOS);

        eventWriter.add(dados);
    }

    private void fechaTagXml() throws XMLStreamException {
        if (eventWriter != null) {
            indenta(1, true);
            eventWriter.add(eventFactory.createEndElement(DEFAULT_PREFIX,
                    DEFAULT_NAME_SPACE_URI, DADOS));
            indenta(SEM_PROFUNDIDADE, NOVA_LINHA);
            eventWriter.add(eventFactory.createEndElement(DEFAULT_PREFIX, DEFAULT_NAME_SPACE_URI, ROOT_NAME));
        }
    }
}
