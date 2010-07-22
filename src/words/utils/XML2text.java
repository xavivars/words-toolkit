package words.utils;

import java.util.Arrays;
import java.util.List;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;
import words.utils.CmdOptions.*;

/**
 * 
 * Convierte un fichero XML en texto.
 *
 */
class XML2text extends DefaultHandler {

    StringBuilder buffer = new StringBuilder(10000);
    private boolean showNote = false; // útil para eliminar las notas del TXT final.
    private boolean inNote = false;
    private boolean showHeader = false; // útil per a mostrar/ocultar les capçaleres
    private boolean inHeader = false;
    private boolean inDocEdition = false;
    private boolean inForeign = false;
    private boolean inAbbr = false;
    private StringBuilder abbrContent = null;
    private List<String> newline;
    private List<String> dot;

    public static void main(String[] args) {
        new XML2text(args);
    }

    public XML2text(String[] args) {
        CmdOptions parser = new CmdOptions();
        CmdOptionTester optionTester = new CmdOptionTester();

        Option file = parser.addStringOption('f', "file");
        Option note = parser.addBooleanOption('n', "note");
        Option header = parser.addBooleanOption('h', "header");

        String [] ar_n = {"p","abbr","header","docAuthor","speaker"};
        String [] ar_d = {"head"};
        newline = Arrays.asList(ar_n);
        dot = Arrays.asList(ar_d);

        try {
            parser.parse(args);

            showNote = optionTester.testBoolean(parser, note);
            showHeader = optionTester.testBoolean(parser, header);
            String f = optionTester.testFile(parser, file, false, true, true);

            System.out.print(this.getText(f));

        } catch (Exception e) {
            System.err.println(e);
        }
    }

    @Override
    public void characters(char[] c, int start, int length) {
        if (length > 0) {
            boolean wr = true;
            try {
                // Si no estamos en una etiqueta "note" añadimos el contenido a buffer.
                if (inNote && !showNote) {
                    wr = false;
                }
                if (inHeader && !showHeader) {
                    wr = false;
                }
                if (inForeign) {
                    //wr = false;
                }
                if (inDocEdition) {
                    wr = false;
                }

                if(inAbbr) {
                    if(abbrContent == null)
                        abbrContent = new StringBuilder();

                    abbrContent.append(c,start,length);
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
    public void startElement(String uri, String localName, String tag, Attributes attributes) {
        if (tag.equals("teiHeader")) {
            inHeader = true;
            if (showHeader) {
                buffer.append(" _CABECERA_INICIAL_ ");
            }
        }

        if (tag.equals("note")) {
            inNote = true;
        }

        if (tag.equals("docEdition") || tag.equals("front")) {
            inDocEdition = true;
        }

        if(tag.equals("foreign")) {
            inForeign = true;
        }

        if(tag.equals("abbr")) {
            inAbbr = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String tag) {
        // Para saber donde termina la cabecera del documento XML en el fichero de texto llano
        // colocamos una marca. De esta forma luego podemos eliminar el contenido de la cabecera en
        // el fichero llano.
        if (tag.equals("teiHeader")) {
            if (showHeader) {
                buffer.append(" \\_CABECERA_INICIAL_ ");
            }
            inHeader = false;

        }

        if (tag.equals("docEdition") || tag.equals("front")) {
            inDocEdition = false;
        }

        if(tag.equals("foreign")) {
            inForeign = false;
        }

        // Ya no estamos en una etiqueta "note".
        if (tag.equals("note")) {
            inNote = false;
            /* buffer.append(" "); */
        }

        if(tag.equals("abbr")) {
            buffer.append(abbrContent.toString());
            abbrContent = null;
            inAbbr = false;
        }

        if(newline.contains(tag)) {
            buffer.append("\n\n");
        }

        if(dot.contains(tag)) {
            buffer.append(".\n\n");
        }
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
            if(fileName.equalsIgnoreCase("-"))
                reader.parse(new InputSource(System.in));
            else
                reader.parse(fileName);
        } catch (Exception x) {
            System.err.println("Error parsing " + fileName
                    + ": " + x.getMessage());
        }
        return buffer.toString();
    }
}
