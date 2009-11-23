package words.utils;


import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;

/**
 * 
 * Convierte un fichero XML en texto.
 *
 */
class XML2text extends DefaultHandler {

    StringBuilder buffer = new StringBuilder(10000);

    private boolean nota = false; // útil para eliminar las notas del TXT final.
    private boolean showHeader = true; // útil per a mostrar/ocultar les capçaleres
    private boolean inHeader = false;

    @Override
    public void characters(char[] c, int start, int length) {
        if (length > 0) {
            boolean wr = true;
            try {
                // Si no estamos en una etiqueta "note" añadimos el contenido a buffer.
                if (nota) {
                    wr = false;
                }
                if (inHeader && !showHeader) {
                    wr = false;
                }
                if (wr) {
                    buffer.append(c, start, length);
                }
            } catch (java.nio.BufferOverflowException x) {
                System.err.println("Insufficient text buffer size");
                System.exit(1);
            }
        }
    }

    @Override
    public void startElement(String uri, String localName,
            String tag, Attributes attributes) {
        if (tag.equals("note")) {
            nota = true;
        }
        if(!nota)
            buffer.append(" ");
        // Si nos encontramos con la etiqueta "teiHeader" significa que comenzamos con
        // la cabecera del documento XML y por tanto lo marcamos para saber en el texto llano
        // donde comienza dicha cabecera.
        if (tag.equals("teiHeader")) {
            inHeader = true;
            if (showHeader)
                buffer.append(" _CABECERA_INICIAL_ ");
        // Comienzo de una etiqueta "note". Por tanto ponemos nota = true para saberlo en characters.
        }
    }

    @Override
    public void endElement(String uri, String localName, String tag) {
        // Para saber donde termina la cabecera del documento XML en el fichero de texto llano
        // colocamos una marca. De esta forma luego podemos eliminar el contenido de la cabecera en
        // el fichero llano.
        if (tag.equals("teiHeader")) {
            if (showHeader)
                buffer.append(" \\_CABECERA_INICIAL_ ");
            inHeader = false;
        
        }
        // Ya no estamos en una etiqueta "note".
        if (tag.equals("note")) {
            nota = false;
        }
        if(!nota)
            buffer.append(" ");
    }

    private XMLReader getXMLReader() {
        XMLReader reader = null;
        try {
            reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            reader.setContentHandler(this);
        } catch (Exception x) {
            System.err.println(x.getMessage());
        }
        return reader;
    }

    public String getText(String fileName) {
        XMLReader reader = getXMLReader();
        try {
            reader.parse(fileName);
        } catch (Exception x) {
            System.err.println("Error parsing " + fileName +
                    ": " + x.getMessage());
        }
        return buffer.toString();
    }
}
