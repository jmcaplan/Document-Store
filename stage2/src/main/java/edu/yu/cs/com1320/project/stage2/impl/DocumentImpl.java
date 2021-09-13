package edu.yu.cs.com1320.project.stage2.impl;

import java.io.*;
import java.net.URI;
import edu.yu.cs.com1320.project.stage2.*;
import org.apache.pdfbox.pdmodel.*;
//import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;


public class DocumentImpl implements Document {

	private int textHashCode;
	private URI uri;
	private String documentAsTxt;
	private byte[] documentAsPDF;
	
	public DocumentImpl(URI uri, String documentAsTxt, int textHashCode) {
		rejectNulls(uri,documentAsTxt);
		this.textHashCode = textHashCode;
		this.uri = uri;
		this.documentAsTxt = documentAsTxt.trim().replace("\n", " ");
	}
	
	public DocumentImpl(URI uri, String documentAsTxt, int textHashCode, byte[] documentAsPDF) {
		this(uri, documentAsTxt, textHashCode);
		this.documentAsPDF = documentAsPDF;
	}
	
	private void rejectNulls(URI uri,String documentAsTxt) {
		if (uri == null || documentAsTxt == null) {
			throw new IllegalArgumentException("Cannot create DocumentImpl with null URI or Text");
		}
	}
	
	/**
     * @return the document as a PDF
     */
    public byte[] getDocumentAsPdf() {
    	if (documentAsPDF != null) {
    		return documentAsPDF;
    	} // if this instance of Document was originally constructed with byte[], or if getDocumentAsPDF has been called previously,
    	  // we have assigned value to field documentAsPDF, so we just return that
    	else {
    		PDDocument document = new PDDocument();
    		PDPage newPage = new PDPage();
    		document.addPage(newPage);
    		
    		try (PDPageContentStream contents = new PDPageContentStream(document, newPage)) {
    			contents.setFont(PDType1Font.TIMES_BOLD, 8.0f);
    			contents.beginText();
    			contents.newLineAtOffset(0, 0);
    			contents.showText(documentAsTxt);
    			contents.endText();
    			contents.close();
    			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    			document.save(byteArrayOutputStream);
    			//document.save("Hello_Mom2.pdf");
    			document.close(); 
        		return byteArrayOutputStream.toByteArray();
    		}
    		catch (IOException e) {
    			e.printStackTrace();
    			return null;
    		}
    	}
    }

    /**
     * @return the document as a Plain String
     */
    public String getDocumentAsTxt() {
    	return documentAsTxt;
    }

    /**
     * @return hash code of the plain text version of the document
     */
    public int getDocumentTextHashCode() {
    	return textHashCode;
    }

    /**
     * @return URI which uniquely identifies this document
     */
    public URI getKey() {
    	return uri;
    }
}
